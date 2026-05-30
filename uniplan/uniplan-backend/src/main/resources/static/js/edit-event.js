/* =========================================================
   EDIT EVENT — carga el evento por ID y hace PUT /events/{id}
========================================================= */

const token   = sessionStorage.getItem("token");
const msgDiv  = document.getElementById("message");

// Obtener el ID del evento desde la URL: /admin/events/edit?id=xxx
const eventId = new URLSearchParams(window.location.search).get("id");

if (!eventId) {
    window.location.href = "/admin/events";
}

/* ── SHOW / HIDE MEETING URL ── */
document.getElementById("modality").addEventListener("change", toggleMeetingUrl);

function toggleMeetingUrl() {
    const v = document.getElementById("modality").value;
    document.getElementById("meetingUrlGroup").style.display =
        (v === "VIRTUAL" || v === "HYBRID") ? "block" : "none";
}

/* ── HELPERS ── */
function showMessage(text, type = "success") {
    msgDiv.style.display = "block";
    msgDiv.textContent   = text;
    msgDiv.style.background = type === "success"
        ? "rgba(34,197,94,0.12)"  : "rgba(239,68,68,0.12)";
    msgDiv.style.border = type === "success"
        ? "1px solid rgba(34,197,94,0.28)" : "1px solid rgba(239,68,68,0.28)";
    msgDiv.style.color  = type === "success" ? "#86efac" : "#fca5a5";
    msgDiv.style.padding = "0.75rem 1rem";
    msgDiv.style.borderRadius = "10px";
}

function toDatetimeLocal(isoString) {
    if (!isoString) return "";
    // "2025-05-30T10:00:00" → "2025-05-30T10:00"
    return isoString.substring(0, 16);
}

/* ── LOAD EVENT ── */
async function loadEvent() {
    try {
        const res = await fetch(`/events/${eventId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("No se encontró el evento");
        const e = await res.json();

        // Básicos
        document.getElementById("title").value       = e.title       || "";
        document.getElementById("type").value        = e.type        || "";
        document.getElementById("description").value = e.description || "";

        // Schedule
        if (e.schedule) {
            document.getElementById("startDate").value = toDatetimeLocal(e.schedule.startDate);
            document.getElementById("endDate").value   = toDatetimeLocal(e.schedule.endDate);
        }

        // Location
        if (e.location) {
            document.getElementById("modality").value   = e.location.modality  || "";
            document.getElementById("venue").value      = e.location.venue     || "";
            document.getElementById("campus").value     = e.location.campus    || "";
            document.getElementById("room").value       = e.location.room      || "";
            document.getElementById("meetingUrl").value = e.location.meetingUrl || "";
            toggleMeetingUrl();
        }

        // Capacity
        if (e.capacity) {
            document.getElementById("capacity").value         = e.capacity.total || "";
            document.getElementById("waitlistEnabled").checked = e.capacity.waitlistEnabled || false;
        }

        // Details
        if (e.details && Object.keys(e.details).length > 0) {
            document.getElementById("details").value = JSON.stringify(e.details, null, 2);
        }

    } catch (err) {
        showMessage(err.message, "error");
    }
}

/* ── SUBMIT ── */
document.getElementById("editEventForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    msgDiv.style.display = "none";

    const title       = document.getElementById("title").value.trim();
    const type        = document.getElementById("type").value;
    const description = document.getElementById("description").value.trim();
    const startDate   = document.getElementById("startDate").value + ":00";
    const endDate     = document.getElementById("endDate").value   + ":00";
    const modality    = document.getElementById("modality").value;
    const venue       = document.getElementById("venue").value.trim();
    const campus      = document.getElementById("campus").value.trim();
    const room        = document.getElementById("room").value.trim();
    const meetingUrl  = document.getElementById("meetingUrl").value.trim();
    const capacity    = parseInt(document.getElementById("capacity").value);
    const waitlist    = document.getElementById("waitlistEnabled").checked;
    const detailsRaw  = document.getElementById("details").value.trim();

    if (!title || !type || !description || !startDate || !endDate || !modality || !venue || !capacity) {
        showMessage("Completa todos los campos requeridos (*)", "error");
        return;
    }
    if (new Date(endDate) <= new Date(startDate)) {
        showMessage("La fecha de finalización debe ser posterior a la de inicio.", "error");
        return;
    }
    if ((modality === "VIRTUAL" || modality === "HYBRID") && !meetingUrl) {
        showMessage("Ingresa el enlace de reunión.", "error");
        return;
    }

    let details = {};
    if (detailsRaw) {
        try { details = JSON.parse(detailsRaw); }
        catch { showMessage("El JSON de detalles no es válido.", "error"); return; }
    }

    const body = {
        title, description, type,
        schedule: { startDate, endDate },
        location: { venue, campus: campus || null, room: room || null, modality, meetingUrl: meetingUrl || null },
        capacity: { total: capacity, waitlistEnabled: waitlist },
        details
    };

    const btn = document.querySelector(".create-event-btn[type=submit]");
    const orig = btn.innerHTML;
    btn.disabled  = true;
    btn.innerHTML = "<span>Guardando...</span>";

    try {
        const res = await fetch(`/events/${eventId}`, {
            method:  "PUT",
            headers: {
                "Content-Type":  "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err);
        }

        showMessage("Evento actualizado correctamente.", "success");
        setTimeout(() => { window.location.href = "/admin/events"; }, 1500);

    } catch (err) {
        const msg = err.message?.length < 200 ? err.message : "No se pudo actualizar el evento.";
        showMessage(msg, "error");
    } finally {
        btn.disabled  = false;
        btn.innerHTML = orig;
    }
});

// Cargar datos al iniciar
loadEvent();
