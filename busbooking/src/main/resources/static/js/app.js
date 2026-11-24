/* ==========================================================================
   Bus Travel Booking Management System - frontend logic
   Vanilla JavaScript + Fetch API. All data lives in MySQL; this file only
   calls the Spring Boot REST API and renders what comes back.
   ========================================================================== */
"use strict";

const API_BASE = "/api/v1/bus-bookings";
const FORM_FIELDS = ["passengerName", "busNumber", "source", "destination", "travelDate", "seatNumber", "ticketFare"];

const state = {
  rows: [],                 // bookings currently shown in the table
  view: { url: null, label: "" }, // url === null means "all bookings"; otherwise a report URL
  editing: null,            // booking being edited, or null when adding
  viewedId: null,           // booking shown in the View modal
  cancelId: null,           // booking waiting for cancel confirmation
  saving: false
};

let bookingModal, viewModal, cancelModal;

const $ = id => document.getElementById(id);

/* ------------------------------------------------------------------ helpers */

function esc(value) {
  return String(value ?? "").replace(/[&<>"']/g, ch => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[ch]
  ));
}

/** Today as yyyy-MM-dd in the browser's local time zone (toISOString would use UTC). */
function todayISO() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

function formatDate(iso) {
  if (!iso) return "";
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d).toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
}

const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", minimumFractionDigits: 2 });
const inrWhole = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 });
const formatINR = value => inr.format(Number(value));

function showToast(message, type = "success") {
  const el = document.createElement("div");
  el.className = `toast align-items-center text-bg-${type} border-0`;
  el.setAttribute("role", type === "danger" ? "alert" : "status");
  el.setAttribute("aria-atomic", "true");
  el.innerHTML = '<div class="d-flex"><div class="toast-body"></div>' +
    '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button></div>';
  el.querySelector(".toast-body").textContent = message;
  $("toastArea").appendChild(el);
  el.addEventListener("hidden.bs.toast", () => el.remove());
  new bootstrap.Toast(el, { delay: type === "danger" ? 7000 : 3500 }).show();
}

/* --------------------------------------------------------------- API layer */

class ApiError extends Error {
  constructor(status, message, fieldErrors = {}) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

/**
 * Single place for every fetch() call.
 *  - HTTP errors (400, 404, 500...) are turned into ApiError with the server's message.
 *  - 204 No Content (DELETE) returns null instead of trying to parse an empty body.
 */
async function api(url, options = {}) {
  const headers = { Accept: "application/json" };
  if (options.body) headers["Content-Type"] = "application/json";

  let response;
  try {
    response = await fetch(url, { ...options, headers });
  } catch (networkError) {
    throw new ApiError(0, "Cannot reach the server. Check that the Spring Boot application is running on port 8080.");
  }

  if (!response.ok) {
    let body = null;
    try { body = await response.json(); } catch (_) { /* error response had no JSON body */ }
    throw new ApiError(
      response.status,
      (body && body.message) || `Request failed with status ${response.status}.`,
      (body && body.errors) || {}
    );
  }

  if (response.status === 204) return null;
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

/* --------------------------------------------------- loading and rendering */

/** Reloads everything from the database: statistics first, then the table (all rows or the active report). */
async function refresh() {
  try {
    const all = await api(API_BASE);
    updateStats(all);
    const rows = state.view.url ? await api(state.view.url) : all;
    showRows(rows);
  } catch (err) {
    showLoadError(err);
  }
}

function showRows(rows) {
  state.rows = rows;
  renderTable(rows);
  updateFilterBar();
}

function renderTable(rows) {
  const body = $("bookingTableBody");
  $("resultCount").textContent = `${rows.length} ${rows.length === 1 ? "booking" : "bookings"}`;

  if (!rows.length) {
    const filtered = Boolean(state.view.url);
    body.innerHTML = `<tr><td colspan="9" class="empty-state">
        <i class="bi bi-ticket-perforated" aria-hidden="true"></i>
        ${filtered ? "No bookings match this search. Check the spelling or try another value."
                   : "No bookings yet. Add the first booking to see it here."}
        ${filtered ? "" : '<div class="mt-3"><button type="button" class="btn btn-primary btn-sm" data-action="add-booking">Add booking</button></div>'}
      </td></tr>`;
    return;
  }

  body.innerHTML = rows.map(b => `
    <tr>
      <td><span class="id-chip">#${esc(b.bookingId)}</span></td>
      <td class="fw-semibold">${esc(b.passengerName)}</td>
      <td>${esc(b.busNumber)}</td>
      <td>${esc(b.source)}</td>
      <td>${esc(b.destination)}</td>
      <td class="text-nowrap">${esc(formatDate(b.travelDate))}</td>
      <td><span class="seat">${esc(b.seatNumber)}</span></td>
      <td class="fare">${esc(formatINR(b.ticketFare))}</td>
      <td class="actions">
        <button type="button" class="btn btn-outline-secondary btn-icon" data-action="view" data-id="${esc(b.bookingId)}"
                title="View booking" aria-label="View booking ${esc(b.bookingId)}"><i class="bi bi-eye" aria-hidden="true"></i></button>
        <button type="button" class="btn btn-outline-primary btn-icon" data-action="edit" data-id="${esc(b.bookingId)}"
                title="Edit booking" aria-label="Edit booking ${esc(b.bookingId)}"><i class="bi bi-pencil-square" aria-hidden="true"></i></button>
        <button type="button" class="btn btn-outline-danger btn-icon" data-action="cancel" data-id="${esc(b.bookingId)}"
                title="Cancel booking" aria-label="Cancel booking ${esc(b.bookingId)}"><i class="bi bi-trash3" aria-hidden="true"></i></button>
      </td>
    </tr>`).join("");
}

function showLoadError(err) {
  $("resultCount").textContent = "0 bookings";
  $("bookingTableBody").innerHTML = `<tr><td colspan="9" class="empty-state">
      <i class="bi bi-exclamation-triangle-fill text-danger" aria-hidden="true"></i>
      <div class="text-danger fw-semibold">${esc(err.message)}</div>
      <div class="mt-3"><button type="button" class="btn btn-outline-secondary btn-sm" data-action="retry">Try again</button></div>
    </td></tr>`;
}

function updateFilterBar() {
  const bar = $("filterBar");
  if (!state.view.url) { bar.hidden = true; return; }
  $("filterLabel").textContent = state.view.label;
  bar.hidden = false;
}

/* ---------------------------------------------------------------- dashboard */

function updateStats(all) {
  const revenue = all.reduce((sum, b) => sum + Number(b.ticketFare), 0);
  const buses = new Set(all.map(b => b.busNumber)).size;
  const today = todayISO();
  const travellingToday = all.filter(b => b.travelDate === today).length;

  animateNumber($("statTotal"), all.length, n => String(Math.round(n)));
  animateNumber($("statRevenue"), revenue, n => inrWhole.format(Math.round(n)));
  animateNumber($("statBuses"), buses, n => String(Math.round(n)));
  animateNumber($("statToday"), travellingToday, n => String(Math.round(n)));
}

/** Counts up once on the first load only; afterwards (and with reduced motion) values update instantly. */
function animateNumber(el, target, format) {
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (reduceMotion || el.dataset.animated === "1") {
    el.textContent = format(target);
    el.dataset.animated = "1";
    return;
  }
  el.dataset.animated = "1";
  const start = performance.now();
  const duration = 700;
  const tick = now => {
    const t = Math.min(1, (now - start) / duration);
    el.textContent = format(target * (1 - Math.pow(1 - t, 3)));
    if (t < 1) requestAnimationFrame(tick); else el.textContent = format(target);
  };
  requestAnimationFrame(tick);
}

/* --------------------------------------------------- add / edit booking form */

function readForm() {
  const text = name => $(`field-${name}`).value.trim();
  return {
    passengerName: text("passengerName"),
    busNumber: text("busNumber"),
    source: text("source"),
    destination: text("destination"),
    travelDate: $("field-travelDate").value,
    seatNumber: text("seatNumber"),
    ticketFare: text("ticketFare")
  };
}

/** Client-side validation. The same rules are enforced again by the Spring Boot backend. */
function validateBooking(v) {
  const errors = {};

  if (!v.passengerName) errors.passengerName = "Enter the passenger name.";
  else if (v.passengerName.length > 100) errors.passengerName = "Passenger name can have at most 100 characters.";

  if (!v.busNumber) errors.busNumber = "Enter the bus number.";
  else if (v.busNumber.length > 20) errors.busNumber = "Bus number can have at most 20 characters.";

  if (!v.source) errors.source = "Enter the source.";
  else if (v.source.length > 50) errors.source = "Source can have at most 50 characters.";

  if (!v.destination) errors.destination = "Enter the destination.";
  else if (v.destination.length > 50) errors.destination = "Destination can have at most 50 characters.";
  else if (v.source && v.source.toLowerCase() === v.destination.toLowerCase()) {
    errors.destination = "Destination must be different from the source.";
  }

  if (!v.travelDate) errors.travelDate = "Choose the travel date.";
  else if (v.travelDate < todayISO()) errors.travelDate = "Travel date must be today or a later date.";

  if (v.seatNumber === "") errors.seatNumber = "Enter a seat number from 1 to 50.";
  else {
    const seat = Number(v.seatNumber);
    if (!Number.isInteger(seat) || seat < 1 || seat > 50) errors.seatNumber = "Seat number must be a whole number from 1 to 50.";
  }

  if (v.ticketFare === "") errors.ticketFare = "Enter the ticket fare.";
  else if (!(Number(v.ticketFare) > 0)) errors.ticketFare = "Ticket fare must be greater than zero.";
  else if (!/^\d{1,8}(\.\d{1,2})?$/.test(v.ticketFare)) errors.ticketFare = "Use up to 8 digits and 2 decimal places.";

  return errors;
}

function clearErrors() {
  FORM_FIELDS.forEach(name => $(`field-${name}`).classList.remove("is-invalid"));
  $("formAlert").classList.add("d-none");
}

function showFieldErrors(errors) {
  let first = null;
  Object.entries(errors || {}).forEach(([name, message]) => {
    if (!FORM_FIELDS.includes(name)) return;
    const input = $(`field-${name}`);
    input.classList.add("is-invalid");
    $(`error-${name}`).textContent = message;
    if (!first) first = input;
  });
  if (first) first.focus();
}

function showFormAlert(message) {
  const alertBox = $("formAlert");
  alertBox.textContent = message;
  alertBox.classList.remove("d-none");
}

function fillForm(booking) {
  const data = booking || {};
  FORM_FIELDS.forEach(name => { $(`field-${name}`).value = data[name] ?? ""; });
  clearErrors();
}

function setSaving(isSaving) {
  state.saving = isSaving;
  $("saveBtn").disabled = isSaving;
  $("saveSpinner").classList.toggle("d-none", !isSaving);
}

function openAdd() {
  state.editing = null;
  $("bookingModalTitle").textContent = "Add booking";
  fillForm(null);
  bookingModal.show();
}

async function openEdit(id) {
  try {
    const booking = await api(`${API_BASE}/${id}`);          // GET by ID
    state.editing = booking;
    $("bookingModalTitle").textContent = `Edit booking #${booking.bookingId}`;
    fillForm(booking);
    bookingModal.show();
  } catch (err) {
    showToast(err.message, "danger");
    if (err.status === 404) refresh();
  }
}

async function onSubmitBooking(event) {
  event.preventDefault();
  if (state.saving) return;
  clearErrors();

  const values = readForm();
  const errors = validateBooking(values);
  if (Object.keys(errors).length) { showFieldErrors(errors); return; }

  const payload = {
    passengerName: values.passengerName,
    busNumber: values.busNumber.toUpperCase(),
    source: values.source,
    destination: values.destination,
    travelDate: values.travelDate,
    seatNumber: Number(values.seatNumber),
    ticketFare: Number(values.ticketFare)
  };

  const editing = state.editing;
  setSaving(true);
  try {
    if (editing) {
      await api(`${API_BASE}/${editing.bookingId}`, { method: "PUT", body: JSON.stringify(payload) });
      showToast(`Booking #${editing.bookingId} updated.`);
    } else {
      const saved = await api(API_BASE, { method: "POST", body: JSON.stringify(payload) });
      showToast(`Booking #${saved.bookingId} saved.`);
    }
    bookingModal.hide();
    await refresh();
  } catch (err) {
    showFormAlert(err.message);
    showFieldErrors(err.fieldErrors);
  } finally {
    setSaving(false);
  }
}

/* ------------------------------------------------------------ view / cancel */

async function openView(id) {
  try {
    const b = await api(`${API_BASE}/${id}`);                // GET by ID
    state.viewedId = b.bookingId;
    $("view-source").textContent = b.source;
    $("view-destination").textContent = b.destination;
    $("view-bookingId").textContent = `#${b.bookingId}`;
    $("view-passengerName").textContent = b.passengerName;
    $("view-busNumber").textContent = b.busNumber;
    $("view-travelDate").textContent = formatDate(b.travelDate);
    $("view-seatNumber").textContent = b.seatNumber;
    $("view-ticketFare").textContent = formatINR(b.ticketFare);
    viewModal.show();
  } catch (err) {
    showToast(err.message, "danger");
    if (err.status === 404) refresh();
  }
}

function askCancel(id) {
  const b = state.rows.find(row => row.bookingId === id);
  state.cancelId = id;
  $("cancelSummary").textContent = b
    ? `Booking #${b.bookingId}: ${b.passengerName}, ${b.source} to ${b.destination}, seat ${b.seatNumber} on ${formatDate(b.travelDate)}.`
    : `Booking #${id}`;
  cancelModal.show();
}

async function confirmCancel() {
  const id = state.cancelId;
  if (id == null) return;
  const button = $("confirmCancelBtn");
  button.disabled = true;
  try {
    await api(`${API_BASE}/${id}`, { method: "DELETE" });    // 204 No Content
    showToast(`Booking #${id} cancelled.`);
  } catch (err) {
    showToast(err.message, "danger");
  } finally {
    button.disabled = false;
    state.cancelId = null;
    cancelModal.hide();
    await refresh();
  }
}

/* ------------------------------------------------------------------ reports */

async function runReport(path, params, label) {
  // URLSearchParams encodes spaces and special characters safely
  const url = `${API_BASE}/reports/${path}?${new URLSearchParams(params)}`;
  try {
    const rows = await api(url);
    state.view = { url, label };
    showRows(rows);
    $("bookings").scrollIntoView();
  } catch (err) {
    showToast(err.message, "danger");
  }
}

function bindReportForm(formId, build) {
  $(formId).addEventListener("submit", event => {
    event.preventDefault();
    const form = event.currentTarget;
    form.querySelectorAll("input").forEach(input => { input.value = input.value.trim(); });
    if (!form.checkValidity()) { form.classList.add("was-validated"); return; }
    form.classList.remove("was-validated");
    const { path, params, label } = build();
    runReport(path, params, label);
  });
}

function clearFilter() {
  state.view = { url: null, label: "" };
  ["routeForm", "dateForm", "passengerForm"].forEach(id => {
    $(id).reset();
    $(id).classList.remove("was-validated");
  });
  refresh();
}

/* --------------------------------------------------------------------- init */

document.addEventListener("DOMContentLoaded", () => {
  bookingModal = new bootstrap.Modal($("bookingModal"));
  viewModal = new bootstrap.Modal($("viewModal"));
  cancelModal = new bootstrap.Modal($("cancelModal"));

  $("field-travelDate").min = todayISO();   // blocks past dates in the date picker

  // Any "Add booking" button
  document.addEventListener("click", event => {
    if (event.target.closest('[data-action="add-booking"]')) openAdd();
  });

  // Row buttons (event delegation) plus the "Try again" button of the load error
  $("bookingTableBody").addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const id = Number(button.dataset.id);
    const actions = { view: () => openView(id), edit: () => openEdit(id), cancel: () => askCancel(id), retry: () => refresh() };
    if (actions[button.dataset.action]) actions[button.dataset.action]();
  });

  $("bookingForm").addEventListener("submit", onSubmitBooking);
  $("resetFormBtn").addEventListener("click", () => { fillForm(state.editing); $("field-passengerName").focus(); });
  FORM_FIELDS.forEach(name => $(`field-${name}`).addEventListener("input", () => $(`field-${name}`).classList.remove("is-invalid")));
  $("bookingModal").addEventListener("shown.bs.modal", () => $("field-passengerName").focus());

  $("viewEditBtn").addEventListener("click", () => {
    const id = state.viewedId;
    viewModal.hide();
    if (id != null) openEdit(id);
  });
  $("confirmCancelBtn").addEventListener("click", confirmCancel);
  $("refreshBtn").addEventListener("click", refresh);
  $("clearFilterBtn").addEventListener("click", clearFilter);

  bindReportForm("routeForm", () => {
    const source = $("routeSource").value;
    const dest = $("routeDest").value;
    return { path: "route", params: { source, dest }, label: `Route: ${source} to ${dest}` };
  });
  bindReportForm("dateForm", () => {
    const travelDate = $("reportDate").value;
    return { path: "date", params: { travelDate }, label: `Travel date: ${formatDate(travelDate)}` };
  });
  bindReportForm("passengerForm", () => {
    const passengerName = $("reportPassenger").value;
    return { path: "passenger", params: { passengerName }, label: `Passenger: ${passengerName}` };
  });

  refresh();
});
