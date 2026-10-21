
import { useEffect, useState } from "react";
import "./App.css";

const API = "http://localhost:8080/api";

function App() {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState([]);
  const [hold, setHold] = useState(null);
  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(false);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  // Fetch events when the application first loads.
  useEffect(() => {
    loadEvents();
  }, []);

  // Shared helper for calling the Spring Boot API.
  async function apiRequest(path, options = {}) {
    const response = await fetch(`${API}${path}`, {
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers,
      },
    });

    const text = await response.text();
    let data = null;

    try {
      data = text ? JSON.parse(text) : null;
    } catch {
      data = text;
    }

    if (!response.ok) {
      throw new Error(
        data?.message ||
          data?.error ||
          (typeof data === "string" ? data : "") ||
          `Request failed with status ${response.status}`
      );
    }

    return data;
  }

  async function loadEvents() {
    setLoading(true);
    setError("");

    try {
      const data = await apiRequest("/events");
      setEvents(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(`Could not load events: ${err.message}`);
    } finally {
      setLoading(false);
    }
  }

  // Open an event and fetch its current seat statuses.
  async function openEvent(event) {
    setSelectedEvent(event);
    setSeats([]);
    setSelectedSeatIds([]);
    setHold(null);
    setTicket(null);
    setError("");
    setMessage("");
    setWorking(true);

    try {
      const data = await apiRequest(`/events/${event.id}/seats`);
      setSeats(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(`Could not load seats: ${err.message}`);
    } finally {
      setWorking(false);
    }
  }

  function goHome() {
    setSelectedEvent(null);
    setSeats([]);
    setSelectedSeatIds([]);
    setHold(null);
    setTicket(null);
    setError("");
    setMessage("");
  }

  // Toggle an available seat in the customer's selection.
  function toggleSeat(seat) {
    if (seat.status !== "AVAILABLE" || hold || working) {
      return;
    }

    setError("");

    setSelectedSeatIds((current) => {
      if (current.includes(seat.id)) {
        return current.filter((id) => id !== seat.id);
      }

      // The backend request supports at most 20 seats.
      if (current.length >= 20) {
        setError("You can select a maximum of 20 seats.");
        return current;
      }

      return [...current, seat.id];
    });
  }

  const selectedSeats = seats.filter((seat) =>
    selectedSeatIds.includes(seat.id)
  );

  const selectedTotal = selectedSeats.reduce(
    (sum, seat) => sum + Number(seat.price),
    0
  );

  function money(amount) {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(Number(amount || 0));
  }

  function formatDate(value) {
    if (!value) return "Date to be announced";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return "Date to be announced";
    }

    return date.toLocaleString("en-IN", {
      dateStyle: "medium",
      timeStyle: "short",
    });
  }

  // Create a five-minute hold through the backend.
  async function createHold() {
    if (!selectedEvent || selectedSeatIds.length === 0) {
      setError("Please select at least one available seat.");
      return;
    }

    setWorking(true);
    setError("");
    setMessage("");

    try {
      const result = await apiRequest("/bookings/hold", {
        method: "POST",
        body: JSON.stringify({
          eventId: selectedEvent.id,
          seatIds: selectedSeatIds,
        }),
      });

      setHold(result);
      setSelectedSeatIds([]);

      // Refresh the layout so the held seats appear unavailable.
      const latest = await apiRequest(
        `/events/${selectedEvent.id}/seats`
      );
      setSeats(Array.isArray(latest) ? latest : []);

      setMessage("Seats held. Please complete payment within 5 minutes.");
    } catch (err) {
      setError(`Could not hold seats: ${err.message}`);

      // Refresh statuses in case another customer took a seat.
      try {
        const latest = await apiRequest(
          `/events/${selectedEvent.id}/seats`
        );
        setSeats(Array.isArray(latest) ? latest : []);
      } catch {
        // Preserve the original error.
      }
    } finally {
      setWorking(false);
    }
  }

  // Call the simulated payment endpoint.
  async function pay(success) {
    if (!hold?.bookingCode) return;

    setWorking(true);
    setError("");
    setMessage("");

    try {
      const result = await apiRequest(
        `/bookings/${hold.bookingCode}/payment`,
        {
          method: "POST",
          body: JSON.stringify({ success }),
        }
      );

      if (success && result.status === "CONFIRMED") {
        const ticketData = await apiRequest(
          `/tickets/${hold.bookingCode}`
        );

        setTicket(ticketData);
        setHold(null);
        setMessage("Payment successful. Your ticket is ready.");
      } else {
        setHold(null);
        setSelectedSeatIds([]);
        setMessage(
          result.message || "Payment failed. Your seats have been released."
        );

        const latest = await apiRequest(
          `/events/${selectedEvent.id}/seats`
        );
        setSeats(Array.isArray(latest) ? latest : []);
      }
    } catch (err) {
      setError(`Payment request failed: ${err.message}`);
    } finally {
      setWorking(false);
    }
  }

  function seatClass(seat) {
    if (selectedSeatIds.includes(seat.id)) return "seat seat-selected";
    return `seat seat-${seat.status.toLowerCase()}`;
  }

  // Group seats by their row label, such as A and B.
  const seatRows = [...new Set(seats.map((seat) => seat.rowLabel))].sort();

  return (
    <div className="app">
      <header className="navbar">
        <button className="brand" onClick={goHome} type="button">
          <span className="brand-icon">H</span>
          <span>housefull</span>
        </button>

        <span className="nav-label">
          Live events. Unforgettable moments.
        </span>

        <button className="nav-button" onClick={goHome} type="button">
          All events
        </button>
      </header>

      <main>
        {!selectedEvent ? (
          <>
            <section className="hero">
              <div className="hero-content">
                <span className="eyebrow">
                  YOUR NEXT EXPERIENCE STARTS HERE
                </span>

                <h1>
                  Make room for
                  <br />
                  <span>great moments.</span>
                </h1>

                <p>
                  Discover live events, choose your seats, and book your next
                  unforgettable experience.
                </p>

                <a className="primary-button" href="#events">
                  Explore events <span>→</span>
                </a>
              </div>

              <div className="hero-art" aria-hidden="true">
                <div className="hero-circle circle-one" />
                <div className="hero-circle circle-two" />

                <div className="hero-ticket">
                  <span className="ticket-small">YOUR NEXT EXPERIENCE</span>
                  <span className="ticket-title">LIVE IT.</span>
                  <span className="ticket-title ticket-title-accent">
                    LOVE IT.
                  </span>
                  <span className="ticket-bottom">
                    MUSIC · COMEDY · MORE
                  </span>
                </div>

                <span className="floating-star star-one">✳</span>
                <span className="floating-star star-two">✦</span>
              </div>
            </section>

            <section className="events-section" id="events">
              <div className="section-heading">
                <div>
                  <span className="eyebrow">FIND YOUR NEXT EXPERIENCE</span>
                  <h2>Upcoming events</h2>
                  <p>Pick an event. We'll help you get there.</p>
                </div>

                {!loading && !error && (
                  <span className="event-count">
                    {events.length} {events.length === 1 ? "event" : "events"}
                  </span>
                )}
              </div>

              {loading && (
                <div className="state-message">
                  <div className="loader" />
                  <p>Finding events for you...</p>
                </div>
              )}

              {error && (
                <div className="state-message error-state">
                  <h3>Events could not be loaded</h3>
                  <p>{error}</p>
                  <button
                    className="primary-button"
                    onClick={loadEvents}
                    type="button"
                  >
                    Try again
                  </button>
                </div>
              )}

              {!loading && !error && events.length === 0 && (
                <div className="state-message">
                  <h3>No events available yet</h3>
                  <p>Check back soon for new experiences.</p>
                </div>
              )}

              {!loading && !error && events.length > 0 && (
                <div className="events-grid">
                  {events.map((event, index) => (
                    <article className="event-card" key={event.id}>
                      <div className={`event-art event-art-${index % 3}`}>
                        <span className="event-category">
                          {event.name?.toLowerCase().includes("music")
                            ? "MUSIC"
                            : event.name?.toLowerCase().includes("comedy")
                              ? "COMEDY"
                              : "LIVE EVENT"}
                        </span>

                        <span className="event-art-symbol" aria-hidden="true">
                          {event.name?.toLowerCase().includes("music")
                            ? "♫"
                            : event.name?.toLowerCase().includes("comedy")
                              ? "☻"
                              : "✳"}
                        </span>

                        <span className="event-art-caption">
                          MAKE IT A MOMENT
                        </span>
                      </div>

                      <div className="event-details">
                        <h3>{event.name}</h3>

                        <p className="event-info">
                          <span>⌖</span>
                          {event.venue || "Venue to be announced"}
                        </p>

                        <p className="event-info">
                          <span>◷</span>
                          {formatDate(event.eventDateTime)}
                        </p>

                        <div className="event-card-footer">
                          <span className="seat-info">
                            {event.totalSeats ?? "—"} seats
                          </span>

                          <button
                            className="secondary-button"
                            onClick={() => openEvent(event)}
                            type="button"
                          >
                            View seats →
                          </button>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </section>
          </>
        ) : (
          <section className="booking-section">
            <button className="back-button" onClick={goHome} type="button">
              ← Back to events
            </button>

            <div className="booking-heading">
              <span className="eyebrow">CHOOSE YOUR EXPERIENCE</span>
              <h1>{selectedEvent.name}</h1>
              <p>
                {selectedEvent.venue} ·{" "}
                {formatDate(selectedEvent.eventDateTime)}
              </p>
            </div>

            {error && <div className="notice error-notice">{error}</div>}
            {message && (
              <div className="notice success-notice">{message}</div>
            )}

            {working && (
              <div className="notice">Processing your request...</div>
            )}

            {!ticket && (
              <div className="booking-layout">
                <div className="seat-panel">
                  <div className="screen">STAGE / SCREEN</div>

                  <div className="seat-legend">
                    <span><i className="legend-dot available-dot" /> Available</span>
                    <span><i className="legend-dot selected-dot" /> Selected</span>
                    <span><i className="legend-dot held-dot" /> Held</span>
                    <span><i className="legend-dot booked-dot" /> Booked</span>
                  </div>

                  {seatRows.map((row) => (
                    <div className="seat-row" key={row}>
                      <span className="row-label">{row}</span>

                      <div className="seat-row-items">
                        {seats
                          .filter((seat) => seat.rowLabel === row)
                          .sort((a, b) => a.seatNumber - b.seatNumber)
                          .map((seat) => (
                            <button
                              key={seat.id}
                              type="button"
                              className={seatClass(seat)}
                              disabled={
                                seat.status !== "AVAILABLE" ||
                                Boolean(hold) ||
                                working
                              }
                              onClick={() => toggleSeat(seat)}
                              title={`${row}${seat.seatNumber} · ${money(seat.price)} · ${seat.status}`}
                            >
                              {seat.seatNumber}
                            </button>
                          ))}
                      </div>
                    </div>
                  ))}

                  {!working && seats.length === 0 && !error && (
                    <p className="muted">No seats found for this event.</p>
                  )}

                  <p className="seat-help">
                    Select available seats. Prices appear in the summary.
                  </p>
                </div>

                <aside className="summary-panel">
                  <span className="eyebrow">YOUR BOOKING</span>
                  <h2>Booking summary</h2>

                  {!hold && selectedSeats.length === 0 && (
                    <p className="muted">
                      Select seats from the layout to get started.
                    </p>
                  )}

                  {selectedSeats.map((seat) => (
                    <div className="summary-line" key={seat.id}>
                      <span>
                        Row {seat.rowLabel}, Seat {seat.seatNumber}
                      </span>
                      <strong>{money(seat.price)}</strong>
                    </div>
                  ))}

                  {hold && (
                    <>
                      <div className="summary-line">
                        <span>Booking reference</span>
                        <strong className="reference-code">
                          {hold.bookingCode}
                        </strong>
                      </div>

                      <div className="summary-line">
                        <span>Hold expires</span>
                        <strong>
                          {hold.holdExpiresAt
                            ? new Date(hold.holdExpiresAt).toLocaleTimeString(
                                "en-IN",
                                { hour: "2-digit", minute: "2-digit" }
                              )
                            : "Within 5 minutes"}
                        </strong>
                      </div>
                    </>
                  )}

                  <div className="summary-total">
                    <span>Total</span>
                    <strong>
                      {money(hold ? hold.totalAmount : selectedTotal)}
                    </strong>
                  </div>

                  {!hold && (
                    <button
                      className="primary-button full-button"
                      disabled={selectedSeatIds.length === 0 || working}
                      onClick={createHold}
                      type="button"
                    >
                      {working ? "Please wait..." : "Hold selected seats"}
                    </button>
                  )}

                  {hold && (
                    <div className="payment-actions">
                      <p className="muted">
                        Simulated payment for testing.
                      </p>

                      <button
                        className="primary-button full-button"
                        disabled={working}
                        onClick={() => pay(true)}
                        type="button"
                      >
                        Pay successfully
                      </button>

                      <button
                        className="fail-button full-button"
                        disabled={working}
                        onClick={() => pay(false)}
                        type="button"
                      >
                        Simulate payment failure
                      </button>
                    </div>
                  )}

                  <p className="summary-note">
                    The backend confirms the hold before seats are reserved.
                  </p>
                </aside>
              </div>
            )}

            {ticket && (
              <div className="ticket-card">
                <div className="ticket-success">✓ PAYMENT CONFIRMED</div>
                <h2>Your Housefull ticket</h2>
                <p>{ticket.eventName}</p>
                <p>{ticket.venue}</p>
                <p>{formatDate(ticket.eventDateTime)}</p>

                <div className="ticket-seats">
                  {ticket.seats?.map((seat) => (
                    <span key={seat}>{seat}</span>
                  ))}
                </div>

                <h3>{money(ticket.totalAmount)}</h3>

                {ticket.qrCodeBase64 && (
                  <img
                    className="ticket-qr"
                    src={`data:image/png;base64,${ticket.qrCodeBase64}`}
                    alt="QR code for your Housefull ticket"
                  />
                )}

                <p className="muted">
                  Show this QR code to staff at the venue.
                </p>

                <p className="ticket-reference">
                  Booking: {ticket.bookingCode}
                </p>

                {ticket.checkedIn && (
                  <div className="notice success-notice">
                    This ticket has already been checked in.
                  </div>
                )}

                <button
                  className="primary-button"
                  onClick={() => openEvent(selectedEvent)}
                  type="button"
                >
                  Back to seat layout
                </button>
              </div>
            )}
          </section>
        )}
      </main>

      <footer className="footer">
        <button className="brand footer-brand" onClick={goHome} type="button">
          <span className="brand-icon">H</span>
          <span>housefull</span>
        </button>
        <p>Made for moments worth remembering.</p>
      </footer>
    </div>
  );
}

export default App;
