/* =========================================================
   EVENT DETAIL — carga evento + lógica de inscripción
   IDs: navBrand, navRole, backBtn, eventCard, eventTypeBadge,
        eventTitle, eventDescription, startDate, endDate,
        modality, venue, campus, room, meetingUrlRow, meetingUrl,
        capTotal, capRegistered, capAvailable, capFill,
        sidebarSpots, statusBadgeWrap, detailsSection, detailsContent,
        regMessage, studentActions, btnRegister, btnCancelReg, regNote,
        adminActions, editEventLink,
        registrationsSection, regCount, regTableBody
========================================================= */

const token   = sessionStorage.getItem("token");
const role    = sessionStorage.getItem("role");
const eventId = new URLSearchParams(window.location.search).get("id");

let currentEvent   = null;
let myRegistration = null;

/* ── Navbar según rol ── */
document.getElementById("navRole").textContent = role || "";
const backHref = role === "STUDENT" ? "/student/home" : "/admin/events";
document.getElementById("navBrand").href = backHref;
document.getElementById("backBtn").href  = backHref;

if (!eventId) window.location.href = backHref;

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
            document.getElementById("studentActions").style.display  = "none";
            document.getElementById("adminActions").style.display    = "block";
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

    document.getElementById("eventTypeBadge").textContent  = labelType(e.type);
    document.getElementById("eventTitle").textContent      = e.title       || "Sin título";
    document.getElementById("eventDescription").textContent= e.description || "";

    // Estado badge
    const s = (e.status || "").toLowerCase();
    document.getElementById("statusBadgeWrap").innerHTML =
        `<span class="ev-status ev-status-${s}">${e.status || "—"}</span>`;

    // Fechas
    if (e.schedule) {
        document.getElementById("startDate").textContent = fmtDate(e.schedule.startDate);
        document.getElementById("endDate").textContent   = fmtDate(e.schedule.endDate);
    }

    // Ubicación
    if (e.location) {
        document.getElementById("modality").textContent = labelModality(e.location.modality);
        document.getElementById("venue").textContent    = e.location.venue  || "—";
        document.getElementById("campus").textContent   = e.location.campus || "—";
        document.getElementById("room").textContent     = e.location.room   || "—";
        if (e.location.meetingUrl) {
            document.getElementById("meetingUrl").href = e.location.meetingUrl;
            document.getElementById("meetingUrlRow").style.display = "block";
        }
    }

    // Capacidad
    if (e.capacity) {
        const total      = e.capacity.total      || 0;
        const registered = e.capacity.registered || 0;
        const available  = e.capacity.available  ?? (total - registered);
        const pct        = total > 0 ? Math.round((registered / total) * 100) : 0;

        document.getElementById("capTotal").textContent      = total;
        document.getElementById("capRegistered").textContent = registered;
        document.getElementById("capAvailable").textContent  = available;
        document.getElementById("sidebarSpots").textContent  = available;

        const fill = document.getElementById("capFill");
        fill.style.width = pct + "%";
        if (pct >= 100) fill.classList.add("full");
    }

    // Detalles extras
    if (e.details && Object.keys(e.details).length > 0) {
        document.getElementById("detailsSection").style.display = "block";
        document.getElementById("detailsContent").innerHTML =
            Object.entries(e.details).map(([k, v]) => `
                <div class="ev-info-item">
                    <p class="ev-info-label">${k}</p>
                    <p class="ev-info-value">${Array.isArray(v) ? v.join(", ") : v}</p>
                </div>
            `).join("");
    }
}

/* ─────────────────────────────────────────────
   CHECK MY REGISTRATION
───────────────────────────────────────────── */
async function checkMyRegistration() {
    try {
        const res = await fetch("/registrations/my", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) return;
        const regs = await res.json();
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
    const note      = document.getElementById("regNote");
    const available = currentEvent?.capacity?.available ?? 1;
    const status    = currentEvent?.status;

    if (status !== "ACTIVE") {
        btnReg.disabled = true;
        btnReg.textContent = `Evento ${(status||"").toLowerCase()}`;
        note.textContent = "Este evento ya no acepta inscripciones.";
        return;
    }

    if (myRegistration) {
        btnReg.style.display    = "none";
        btnCancel.style.display = "block";
        note.textContent = myRegistration.status === "WAITLIST"
            ? "Estás en lista de espera."
            : "✓ Ya estás inscrito en este evento.";
        if (myRegistration.status === "REGISTERED") note.style.color = "#86efac";
        return;
    }

    btnReg.style.display    = "block";
    btnCancel.style.display = "none";

    if (available <= 0) {
        const waitlist = currentEvent?.capacity?.waitlistEnabled;
        if (waitlist) {
            btnReg.textContent = "Unirme a lista de espera";
            note.textContent   = "No hay cupos. Puedes unirte a la lista de espera.";
        } else {
            btnReg.disabled    = true;
            btnReg.textContent = "Sin cupos disponibles";
            note.textContent   = "Este evento está lleno.";
        }
    } else {
        btnReg.textContent = "Inscribirme al evento";
        note.textContent   = "Al inscribirte aceptas las condiciones del evento.";
    }
}

/* ─────────────────────────────────────────────
   REGISTER
───────────────────────────────────────────── */
async function registerToEvent() {
    const btn = document.getElementById("btnRegister");
    btn.disabled = true; btn.textContent = "Procesando...";
    hideMsg();

    try {
        const res = await fetch("/registrations", {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
            body: JSON.stringify({ eventId })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || data.message || "Error al inscribirse");

        myRegistration = data;

        if (data.availableSpotsAfter !== undefined) {
            document.getElementById("capAvailable").textContent = data.availableSpotsAfter;
            document.getElementById("sidebarSpots").textContent = data.availableSpotsAfter;
            const total = currentEvent?.capacity?.total || 1;
            const pct   = Math.round(((total - data.availableSpotsAfter) / total) * 100);
            document.getElementById("capFill").style.width = pct + "%";
        }

        showMsg(data.status === "WAITLIST"
            ? "Te has unido a la lista de espera."
            : "¡Inscripción exitosa!", "success");
        updateStudentUI();

    } catch (err) {
        showMsg(err.message?.length < 200 ? err.message : "Error al procesar la inscripción.", "error");
        btn.disabled = false; btn.textContent = "Inscribirme al evento";
    }
}

/* ─────────────────────────────────────────────
   CANCEL REGISTRATION
───────────────────────────────────────────── */
async function cancelMyRegistration() {
    if (!myRegistration?.id) return;
    if (!confirm("¿Cancelar tu inscripción a este evento?")) return;

    const btn = document.getElementById("btnCancelReg");
    btn.disabled = true; btn.textContent = "Cancelando...";
    hideMsg();

    try {
        const res = await fetch(`/registrations/${myRegistration.id}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) { const d = await res.json(); throw new Error(d.error || "Error"); }

        myRegistration = null;
        const prev = parseInt(document.getElementById("capAvailable").textContent || "0") + 1;
        document.getElementById("capAvailable").textContent = prev;
        document.getElementById("sidebarSpots").textContent = prev;

        showMsg("Inscripción cancelada.", "success");
        updateStudentUI();
    } catch (err) {
        showMsg(err.message, "error");
        btn.disabled = false; btn.textContent = "Cancelar inscripción";
    }
}

/* ─────────────────────────────────────────────
   LOAD REGISTRATIONS (admin)
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
            tbody.innerHTML = `<tr><td colspan="4" class="ev-empty">Sin inscritos aún</td></tr>`;
            return;
        }
        tbody.innerHTML = regs.map(r => `
            <tr>
                <td>${r.studentName || "—"}</td>
                <td>${r.studentEmail || "—"}</td>
                <td><span class="ev-badge ev-badge-${(r.status||"").toLowerCase()}">${r.status || "—"}</span></td>
                <td>${r.registeredAt ? fmtDate(r.registeredAt) : "—"}</td>
            </tr>
        `).join("");
    } catch {
        document.getElementById("regTableBody").innerHTML =
            `<tr><td colspan="4" class="ev-empty" style="color:#fca5a5;">Error al cargar inscritos</td></tr>`;
    }
}

/* ─────────────────────────────────────────────
   HELPERS
───────────────────────────────────────────── */
function showMsg(text, type) {
    const el = document.getElementById("regMessage");
    el.textContent   = text;
    el.className     = `ev-reg-msg ev-msg-${type}`;
    el.style.display = "block";
}
function hideMsg() {
    document.getElementById("regMessage").style.display = "none";
}
function fmtDate(dt) {
    if (!dt) return "—";
    return new Date(dt).toLocaleString("es-CO", {
        day:"2-digit", month:"short", year:"numeric",
        hour:"2-digit", minute:"2-digit"
    });
}
function labelType(t) {
    return { ACADEMIC:"Académico", CULTURAL:"Cultural", SPORT:"Deporte",
             VOLUNTEER:"Voluntariado", WORKSHOP:"Taller", OTHER:"Otro" }[t] || t || "Evento";
}
function labelModality(m) {
    return { IN_PERSON:"Presencial", VIRTUAL:"Virtual", HYBRID:"Híbrido" }[m] || m || "—";
}

loadEvent();
