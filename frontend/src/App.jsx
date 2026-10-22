




// import { useEffect, useState } from "react";
// import "./App.css";
// import EventCreateForm from "./EventCreateForm.jsx";

// const API = "http://localhost:8080/api";

// function App() {
//   // Customer booking state.
//   const [page, setPage] = useState("customer");
//   const [events, setEvents] = useState([]);
//   const [selectedEvent, setSelectedEvent] = useState(null);
//   const [seats, setSeats] = useState([]);
//   const [selectedSeatIds, setSelectedSeatIds] = useState([]);
//   const [hold, setHold] = useState(null);
//   const [ticket, setTicket] = useState(null);

//   // Organiser dashboard state.
//   const [dashboardEventId, setDashboardEventId] = useState("");
//   const [dashboard, setDashboard] = useState(null);
//   const [bookings, setBookings] = useState([]);

//   // Staff check-in state.
//   const [staffBookingCode, setStaffBookingCode] = useState("");
//   const [checkInResult, setCheckInResult] = useState(null);

//   // Shared loading and feedback state.
//   const [loading, setLoading] = useState(false);
//   const [working, setWorking] = useState(false);
//   const [error, setError] = useState("");
//   const [message, setMessage] = useState("");

//   // Load events once when the React application starts.
//   useEffect(() => {
//     loadEvents();
//   }, []);

//   // Send a request to Spring Boot and handle HTTP errors consistently.
//   async function apiRequest(path, options = {}) {
//     const response = await fetch(`${API}${path}`, {
//       ...options,
//       headers: {
//         ...(options.body
//           ? { "Content-Type": "application/json" }
//           : {}),
//         ...options.headers,
//       },
//     });

//     const text = await response.text();
//     let data = null;

//     try {
//       data = text ? JSON.parse(text) : null;
//     } catch {
//       data = text;
//     }

//     if (!response.ok) {
//       throw new Error(
//         data?.message ||
//           data?.error ||
//           (typeof data === "string" ? data : "") ||
//           `Request failed with status ${response.status}`
//       );
//     }

//     return data;
//   }

//   // Retrieve all events from the backend.
//   async function loadEvents() {
//     setLoading(true);
//     setError("");

//     try {
//       const data = await apiRequest("/events");
//       const eventList = Array.isArray(data) ? data : [];

//       setEvents(eventList);

//       if (eventList.length > 0) {
//         setDashboardEventId((current) =>
//           current || String(eventList[0].id)
//         );
//       }
//     } catch (err) {
//       setError(`Could not load events: ${err.message}`);
//     } finally {
//       setLoading(false);
//     }
//   }

//   // Open an event and retrieve its live seat availability.
//   async function openEvent(event) {
//     setPage("customer");
//     setSelectedEvent(event);
//     setSeats([]);
//     setSelectedSeatIds([]);
//     setHold(null);
//     setTicket(null);
//     setError("");
//     setMessage("");
//     setWorking(true);

//     try {
//       const data = await apiRequest(`/events/${event.id}/seats`);
//       setSeats(Array.isArray(data) ? data : []);
//     } catch (err) {
//       setError(`Could not load seats: ${err.message}`);
//     } finally {
//       setWorking(false);
//     }
//   }

//   // Return to the event listing.
//   function goHome() {
//     setPage("customer");
//     setSelectedEvent(null);
//     setSelectedSeatIds([]);
//     setHold(null);
//     setTicket(null);
//     setError("");
//     setMessage("");
//   }

//   // Format amounts using Indian rupees.
//   function money(amount) {
//     return new Intl.NumberFormat("en-IN", {
//       style: "currency",
//       currency: "INR",
//       maximumFractionDigits: 2,
//     }).format(Number(amount || 0));
//   }

//   // Format event dates and times for display.
//   function formatDate(value) {
//     if (!value) return "Date to be announced";

//     const date = new Date(value);

//     if (Number.isNaN(date.getTime())) {
//       return "Date to be announced";
//     }

//     return date.toLocaleString("en-IN", {
//       dateStyle: "medium",
//       timeStyle: "short",
//     });
//   }

//   // Toggle a seat only if the backend says it is available.
//   function toggleSeat(seat) {
//     if (seat.status !== "AVAILABLE" || hold || working) {
//       return;
//     }

//     setError("");

//     setSelectedSeatIds((current) => {
//       if (current.includes(seat.id)) {
//         return current.filter((id) => id !== seat.id);
//       }

//       if (current.length >= 20) {
//         setError("You can select a maximum of 20 seats.");
//         return current;
//       }

//       return [...current, seat.id];
//     });
//   }

//   // Calculate the selected total using backend-provided seat prices.
//   const selectedSeats = seats.filter((seat) =>
//     selectedSeatIds.includes(seat.id)
//   );

//   const selectedTotal = selectedSeats.reduce(
//     (sum, seat) => sum + Number(seat.price),
//     0
//   );

//   // Ask the backend to hold selected seats for five minutes.
//   async function createHold() {
//     if (!selectedEvent || selectedSeatIds.length === 0) {
//       setError("Please select at least one available seat.");
//       return;
//     }

//     setWorking(true);
//     setError("");
//     setMessage("");

//     try {
//       const result = await apiRequest("/bookings/hold", {
//         method: "POST",
//         body: JSON.stringify({
//           eventId: selectedEvent.id,
//           seatIds: selectedSeatIds,
//         }),
//       });

//       setHold(result);
//       setSelectedSeatIds([]);

//       // Refresh the layout to display the latest seat statuses.
//       const latestSeats = await apiRequest(
//         `/events/${selectedEvent.id}/seats`
//       );

//       setSeats(Array.isArray(latestSeats) ? latestSeats : []);
//       setMessage("Seats held successfully. Complete payment within 5 minutes.");
//     } catch (err) {
//       setError(`Could not hold seats: ${err.message}`);

//       // Another customer may have taken a seat, so refresh the layout.
//       try {
//         const latestSeats = await apiRequest(
//           `/events/${selectedEvent.id}/seats`
//         );
//         setSeats(Array.isArray(latestSeats) ? latestSeats : []);
//       } catch {
//         // Keep the original error if the refresh also fails.
//       }
//     } finally {
//       setWorking(false);
//     }
//   }

//   // Process the simulated payment and retrieve the ticket after success.
//   async function pay(success) {
//     if (!hold?.bookingCode) return;

//     setWorking(true);
//     setError("");
//     setMessage("");

//     try {
//       const result = await apiRequest(
//         `/bookings/${hold.bookingCode}/payment`,
//         {
//           method: "POST",
//           body: JSON.stringify({ success }),
//         }
//       );

//       if (success && result.status === "CONFIRMED") {
//         const ticketData = await apiRequest(
//           `/tickets/${hold.bookingCode}`
//         );

//         setTicket(ticketData);
//         setHold(null);
//         setMessage("Payment successful. Your ticket is ready.");
//       } else {
//         setHold(null);
//         setSelectedSeatIds([]);
//         setMessage(
//           result.message || "Payment failed. Your seats have been released."
//         );

//         const latestSeats = await apiRequest(
//           `/events/${selectedEvent.id}/seats`
//         );

//         setSeats(Array.isArray(latestSeats) ? latestSeats : []);
//       }
//     } catch (err) {
//       setError(`Payment request failed: ${err.message}`);
//     } finally {
//       setWorking(false);
//     }
//   }

//   // Determine the visual class for each seat.
//   function seatClass(seat) {
//     if (selectedSeatIds.includes(seat.id)) {
//       return "seat seat-selected";
//     }

//     return `seat seat-${seat.status.toLowerCase()}`;
//   }

//   // Load statistics and bookings for the selected organiser event.
//   async function openOrganiser(eventId = dashboardEventId) {
//     if (!eventId) {
//       setError("No event is available to display.");
//       return;
//     }

//     setPage("organiser");
//     setDashboardEventId(String(eventId));
//     setDashboard(null);
//     setBookings([]);
//     setError("");
//     setMessage("");
//     setWorking(true);

//     try {
//       const [stats, bookingData] = await Promise.all([
//         apiRequest(`/events/${eventId}/dashboard`),
//         apiRequest(`/bookings/events/${eventId}`),
//       ]);

//       setDashboard(stats);
//       setBookings(Array.isArray(bookingData) ? bookingData : []);
//     } catch (err) {
//       setError(`Could not load dashboard: ${err.message}`);
//     } finally {
//       setWorking(false);
//     }
//   }

//   // Open the staff screen.
//   function openStaff() {
//     setPage("staff");
//     setError("");
//     setMessage("");
//     setCheckInResult(null);
//   }

//   // Validate a booking reference through the backend check-in endpoint.
//   async function validateTicket(event) {
//     event.preventDefault();

//     const code = staffBookingCode.trim();

//     if (!code) {
//       setError("Enter a booking reference first.");
//       return;
//     }

//     setWorking(true);
//     setError("");
//     setMessage("");
//     setCheckInResult(null);

//     try {
//       const result = await apiRequest(
//         `/bookings/${encodeURIComponent(code)}/check-in`,
//         { method: "POST" }
//       );

//       setCheckInResult(result);
//     } catch (err) {
//       setError(`Ticket validation failed: ${err.message}`);
//     } finally {
//       setWorking(false);
//     }
//   }

//   // Build a seat list for the organiser table.
//   function bookingSeatNames(booking) {
//     if (!Array.isArray(booking.seats)) return "—";

//     return booking.seats
//       .map((seat) => {
//         if (typeof seat === "string") return seat;
//         return `${seat.rowLabel}${seat.seatNumber}`;
//       })
//       .join(", ");
//   }

//   // Group seats into rows, for example A, B, and C.
//   const seatRows = [...new Set(seats.map((seat) => seat.rowLabel))].sort();

//   return (
//     <div className="app">
//       <header className="navbar">
//         <button className="brand" onClick={goHome} type="button">
//           <span className="brand-icon">H</span>
//           <span>housefull</span>
//         </button>

//         <span className="nav-label">
//           Live events. Unforgettable moments.
//         </span>

//         <div className="nav-actions">
//           <button
//             className="nav-button"
//             onClick={goHome}
//             type="button"
//           >
//             All events
//           </button>

//           <button
//             className="nav-button"
//             onClick={() => openOrganiser()}
//             type="button"
//           >
//             Organiser
//           </button>

//           <button
//             className="nav-button"
//             onClick={() => {
//               setError("");
//               setMessage("");
//               setPage("create-event");
//             }}
//             type="button"
//           >
//             Create event
//           </button>

//           <button
//             className="nav-button"
//             onClick={openStaff}
//             type="button"
//           >
//             Staff check-in
//           </button>
//         </div>
//       </header>

//       <main>
//         {/* CUSTOMER EVENT LISTING */}
//         {page === "customer" && !selectedEvent && (
//           <>
//             <section className="hero">
//               <div className="hero-content">
//                 <span className="eyebrow">
//                   YOUR NEXT EXPERIENCE STARTS HERE
//                 </span>

//                 <h1>
//                   Make room for
//                   <br />
//                   <span>great moments.</span>
//                 </h1>

//                 <p>
//                   Discover live events, choose your seats, and book your next
//                   unforgettable experience.
//                 </p>

//                 <a className="primary-button" href="#events">
//                   Explore events <span>→</span>
//                 </a>
//               </div>

//               <div className="hero-art" aria-hidden="true">
//                 <div className="hero-circle circle-one" />
//                 <div className="hero-circle circle-two" />

//                 <div className="hero-ticket">
//                   <span className="ticket-small">YOUR NEXT EXPERIENCE</span>
//                   <span className="ticket-title">LIVE IT.</span>
//                   <span className="ticket-title ticket-title-accent">
//                     LOVE IT.
//                   </span>
//                   <span className="ticket-bottom">
//                     MUSIC · COMEDY · MORE
//                   </span>
//                 </div>

//                 <span className="floating-star star-one">✳</span>
//                 <span className="floating-star star-two">✦</span>
//               </div>
//             </section>

//             <section className="events-section" id="events">
//               <div className="section-heading">
//                 <div>
//                   <span className="eyebrow">FIND YOUR NEXT EXPERIENCE</span>
//                   <h2>Upcoming events</h2>
//                   <p>Pick an event. We'll help you get there.</p>
//                 </div>

//                 {!loading && !error && (
//                   <span className="event-count">
//                     {events.length} events
//                   </span>
//                 )}
//               </div>

//               {loading && (
//                 <div className="state-message">
//                   <div className="loader" />
//                   <p>Finding events for you...</p>
//                 </div>
//               )}

//               {error && (
//                 <div className="state-message error-state">
//                   <h3>Events could not be loaded</h3>
//                   <p>{error}</p>
//                   <button
//                     className="primary-button"
//                     onClick={loadEvents}
//                     type="button"
//                   >
//                     Try again
//                   </button>
//                 </div>
//               )}

//               {!loading && !error && events.length === 0 && (
//                 <div className="state-message">
//                   <h3>No events available yet</h3>
//                   <p>Check back soon for new experiences.</p>
//                 </div>
//               )}

//               {!loading && !error && events.length > 0 && (
//                 <div className="events-grid">
//                   {events.map((event, index) => (
//                     <article className="event-card" key={event.id}>
//                       <div className={`event-art event-art-${index % 3}`}>
//                         <span className="event-category">
//                           {event.name?.toLowerCase().includes("music")
//                             ? "MUSIC"
//                             : event.name?.toLowerCase().includes("comedy")
//                               ? "COMEDY"
//                               : "LIVE EVENT"}
//                         </span>

//                         <span className="event-art-symbol" aria-hidden="true">
//                           {event.name?.toLowerCase().includes("music")
//                             ? "♫"
//                             : event.name?.toLowerCase().includes("comedy")
//                               ? "☻"
//                               : "✳"}
//                         </span>

//                         <span className="event-art-caption">
//                           MAKE IT A MOMENT
//                         </span>
//                       </div>

//                       <div className="event-details">
//                         <h3>{event.name}</h3>

//                         <p className="event-info">
//                           <span>⌖</span>
//                           {event.venue || "Venue to be announced"}
//                         </p>

//                         <p className="event-info">
//                           <span>◷</span>
//                           {formatDate(event.eventDateTime)}
//                         </p>

//                         <div className="event-card-footer">
//                           <span className="seat-info">
//                             {event.totalSeats ?? "—"} seats
//                           </span>

//                           <button
//                             className="secondary-button"
//                             onClick={() => openEvent(event)}
//                             type="button"
//                           >
//                             View seats →
//                           </button>
//                         </div>
//                       </div>
//                     </article>
//                   ))}
//                 </div>
//               )}
//             </section>
//           </>
//         )}

//         {/* CUSTOMER SEAT SELECTION AND CHECKOUT */}
//         {page === "customer" && selectedEvent && (
//           <section className="booking-section">
//             <button className="back-button" onClick={goHome} type="button">
//               ← Back to events
//             </button>

//             <div className="booking-heading">
//               <span className="eyebrow">CHOOSE YOUR EXPERIENCE</span>
//               <h1>{selectedEvent.name}</h1>
//               <p>
//                 {selectedEvent.venue} ·{" "}
//                 {formatDate(selectedEvent.eventDateTime)}
//               </p>
//             </div>

//             {error && <div className="notice error-notice">{error}</div>}
//             {message && (
//               <div className="notice success-notice">{message}</div>
//             )}

//             {working && (
//               <div className="notice">Processing your request...</div>
//             )}

//             {!ticket && (
//               <div className="booking-layout">
//                 <div className="seat-panel">
//                   <div className="screen">STAGE / SCREEN</div>

//                   <div className="seat-legend">
//                     <span><i className="legend-dot available-dot" /> Available</span>
//                     <span><i className="legend-dot selected-dot" /> Selected</span>
//                     <span><i className="legend-dot held-dot" /> Held</span>
//                     <span><i className="legend-dot booked-dot" /> Booked</span>
//                   </div>

//                   {seatRows.map((row) => (
//                     <div className="seat-row" key={row}>
//                       <span className="row-label">{row}</span>

//                       <div className="seat-row-items">
//                         {seats
//                           .filter((seat) => seat.rowLabel === row)
//                           .sort((a, b) => a.seatNumber - b.seatNumber)
//                           .map((seat) => (
//                             <button
//                               key={seat.id}
//                               className={seatClass(seat)}
//                               disabled={
//                                 seat.status !== "AVAILABLE" ||
//                                 Boolean(hold) ||
//                                 working
//                               }
//                               onClick={() => toggleSeat(seat)}
//                               title={`${row}${seat.seatNumber} · ${money(seat.price)} · ${seat.status}`}
//                               type="button"
//                             >
//                               {seat.seatNumber}
//                             </button>
//                           ))}
//                       </div>
//                     </div>
//                   ))}

//                   {!working && seats.length === 0 && !error && (
//                     <p className="muted">No seats found for this event.</p>
//                   )}

//                   <p className="seat-help">
//                     Select available seats. Prices appear in the summary.
//                   </p>
//                 </div>

//                 <aside className="summary-panel">
//                   <span className="eyebrow">YOUR BOOKING</span>
//                   <h2>Booking summary</h2>

//                   {!hold && selectedSeats.length === 0 && (
//                     <p className="muted">
//                       Select seats from the layout to get started.
//                     </p>
//                   )}

//                   {selectedSeats.map((seat) => (
//                     <div className="summary-line" key={seat.id}>
//                       <span>
//                         Row {seat.rowLabel}, Seat {seat.seatNumber}
//                       </span>
//                       <strong>{money(seat.price)}</strong>
//                     </div>
//                   ))}

//                   {hold && (
//                     <>
//                       <div className="summary-line">
//                         <span>Booking reference</span>
//                         <strong className="reference-code">
//                           {hold.bookingCode}
//                         </strong>
//                       </div>

//                       <div className="summary-line">
//                         <span>Hold expires</span>
//                         <strong>
//                           {hold.holdExpiresAt
//                             ? new Date(hold.holdExpiresAt).toLocaleTimeString(
//                                 "en-IN",
//                                 { hour: "2-digit", minute: "2-digit" }
//                               )
//                             : "Within 5 minutes"}
//                         </strong>
//                       </div>
//                     </>
//                   )}

//                   <div className="summary-total">
//                     <span>Total</span>
//                     <strong>
//                       {money(hold ? hold.totalAmount : selectedTotal)}
//                     </strong>
//                   </div>

//                   {!hold && (
//                     <button
//                       className="primary-button full-button"
//                       disabled={selectedSeatIds.length === 0 || working}
//                       onClick={createHold}
//                       type="button"
//                     >
//                       {working ? "Please wait..." : "Hold selected seats"}
//                     </button>
//                   )}

//                   {hold && (
//                     <div className="payment-actions">
//                       <p className="muted">Simulated payment for testing.</p>

//                       <button
//                         className="primary-button full-button"
//                         disabled={working}
//                         onClick={() => pay(true)}
//                         type="button"
//                       >
//                         Pay successfully
//                       </button>

//                       <button
//                         className="fail-button full-button"
//                         disabled={working}
//                         onClick={() => pay(false)}
//                         type="button"
//                       >
//                         Simulate payment failure
//                       </button>
//                     </div>
//                   )}

//                   <p className="summary-note">
//                     The backend confirms the hold before seats are reserved.
//                   </p>
//                 </aside>
//               </div>
//             )}

//             {ticket && (
//               <div className="ticket-card">
//                 <div className="ticket-success">✓ PAYMENT CONFIRMED</div>
//                 <h2>Your Housefull ticket</h2>
//                 <p>{ticket.eventName}</p>
//                 <p>{ticket.venue}</p>
//                 <p>{formatDate(ticket.eventDateTime)}</p>

//                 <div className="ticket-seats">
//                   {ticket.seats?.map((seat) => (
//                     <span key={seat}>{seat}</span>
//                   ))}
//                 </div>

//                 <h3>{money(ticket.totalAmount)}</h3>

//                 {ticket.qrCodeBase64 && (
//                   <img
//                     className="ticket-qr"
//                     src={`data:image/png;base64,${ticket.qrCodeBase64}`}
//                     alt="QR code for your Housefull ticket"
//                   />
//                 )}

//                 <p className="muted">
//                   Show this QR code to staff at the venue.
//                 </p>

//                 <p className="ticket-reference">
//                   Booking: {ticket.bookingCode}
//                 </p>

//                 <button
//                   className="primary-button"
//                   onClick={() => openEvent(selectedEvent)}
//                   type="button"
//                 >
//                   Back to seat layout
//                 </button>
//               </div>
//             )}
//           </section>
//         )}

//         {/* ORGANISER DASHBOARD */}
//         {page === "organiser" && (
//           <section className="booking-section">
//             <div className="booking-heading">
//               <span className="eyebrow">EVENT MANAGEMENT</span>
//               <h1>Organiser dashboard</h1>
//               <p>Monitor bookings, seat availability, revenue, and entry.</p>
//             </div>

//             <label className="dashboard-label" htmlFor="dashboard-event">
//               Select event
//             </label>

//             <select
//               id="dashboard-event"
//               className="dashboard-select"
//               value={dashboardEventId}
//               onChange={(event) => openOrganiser(event.target.value)}
//             >
//               {events.map((event) => (
//                 <option key={event.id} value={event.id}>
//                   {event.name} — #{event.id}
//                 </option>
//               ))}
//             </select>

//             <button
//               className="secondary-button"
//               disabled={working}
//               onClick={() => openOrganiser()}
//               type="button"
//             >
//               Refresh dashboard
//             </button>

//             {error && <div className="notice error-notice">{error}</div>}
//             {working && <div className="notice">Loading dashboard...</div>}

//             {dashboard && (
//               <>
//                 <div className="dashboard-grid">
//                   <article className="stat-card">
//                     <span>Total seats</span>
//                     <strong>{dashboard.totalSeats}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Seats sold</span>
//                     <strong>{dashboard.seatsSold}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Seats held</span>
//                     <strong>{dashboard.seatsHeld}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Seats available</span>
//                     <strong>{dashboard.seatsAvailable}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Total bookings</span>
//                     <strong>{dashboard.totalBookings}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Confirmed bookings</span>
//                     <strong>{dashboard.confirmedBookings}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Failed bookings</span>
//                     <strong>{dashboard.failedBookings}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Cancelled bookings</span>
//                     <strong>{dashboard.cancelledBookings}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Expired bookings</span>
//                     <strong>{dashboard.expiredBookings}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Pending bookings</span>
//                     <strong>{dashboard.pendingBookings}</strong>
//                   </article>

//                   <article className="stat-card">
//                     <span>Revenue collected</span>
//                     <strong>{money(dashboard.revenueCollected)}</strong>
//                   </article>
//                 </div>

//                 <p className="summary-note">
//                   Revenue is displayed as reported by your backend dashboard.
//                   Verify whether cancelled bookings are deducted before using
//                   this figure for financial reporting.
//                 </p>

//                 <h2 className="dashboard-subtitle">Recent bookings</h2>

//                 <div className="booking-table-wrap">
//                   <table className="booking-table">
//                     <thead>
//                       <tr>
//                         <th>Booking reference</th>
//                         <th>Seats</th>
//                         <th>Status</th>
//                         <th>Amount</th>
//                       </tr>
//                     </thead>

//                     <tbody>
//                       {bookings.map((booking) => (
//                         <tr key={booking.bookingCode}>
//                           <td>{booking.bookingCode}</td>
//                           <td>{bookingSeatNames(booking)}</td>
//                           <td>{booking.status}</td>
//                           <td>{money(booking.totalAmount)}</td>
//                         </tr>
//                       ))}

//                       {bookings.length === 0 && (
//                         <tr>
//                           <td colSpan="4">No bookings found for this event.</td>
//                         </tr>
//                       )}
//                     </tbody>
//                   </table>
//                 </div>
//               </>
//             )}
//           </section>
//         )}

//         {/* ORGANISER EVENT CREATION */}
//         {page === "create-event" && (
//           <EventCreateForm
//             onCreated={async () => {
//               // Refresh the event list after the backend creates the event.
//               await loadEvents();
//               setSelectedEvent(null);
//               setPage("customer");
//               setError("");
//               setMessage("Event created successfully. It is now available in the event list.");
//             }}
//           />
//         )}

//         {/* STAFF CHECK-IN */}
//         {page === "staff" && (
//           <section className="booking-section">
//             <div className="booking-heading">
//               <span className="eyebrow">VENUE ENTRY</span>
//               <h1>Staff check-in</h1>
//               <p>Enter a booking reference to validate the ticket.</p>
//             </div>

//             <div className="summary-panel staff-panel">
//               <form onSubmit={validateTicket}>
//                 <label
//                   className="dashboard-label"
//                   htmlFor="staff-booking-code"
//                 >
//                   Booking reference
//                 </label>

//                 <input
//                   id="staff-booking-code"
//                   className="dashboard-input"
//                   value={staffBookingCode}
//                   onChange={(event) =>
//                     setStaffBookingCode(event.target.value)
//                   }
//                   placeholder="Paste the booking reference"
//                 />

//                 <button
//                   className="primary-button full-button"
//                   type="submit"
//                   disabled={working}
//                 >
//                   {working ? "Validating..." : "Validate ticket and check in"}
//                 </button>
//               </form>

//               {error && <div className="notice error-notice">{error}</div>}

//               {checkInResult && (
//                 <div className="checkin-result">
//                   <h2>{checkInResult.message}</h2>
//                   <p>Event: {checkInResult.eventName}</p>
//                   <p>Booking: {checkInResult.bookingCode}</p>
//                   <p>Status: {checkInResult.status}</p>
//                   <p>
//                     Checked in: {checkInResult.checkedIn ? "Yes" : "No"}
//                   </p>
//                   {checkInResult.checkedInAt && (
//                     <p>Time: {formatDate(checkInResult.checkedInAt)}</p>
//                   )}
//                 </div>
//               )}
//             </div>
//           </section>
//         )}
//       </main>

//       <footer className="footer">
//         <button className="brand footer-brand" onClick={goHome} type="button">
//           <span className="brand-icon">H</span>
//           <span>housefull</span>
//         </button>
//         <p>Made for moments worth remembering.</p>
//       </footer>
//     </div>
//   );
// }

// export default App;











import { useEffect, useState } from "react";
import "./App.css";
import EventCreateForm from "./EventCreateForm.jsx";

const API = "http://localhost:8080/api";

function App() {
  // Customer booking state.
  const [page, setPage] = useState("customer");
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState([]);
  const [hold, setHold] = useState(null);
  const [ticket, setTicket] = useState(null);

  // Organiser dashboard state.
  const [dashboardEventId, setDashboardEventId] = useState("");
  const [dashboard, setDashboard] = useState(null);
  const [bookings, setBookings] = useState([]);

  // Staff check-in state.
  const [staffBookingCode, setStaffBookingCode] = useState("");
  const [checkInResult, setCheckInResult] = useState(null);

  // Shared loading and feedback state.
  const [loading, setLoading] = useState(false);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  // Load events once when the React application starts.
  useEffect(() => {
    loadEvents();
  }, []);

  // Send a request to Spring Boot and handle HTTP errors consistently.
  async function apiRequest(path, options = {}) {
    const response = await fetch(`${API}${path}`, {
      ...options,
      headers: {
        ...(options.body
          ? { "Content-Type": "application/json" }
          : {}),
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

  // Retrieve all events from the backend.
  async function loadEvents() {
    setLoading(true);
    setError("");

    try {
      const data = await apiRequest("/events");
      const eventList = Array.isArray(data) ? data : [];

      setEvents(eventList);

      if (eventList.length > 0) {
        setDashboardEventId((current) =>
          current || String(eventList[0].id)
        );
      }
    } catch (err) {
      setError(`Could not load events: ${err.message}`);
    } finally {
      setLoading(false);
    }
  }

  // Open an event and retrieve its live seat availability.
  async function openEvent(event) {
    setPage("customer");
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

  // Return to the event listing.
  function goHome() {
    setPage("customer");
    setSelectedEvent(null);
    setSelectedSeatIds([]);
    setHold(null);
    setTicket(null);
    setError("");
    setMessage("");
  }

  // Format amounts using Indian rupees.
  function money(amount) {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(Number(amount || 0));
  }

  // Format event dates and times for display.
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

  // Toggle a seat only if the backend says it is available.
  function toggleSeat(seat) {
    if (seat.status !== "AVAILABLE" || hold || working) {
      return;
    }

    setError("");

    setSelectedSeatIds((current) => {
      if (current.includes(seat.id)) {
        return current.filter((id) => id !== seat.id);
      }

      if (current.length >= 20) {
        setError("You can select a maximum of 20 seats.");
        return current;
      }

      return [...current, seat.id];
    });
  }

  // Calculate the selected total using backend-provided seat prices.
  const selectedSeats = seats.filter((seat) =>
    selectedSeatIds.includes(seat.id)
  );

  const selectedTotal = selectedSeats.reduce(
    (sum, seat) => sum + Number(seat.price),
    0
  );

  // Ask the backend to hold selected seats for five minutes.
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

      // Refresh the layout to display the latest seat statuses.
      const latestSeats = await apiRequest(
        `/events/${selectedEvent.id}/seats`
      );

      setSeats(Array.isArray(latestSeats) ? latestSeats : []);
      setMessage("Seats held successfully. Complete payment within 5 minutes.");
    } catch (err) {
      setError(`Could not hold seats: ${err.message}`);

      // Another customer may have taken a seat, so refresh the layout.
      try {
        const latestSeats = await apiRequest(
          `/events/${selectedEvent.id}/seats`
        );
        setSeats(Array.isArray(latestSeats) ? latestSeats : []);
      } catch {
        // Keep the original error if the refresh also fails.
      }
    } finally {
      setWorking(false);
    }
  }

  // Process the simulated payment and retrieve the ticket after success.
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

        const latestSeats = await apiRequest(
          `/events/${selectedEvent.id}/seats`
        );

        setSeats(Array.isArray(latestSeats) ? latestSeats : []);
      }
    } catch (err) {
      setError(`Payment request failed: ${err.message}`);
    } finally {
      setWorking(false);
    }
  }

  // Determine the visual class for each seat.
  function seatClass(seat) {
    if (selectedSeatIds.includes(seat.id)) {
      return "seat seat-selected";
    }

    return `seat seat-${seat.status.toLowerCase()}`;
  }

  // Load statistics and bookings for the selected organiser event.
  async function openOrganiser(eventId = dashboardEventId) {
    if (!eventId) {
      setError("No event is available to display.");
      return;
    }

    setPage("organiser");
    setDashboardEventId(String(eventId));
    setDashboard(null);
    setBookings([]);
    setError("");
    setMessage("");
    setWorking(true);

    try {
      const [stats, bookingData] = await Promise.all([
        apiRequest(`/events/${eventId}/dashboard`),
        apiRequest(`/bookings/events/${eventId}`),
      ]);

      setDashboard(stats);
      setBookings(Array.isArray(bookingData) ? bookingData : []);
    } catch (err) {
      setError(`Could not load dashboard: ${err.message}`);
    } finally {
      setWorking(false);
    }
  }

  // Cancel a confirmed booking from the organiser dashboard.
  async function cancelBooking(booking) {
    // Only confirmed bookings can be cancelled from this screen.
    if (booking.status !== "CONFIRMED") {
      setError("Only confirmed bookings can be cancelled.");
      return;
    }

    // Ask for confirmation before making a change that releases seats.
    const confirmed = window.confirm(
      `Cancel this booking?\n\n` +
        `Reference: ${booking.bookingCode}\n` +
        `Seats: ${bookingSeatNames(booking)}\n` +
        `Simulated refund: ${money(booking.totalAmount)}`
    );

    if (!confirmed) return;

    setWorking(true);
    setError("");
    setMessage("");

    try {
      // Call the cancellation endpoint in the Spring Boot backend.
      const result = await apiRequest(
        `/bookings/${encodeURIComponent(booking.bookingCode)}/cancel`,
        { method: "POST" }
      );

      // Show the refund amount returned by the backend.
      const refundAmount = result?.refundAmount ?? booking.totalAmount;
      setMessage(
        `Booking cancelled successfully. Simulated refund: ${money(refundAmount)}.`
      );

      // Reload statistics and bookings so seats, counts, and revenue update.
      const [stats, bookingData] = await Promise.all([
        apiRequest(`/events/${dashboardEventId}/dashboard`),
        apiRequest(`/bookings/events/${dashboardEventId}`),
      ]);

      setDashboard(stats);
      setBookings(Array.isArray(bookingData) ? bookingData : []);
    } catch (err) {
      setError(`Could not cancel booking: ${err.message}`);
    } finally {
      setWorking(false);
    }
  }

  // Open the staff screen.
  function openStaff() {
    setPage("staff");
    setError("");
    setMessage("");
    setCheckInResult(null);
  }

  // Validate a booking reference through the backend check-in endpoint.
  async function validateTicket(event) {
    event.preventDefault();

    const code = staffBookingCode.trim();

    if (!code) {
      setError("Enter a booking reference first.");
      return;
    }

    setWorking(true);
    setError("");
    setMessage("");
    setCheckInResult(null);

    try {
      const result = await apiRequest(
        `/bookings/${encodeURIComponent(code)}/check-in`,
        { method: "POST" }
      );

      setCheckInResult(result);
    } catch (err) {
      setError(`Ticket validation failed: ${err.message}`);
    } finally {
      setWorking(false);
    }
  }

  // Build a seat list for the organiser table.
  function bookingSeatNames(booking) {
    if (!Array.isArray(booking.seats)) return "—";

    return booking.seats
      .map((seat) => {
        if (typeof seat === "string") return seat;
        return `${seat.rowLabel}${seat.seatNumber}`;
      })
      .join(", ");
  }

  // Group seats into rows, for example A, B, and C.
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

        <div className="nav-actions">
          <button
            className="nav-button"
            onClick={goHome}
            type="button"
          >
            All events
          </button>

          <button
            className="nav-button"
            onClick={() => openOrganiser()}
            type="button"
          >
            Organiser
          </button>

          <button
            className="nav-button"
            onClick={() => {
              setError("");
              setMessage("");
              setPage("create-event");
            }}
            type="button"
          >
            Create event
          </button>

          <button
            className="nav-button"
            onClick={openStaff}
            type="button"
          >
            Staff check-in
          </button>
        </div>
      </header>

      <main>
        {/* CUSTOMER EVENT LISTING */}
        {page === "customer" && !selectedEvent && (
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
                    {events.length} events
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
        )}

        {/* CUSTOMER SEAT SELECTION AND CHECKOUT */}
        {page === "customer" && selectedEvent && (
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
                              className={seatClass(seat)}
                              disabled={
                                seat.status !== "AVAILABLE" ||
                                Boolean(hold) ||
                                working
                              }
                              onClick={() => toggleSeat(seat)}
                              title={`${row}${seat.seatNumber} · ${money(seat.price)} · ${seat.status}`}
                              type="button"
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
                      <p className="muted">Simulated payment for testing.</p>

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

        {/* ORGANISER DASHBOARD */}
        {page === "organiser" && (
          <section className="booking-section">
            <div className="booking-heading">
              <span className="eyebrow">EVENT MANAGEMENT</span>
              <h1>Organiser dashboard</h1>
              <p>Monitor bookings, seat availability, revenue, and entry.</p>
            </div>

            <label className="dashboard-label" htmlFor="dashboard-event">
              Select event
            </label>

            <select
              id="dashboard-event"
              className="dashboard-select"
              value={dashboardEventId}
              onChange={(event) => openOrganiser(event.target.value)}
            >
              {events.map((event) => (
                <option key={event.id} value={event.id}>
                  {event.name} — #{event.id}
                </option>
              ))}
            </select>

            <button
              className="secondary-button"
              disabled={working}
              onClick={() => openOrganiser()}
              type="button"
            >
              Refresh dashboard
            </button>

            {error && <div className="notice error-notice">{error}</div>}
            {working && <div className="notice">Loading dashboard...</div>}

            {dashboard && (
              <>
                <div className="dashboard-grid">
                  <article className="stat-card">
                    <span>Total seats</span>
                    <strong>{dashboard.totalSeats}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Seats sold</span>
                    <strong>{dashboard.seatsSold}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Seats held</span>
                    <strong>{dashboard.seatsHeld}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Seats available</span>
                    <strong>{dashboard.seatsAvailable}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Total bookings</span>
                    <strong>{dashboard.totalBookings}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Confirmed bookings</span>
                    <strong>{dashboard.confirmedBookings}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Failed bookings</span>
                    <strong>{dashboard.failedBookings}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Cancelled bookings</span>
                    <strong>{dashboard.cancelledBookings}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Expired bookings</span>
                    <strong>{dashboard.expiredBookings}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Pending bookings</span>
                    <strong>{dashboard.pendingBookings}</strong>
                  </article>

                  <article className="stat-card">
                    <span>Revenue collected</span>
                    <strong>{money(dashboard.revenueCollected)}</strong>
                  </article>
                </div>

                <p className="summary-note">
                  Revenue is displayed as reported by your backend dashboard.
                  Verify whether cancelled bookings are deducted before using
                  this figure for financial reporting.
                </p>

                <h2 className="dashboard-subtitle">Recent bookings</h2>

                {message && <div className="notice success-notice">{message}</div>}
                {error && <div className="notice error-notice">{error}</div>}

                <div className="booking-table-wrap">
                  <table className="booking-table">
                    <thead>
                      <tr>
                        <th>Booking reference</th>
                        <th>Seats</th>
                        <th>Status</th>
                        <th>Amount</th>
                        <th>Action</th>
                      </tr>
                    </thead>

                    <tbody>
                      {bookings.map((booking) => (
                        <tr key={booking.bookingCode}>
                          <td>{booking.bookingCode}</td>
                          <td>{bookingSeatNames(booking)}</td>
                          <td>{booking.status}</td>
                          <td>{money(booking.totalAmount)}</td>
                          <td>
                            {booking.status === "CONFIRMED" ? (
                              <button
                                className="fail-button"
                                type="button"
                                disabled={working}
                                onClick={() => cancelBooking(booking)}
                              >
                                {working ? "Please wait..." : "Cancel booking"}
                              </button>
                            ) : (
                              <span className="muted">—</span>
                            )}
                          </td>
                        </tr>
                      ))}

                      {bookings.length === 0 && (
                        <tr>
                          <td colSpan="5">No bookings found for this event.</td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </section>
        )}

        {/* ORGANISER EVENT CREATION */}
        {page === "create-event" && (
          <EventCreateForm
            onCreated={async () => {
              // Refresh the event list after the backend creates the event.
              await loadEvents();
              setSelectedEvent(null);
              setPage("customer");
              setError("");
              setMessage("Event created successfully. It is now available in the event list.");
            }}
          />
        )}

        {/* STAFF CHECK-IN */}
        {page === "staff" && (
          <section className="booking-section">
            <div className="booking-heading">
              <span className="eyebrow">VENUE ENTRY</span>
              <h1>Staff check-in</h1>
              <p>Enter a booking reference to validate the ticket.</p>
            </div>

            <div className="summary-panel staff-panel">
              <form onSubmit={validateTicket}>
                <label
                  className="dashboard-label"
                  htmlFor="staff-booking-code"
                >
                  Booking reference
                </label>

                <input
                  id="staff-booking-code"
                  className="dashboard-input"
                  value={staffBookingCode}
                  onChange={(event) =>
                    setStaffBookingCode(event.target.value)
                  }
                  placeholder="Paste the booking reference"
                />

                <button
                  className="primary-button full-button"
                  type="submit"
                  disabled={working}
                >
                  {working ? "Validating..." : "Validate ticket and check in"}
                </button>
              </form>

              {error && <div className="notice error-notice">{error}</div>}

              {checkInResult && (
                <div className="checkin-result">
                  <h2>{checkInResult.message}</h2>
                  <p>Event: {checkInResult.eventName}</p>
                  <p>Booking: {checkInResult.bookingCode}</p>
                  <p>Status: {checkInResult.status}</p>
                  <p>
                    Checked in: {checkInResult.checkedIn ? "Yes" : "No"}
                  </p>
                  {checkInResult.checkedInAt && (
                    <p>Time: {formatDate(checkInResult.checkedInAt)}</p>
                  )}
                </div>
              )}
            </div>
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
