import { useState } from "react";

// Starting values for a new seat row.
const createEmptyRow = (rowLabel) => ({
  rowLabel,
  seatCount: "10",
  price: "500",
});

const API = "http://localhost:8080/api";

export default function EventCreateForm({ onCreated }) {
  // Event details entered by the organiser.
  const [name, setName] = useState("");
  const [venue, setVenue] = useState("");
  const [eventDateTime, setEventDateTime] = useState("");

  // Start with two rows so different ticket prices can be configured.
  const [rows, setRows] = useState([
    createEmptyRow("A"),
    createEmptyRow("B"),
  ]);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Change a field in only the row being edited.
  function updateRow(index, field, value) {
    setRows((currentRows) =>
      currentRows.map((row, rowIndex) =>
        rowIndex === index ? { ...row, [field]: value } : row
      )
    );
  }

  // Add the next unused letter as a default row label.
  function addRow() {
    if (rows.length >= 26) return;

    const usedLabels = new Set(rows.map((row) => row.rowLabel.trim().toUpperCase()));
    let nextLabel = "";
    for (let code = 65; code <= 90; code += 1) {
      const candidate = String.fromCharCode(code);
      if (!usedLabels.has(candidate)) {
        nextLabel = candidate;
        break;
      }
    }

    setRows((currentRows) => [...currentRows, createEmptyRow(nextLabel || "NEW")]);
  }

  // Remove a row while ensuring at least one row remains.
  function removeRow(index) {
    setRows((currentRows) =>
      currentRows.length > 1
        ? currentRows.filter((_, rowIndex) => rowIndex !== index)
        : currentRows
    );
  }

  const totalSeats = rows.reduce(
    (total, row) => total + (Number(row.seatCount) || 0),
    0
  );

  // Submit the form to the existing Spring Boot event-creation endpoint.
  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!name.trim() || !venue.trim() || !eventDateTime) {
      setError("Enter the event name, venue, and date/time.");
      return;
    }

    const normalizedRows = rows.map((row) => ({
      rowLabel: row.rowLabel.trim().toUpperCase(),
      seatCount: Number(row.seatCount),
      price: Number(row.price),
    }));

    if (
      normalizedRows.some(
        (row) =>
          !row.rowLabel ||
          !Number.isInteger(row.seatCount) ||
          row.seatCount < 1 ||
          row.seatCount > 500 ||
          !Number.isFinite(row.price) ||
          row.price <= 0
      )
    ) {
      setError("Each row needs a label, 1–500 seats, and a price greater than zero.");
      return;
    }

    const labels = normalizedRows.map((row) => row.rowLabel);
    if (new Set(labels).size !== labels.length) {
      setError("Each row must have a unique label.");
      return;
    }

    // Browser datetime-local already gives Java's local date-time format.
    const payload = {
      name: name.trim(),
      venue: venue.trim(),
      eventDateTime,
      rows: normalizedRows,
    };

    setSaving(true);
    try {
      const response = await fetch(`${API}/events`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      const responseText = await response.text();
      let result = null;
      try {
        result = responseText ? JSON.parse(responseText) : null;
      } catch {
        result = responseText;
      }

      if (!response.ok) {
        const detail =
          result?.message ||
          result?.error ||
          (typeof result === "string" ? result : "") ||
          `Request failed with status ${response.status}`;
        throw new Error(detail);
      }

      const createdName = result?.name || name.trim();
      setSuccess(`${createdName} was created with ${totalSeats} seats.`);
      setName("");
      setVenue("");
      setEventDateTime("");
      setRows([createEmptyRow("A"), createEmptyRow("B")]);

      if (onCreated) {
        await onCreated(result);
      }
    } catch (err) {
      setError(`Could not create event: ${err.message}`);
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="booking-section">
      <button
        className="back-button"
        type="button"
        onClick={onCreated ? undefined : () => window.history.back()}
      >
        Event management
      </button>

      <div className="booking-heading">
        <span className="eyebrow">EVENT MANAGEMENT</span>
        <h1>Create an event</h1>
        <p>Set event details, create seat rows, and assign prices to each row.</p>
      </div>

      {error && <div className="notice error-notice">{error}</div>}
      {success && <div className="notice success-notice">{success}</div>}

      <form className="event-create-form" onSubmit={handleSubmit}>
        <label className="dashboard-label" htmlFor="create-event-name">Event name</label>
        <input
          id="create-event-name"
          className="dashboard-input"
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder="e.g. Music Night"
          maxLength={150}
          required
        />

        <label className="dashboard-label" htmlFor="create-event-venue">Venue</label>
        <input
          id="create-event-venue"
          className="dashboard-input"
          value={venue}
          onChange={(event) => setVenue(event.target.value)}
          placeholder="e.g. Bengaluru Auditorium"
          maxLength={200}
          required
        />

        <label className="dashboard-label" htmlFor="create-event-date">Date and time</label>
        <input
          id="create-event-date"
          className="dashboard-input"
          type="datetime-local"
          value={eventDateTime}
          onChange={(event) => setEventDateTime(event.target.value)}
          required
        />

        <div className="event-rows-heading">
          <div>
            <h2>Seat layout and pricing</h2>
            <p className="muted">Each row creates numbered seats at the price you enter.</p>
          </div>
          <button
            className="secondary-button"
            type="button"
            onClick={addRow}
            disabled={saving || rows.length >= 26}
          >
            + Add row
          </button>
        </div>

        {rows.map((row, index) => (
          <div className="event-row-editor" key={`${index}-${row.rowLabel}`}>
            <div>
              <label className="dashboard-label" htmlFor={`create-row-label-${index}`}>Row label</label>
              <input
                id={`create-row-label-${index}`}
                className="dashboard-input"
                value={row.rowLabel}
                onChange={(event) => updateRow(index, "rowLabel", event.target.value)}
                maxLength={10}
                required
              />
            </div>
            <div>
              <label className="dashboard-label" htmlFor={`create-row-count-${index}`}>Seat count</label>
              <input
                id={`create-row-count-${index}`}
                className="dashboard-input"
                type="number"
                min="1"
                max="500"
                value={row.seatCount}
                onChange={(event) => updateRow(index, "seatCount", event.target.value)}
                required
              />
            </div>
            <div>
              <label className="dashboard-label" htmlFor={`create-row-price-${index}`}>Price per seat (₹)</label>
              <input
                id={`create-row-price-${index}`}
                className="dashboard-input"
                type="number"
                min="0.01"
                step="0.01"
                value={row.price}
                onChange={(event) => updateRow(index, "price", event.target.value)}
                required
              />
            </div>
            <button
              className="secondary-button remove-row-button"
              type="button"
              onClick={() => removeRow(index)}
              disabled={saving || rows.length <= 1}
            >
              Remove
            </button>
          </div>
        ))}

        <div className="event-create-summary">
          <span>Total seats</span>
          <strong>{totalSeats}</strong>
        </div>

        <button className="primary-button full-button" type="submit" disabled={saving}>
          {saving ? "Creating event..." : "Create event and generate seats"}
        </button>
      </form>
    </section>
  );
}
