/* =========================================================
   EDIT EVENT — carga evento y hace PUT /events/{id}
   IDs: title, type, description, startDate, endDate,
        modality, venue, campus, room, address, meetingUrlGroup, meetingUrl,
        capacity, waitlistEnabled, details, message, editEventForm
========================================================= */

const token   = sessionStorage.getItem("token");
const eventId = new URLSearchParams(window.location.search).get("id");
const msgDiv  = document.getElementById("message");
let currentCapacity = { registered: 0, waitlist: 0 };

if (!eventId) window.location.href = "/admin/events";

/* ── Mostrar/ocultar link de reunión ── */
document.getElementById("modality").addEventListener("change", toggleMeetingUrl);

function toggleMeetingUrl() {
    const v = document.getElementById("modality").value;
    document.getElementById("meetingUrlGroup").style.display =
        (v === "VIRTUAL" || v === "HYBRID") ? "block" : "none";
}

/* ── Helpers ── */
function showMsg(text, type) {
    msgDiv.className     = `ev-msg ev-msg-${type}`;
    msgDiv.textContent   = text;
    msgDiv.style.display = "block";
}

function toLocal(iso) {
    if (!iso) return "";
    return iso.substring(0, 16);
}

/* ── Cargar evento ── */
async function loadEvent() {
    try {
        const res = await fetch(`/events/${eventId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Evento no encontrado");
        const e = await res.json();

        document.getElementById("title").value       = e.title       || "";
        document.getElementById("type").value        = e.type        || "";
        document.getElementById("description").value = e.description || "";

        if (e.schedule) {
            document.getElementById("startDate").value = toLocal(e.schedule.startDate);
            document.getElementById("endDate").value   = toLocal(e.schedule.endDate);
        }
        if (e.location) {
            document.getElementById("modality").value   = e.location.modality   || "";
            document.getElementById("venue").value      = e.location.venue      || "";
            document.getElementById("campus").value     = e.location.campus     || "";
            document.getElementById("room").value       = e.location.room       || "";
            document.getElementById("address").value   = e.location.address    || "";
            document.getElementById("meetingUrl").value = e.location.meetingUrl || "";
            toggleMeetingUrl();
        }
        if (e.capacity) {
            document.getElementById("capacity").value          = e.capacity.total || "";
            document.getElementById("waitlistEnabled").checked = !!e.capacity.waitlistEnabled;
            currentCapacity = {
                registered: e.capacity.registered ?? 0,
                waitlist:   e.capacity.waitlist   ?? 0
            };
        }
        if (e.details && Object.keys(e.details).length > 0) {
            document.getElementById("details").value = JSON.stringify(e.details, null, 2);
        }
    } catch (err) {
        showMsg(err.message, "error");
    }
}

/* ── Submit ── */
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
    const address     = document.getElementById("address").value.trim();
    const meetingUrl  = document.getElementById("meetingUrl").value.trim();
    const capacity    = parseInt(document.getElementById("capacity").value);
    const waitlist    = document.getElementById("waitlistEnabled").checked;
    const detailsRaw  = document.getElementById("details").value.trim();
    const timezone    = Intl.DateTimeFormat().resolvedOptions().timeZone || "America/Bogota";

    if (!title || !type || !description || !startDate || !endDate || !modality || !venue || !capacity) {
        showMsg("Completa todos los campos requeridos (*)", "error"); return;
    }
    if (new Date(endDate) <= new Date(startDate)) {
        showMsg("La fecha de finalización debe ser posterior a la de inicio.", "error"); return;
    }
    if ((modality === "VIRTUAL" || modality === "HYBRID") && !meetingUrl) {
        showMsg("Ingresa el enlace de reunión.", "error"); return;
    }

    let details = {};
    if (detailsRaw) {
        try { details = JSON.parse(detailsRaw); }
        catch { showMsg("El JSON de detalles no es válido.", "error"); return; }
    }

    const durationMinutes = Math.round((new Date(endDate) - new Date(startDate)) / 60000);
    const newAvailable    = Math.max(0, capacity - currentCapacity.registered);

    const body = {
        title, description, type,
        schedule: { startDate, endDate, durationMinutes, timezone },
        location: {
            venue,
            campus:     campus     || null,
            room:       room       || null,
            address:    address    || null,
            modality,
            meetingUrl: meetingUrl || null
        },
        capacity: {
            total:           capacity,
            registered:      currentCapacity.registered,
            available:       newAvailable,
            waitlist:        currentCapacity.waitlist,
            waitlistEnabled: waitlist
        },
        details
    };

    const btn = document.querySelector(".ev-btn-primary");
    const orig = btn.textContent;
    btn.disabled = true; btn.textContent = "Guardando...";

    try {
        const res = await fetch(`/events/${eventId}`, {
            method: "PUT",
            headers: { "Content-Type":"application/json", "Authorization":`Bearer ${token}` },
            body: JSON.stringify(body)
        });
        if (!res.ok) { const d = await res.text(); throw new Error(d); }

        showMsg("Evento actualizado correctamente.", "success");
        setTimeout(() => { window.location.href = "/admin/events"; }, 1500);
    } catch (err) {
        showMsg(err.message?.length < 200 ? err.message : "No se pudo actualizar el evento.", "error");
    } finally {
        btn.disabled = false; btn.textContent = orig;
    }
});

loadEvent();
