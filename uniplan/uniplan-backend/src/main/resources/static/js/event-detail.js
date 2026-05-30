/* =========================================================
   EVENT DETAIL — carga evento + lógica de inscripción
========================================================= */

const token   = sessionStorage.getItem("token");
const role    = sessionStorage.getItem("role");
const eventId = new URLSearchParams(window.location.search).get("id");

let currentEvent        = null;
let myRegistration      = null;   // inscripción activa del estudiante (si existe)

/* ── Nav según rol ── */
document.getElementById("navRole").textContent = role || "";
const backHref = role === "STUDENT" ? "/student/home" : "/admin/events";
document.getElementById("navBrand").href = backHref;
document.getElementById("backBtn").href  = backHref;

if (!eventId) { window.location.href = backHref; }

/* ─────────────────────────────────────────────
   LOAD EVENT
───────────────────────────────────────────── */
async function loadEvent() {
    try {
        const res = await fetch(`/events/${eventId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Evento no encontrado");
        currentEvent = await res.json();
        renderEvent(currentEvent);

        if (role === "STUDENT") {
            await checkMyRegistration();
        } else {
            // Admin/Organizer: mostrar acciones de gestión y lista de inscritos
            document.getElementById("studentActions").style.display = "none";
            document.getElementById("adminActions").style.display   = "block";
            document.getElementById("editEventLink").href = `/admin/events/edit?id=${eventId}`;
            document.getElementById("registrationsSection").style.display = "block";
            await loadRegistrations();
        }

    } catch (err) {
        document.getElementById("eventTitle").textContent = "No se pudo cargar el evento";
        showMsg(err.message, "error");
    }
}

/* ─────────────────────────────────────────────
   RENDER EVENT
───────────────────────────────────────────── */
function renderEvent(e) {

    document.title = `UniPlan — ${e.title || "Evento"}`;

    // Hero
    document.getElementById("eventTypeBadge").textContent   = labelType(e.type);
    document.getElementById("eventTitle").textContent        = e.title       || "Sin título";
    document.getElementById("eventDescription").textContent  = e.description || "";

    // Estado
    const statusHtml = `<span class="status-badge status-${(e.status||"").toLowerCase()}">${e.status || "—"}</span>`;
    document.getElementById("statusBadgeWrap").innerHTML = statusHtml;

    // Fechas
    if (e.schedule) {
        document.getElementById("startDate").textContent = formatDate(e.schedule.startDate);
        document.getElementById("endDate").textContent   = formatDate(e.schedule.endDate);
    }

    // Ubicación
    if (e.location) {
        document.getElementById("modality").textContent = labelModality(e.location.modality);
        document.getElementById("venue").textContent    = e.location.venue  || "—";
        document.getElementById("campus").textContent   = e.location.campus || "—";
        document.getElementById("room").textContent     = e.location.room   || "—";

        if (e.location.meetingUrl) {
            document.getElementById("meetingUrl").href = e.location.meetingUrl;
            document.getElementById("meetingUrlRow").style.display = "flex";
        }
    }

    // Capacidad
    if (e.capacity) {
        const total     = e.capacity.total      || 0;
        const registered= e.capacity.registered || 0;
        const available = e.capacity.available  ?? (total - registered);
        const pct       = total > 0 ? Math.round((registered / total) * 100) : 0;

        document.getElementById("capacityTotal").textContent      = total;
        document.getElementById("capacityRegistered").textContent  = registered;
        document.getElementById("capacityAvailable").textContent   = available;
        document.getElementById("sidebarSpots").textContent        = available;

        const fill = document.getElementById("capacityFill");
        fill.style.width = pct + "%";
        if (pct >= 100) fill.classList.add("full");
    }

    // Detalles extras
    if (e.details && Object.keys(e.details).length > 0) {
        document.getElementById("detailsSection").style.display = "block";
        const container = document.getElementById("detailsContent");
        container.innerHTML = Object.entries(e.details).map(([k, v]) => `
            <div class="info-item">
                <div class="info-label">${k}</div>
                <div class="info-value">${Array.isArray(v) ? v.join(", ") : v}</div>
            </div>
        `).join("");
    }
}

/* ─────────────────────────────────────────────
   CHECK MY REGISTRATION (estudiante)
───────────────────────────────────────────── */
async function checkMyRegistration() {
    try {
        const res = await fetch("/registrations/my", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) return;
        const regs = await res.json();

        // Buscar inscripción activa para este evento
        myRegistration = regs.find(r =>
            r.eventId === eventId &&
            (r.status === "REGISTERED" || r.status === "WAITLIST")
        ) || null;

        updateStudentUI();

    } catch { /* sin inscripción previa */ }
}

function updateStudentUI() {
    const btnReg    = document.getElementById("btnRegister");
    const btnCancel = document.getElementById("btnCancelReg");
    const note      = document.getElementById("registerNote");

    const eventStatus = currentEvent?.status;
    const available   = currentEvent?.capacity?.available ?? 1;

    if (eventStatus !== "ACTIVE") {
        btnReg.disabled = true;
        btnReg.textContent = `Evento ${eventStatus?.toLowerCase()}`;
        note.textContent = "Este evento ya no acepta inscripciones.";
        return;
    }

    if (myRegistration) {
        btnReg.style.display    = "none";
        btnCancel.style.display = "block";

        if (myRegistration.status === "WAITLIST") {
            note.textContent = "Estás en lista de espera. Te notificaremos si se libera un cupo.";
        } else {
            note.textContent = "✓ Ya estás inscrito en este evento.";
            note.style.color = "#86efac";
        }
    } else {
        btnReg.style.display    = "block";
        btnCancel.style.display = "none";

        if (available <= 0) {
            const waitlist = currentEvent?.capacity?.waitlistEnabled;
            if (waitlist) {
                btnReg.textContent = "Unirme a lista de espera";
                note.textContent = "No hay cupos. Puedes anotarte en lista de espera.";
            } else {
                btnReg.disabled = true;
                btnReg.textContent = "Sin cupos disponibles";
                note.textContent = "Este evento está lleno y no tiene lista de espera.";
            }
        } else {
            btnReg.textContent = "Inscribirme al evento";
            note.textContent = "Al inscribirte aceptas las condiciones del evento.";
        }
    }
}

/* ─────────────────────────────────────────────
   REGISTER TO EVENT
───────────────────────────────────────────── */
async function registerToEvent() {
    const btn = document.getElementById("btnRegister");
    btn.disabled  = true;
    btn.textContent = "Procesando...";
    hideMsg();

    try {
        const res = await fetch("/registrations", {
            method:  "POST",
            headers: {
                "Content-Type":  "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ eventId })
        });

        const data = await res.json();

        if (!res.ok) {
            throw new Error(data.error || data.message || "No se pudo completar la inscripción");
        }

        myRegistration = data;

        // Actualizar cupos en pantalla
        if (data.availableSpotsAfter !== undefined) {
            document.getElementById("capacityAvailable").textContent = data.availableSpotsAfter;
            document.getElementById("sidebarSpots").textContent      = data.availableSpotsAfter;
            const total = currentEvent?.capacity?.total || 1;
            const registered = total - data.availableSpotsAfter;
            const pct = Math.round((registered / total) * 100);
            document.getElementById("capacityFill").style.width = pct + "%";
        }

        if (data.status === "WAITLIST") {
            showMsg("Te has unido a la lista de espera.", "info");
        } else {
            showMsg("¡Inscripción exitosa! Ya estás registrado en el evento.", "success");
        }

        updateStudentUI();

    } catch (err) {
        const msg = err.message?.length < 200 ? err.message : "Error al procesar la inscripción.";
        showMsg(msg, "error");
        btn.disabled  = false;
        btn.textContent = "Inscribirme al evento";
    }
}

/* ─────────────────────────────────────────────
   CANCEL MY REGISTRATION
───────────────────────────────────────────── */
async function cancelMyRegistration() {
    if (!myRegistration?.id) return;
    if (!confirm("¿Cancelar tu inscripción a este evento?")) return;

    const btn = document.getElementById("btnCancelReg");
    btn.disabled  = true;
    btn.textContent = "Cancelando...";
    hideMsg();

    try {
        const res = await fetch(`/registrations/${myRegistration.id}`, {
            method:  "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) {
            const data = await res.json();
            throw new Error(data.error || "No se pudo cancelar");
        }

        myRegistration = null;

        // Actualizar cupos en pantalla
        const available = parseInt(document.getElementById("capacityAvailable").textContent || "0") + 1;
        document.getElementById("capacityAvailable").textContent = available;
        document.getElementById("sidebarSpots").textContent      = available;

        showMsg("Inscripción cancelada correctamente.", "success");
        updateStudentUI();

    } catch (err) {
        showMsg(err.message, "error");
        btn.disabled  = false;
        btn.textContent = "Cancelar inscripción";
    }
}

/* ─────────────────────────────────────────────
   LOAD REGISTRATIONS (admin/organizer)
───────────────────────────────────────────── */
async function loadRegistrations() {
    try {
        const res = await fetch(`/registrations/event/${eventId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();
        const regs = await res.json();

        document.getElementById("regCount").textContent = `${regs.length} inscriptos`;

        const tbody = document.getElementById("regTableBody");
        if (!regs.length) {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;color:rgba(255,255,255,0.3);padding:1.5rem;">Sin inscritos aún</td></tr>`;
            return;
        }
        tbody.innerHTML = regs.map(r => `
            <tr>
                <td>${r.studentName || r.studentId || "—"}</td>
                <td>${r.studentEmail || "—"}</td>
                <td><span class="reg-badge reg-${(r.status||"").toLowerCase()}">${r.status || "—"}</span></td>
                <td>${r.registeredAt ? formatDate(r.registeredAt) : "—"}</td>
            </tr>
        `).join("");

    } catch {
        document.getElementById("regTableBody").innerHTML =
            `<tr><td colspan="4" style="text-align:center;color:#fca5a5;padding:1rem;">No se pudieron cargar los inscritos</td></tr>`;
    }
}

/* ─────────────────────────────────────────────
   HELPERS
───────────────────────────────────────────── */
function showMsg(text, type) {
    const el = document.getElementById("registerMessage");
    el.textContent  = text;
    el.className    = `register-message msg-${type}`;
    el.style.display = "block";
}
function hideMsg() {
    document.getElementById("registerMessage").style.display = "none";
}

function formatDate(dt) {
    if (!dt) return "—";
    return new Date(dt).toLocaleString("es-CO", {
        day: "2-digit", month: "short", year: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}

function labelType(type) {
    const map = { ACADEMIC:"Académico", CULTURAL:"Cultural", SPORT:"Deporte",
                  VOLUNTEER:"Voluntariado", WORKSHOP:"Taller", OTHER:"Otro" };
    return map[type] || type || "Evento";
}

function labelModality(m) {
    return { IN_PERSON:"Presencial", VIRTUAL:"Virtual", HYBRID:"Híbrido" }[m] || m || "—";
}

/* Iniciar */
loadEvent();
