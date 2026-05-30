/* =========================================================
   STUDENT HOME — eventos y mis inscripciones
   IDs: eventsGrid, myRegsList
   Clases CSS: ev-chip, ev-card, ev-badge, ev-my-reg (de events-style.css)
========================================================= */

const token = sessionStorage.getItem("token");
let allEvents  = [];
let myRegs     = [];
let activeType = "";
let searchQ    = "";

loadEvents();
loadMyRegistrations();

/* ─────────────────────────────────────────────
   EVENTOS
───────────────────────────────────────────── */
async function loadEvents() {
    try {
        const res = await fetch("/events", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();
        allEvents = await res.json();
        renderEvents(allEvents);
    } catch {
        document.getElementById("eventsGrid").innerHTML =
            `<p class="ev-empty">No se pudieron cargar los eventos.</p>`;
    }
}

function renderEvents(events) {
    const grid   = document.getElementById("eventsGrid");
    const active = events.filter(e => e.status === "ACTIVE");

    if (!active.length) {
        grid.innerHTML = `<p class="ev-empty">No hay eventos disponibles.</p>`;
        return;
    }

    grid.innerHTML = active.map(e => {
        const available = e.availableSpots ?? "—";
        const total     = e.totalCapacity  ?? "—";
        const dateStr   = e.startDate ? fmtDate(e.startDate) : "Fecha por confirmar";
        const venue     = e.venue || e.modality || "—";

        return `
            <a class="ev-card" href="/events/detail?id=${e.id}">
                <div class="ev-card-type">${labelType(e.type)}</div>
                <div class="ev-card-title">${e.title || "Sin título"}</div>
                <div class="ev-card-meta">📅 ${dateStr}</div>
                <div class="ev-card-meta">📍 ${venue}</div>
                <div class="ev-card-footer">
                    <span class="ev-card-spots-label">Cupos disponibles</span>
                    <span class="ev-card-spots-num">${available} / ${total}</span>
                </div>
            </a>
        `;
    }).join("");
}

/* ─────────────────────────────────────────────
   FILTROS
───────────────────────────────────────────── */
function setTypeFilter(type, btn) {
    activeType = type;
    document.querySelectorAll(".ev-chip").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    applyFilters();
}

function filterBySearch(query) {
    searchQ = query.toLowerCase();
    applyFilters();
}

function applyFilters() {
    let filtered = allEvents;
    if (activeType) filtered = filtered.filter(e => e.type === activeType);
    if (searchQ)    filtered = filtered.filter(e =>
        (e.title && e.title.toLowerCase().includes(searchQ)) ||
        (e.venue && e.venue.toLowerCase().includes(searchQ))
    );
    renderEvents(filtered);
}

/* ─────────────────────────────────────────────
   MIS INSCRIPCIONES
───────────────────────────────────────────── */
async function loadMyRegistrations() {
    const container = document.getElementById("myRegsList");
    try {
        const res = await fetch("/registrations/my", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();
        myRegs = await res.json();
        renderMyRegs();
    } catch {
        container.innerHTML =
            `<p class="ev-empty" style="font-size:0.83rem;">No se pudieron cargar tus inscripciones.</p>`;
    }
}

function renderMyRegs() {
    const container = document.getElementById("myRegsList");
    const active    = myRegs.filter(r => r.status !== "CANCELLED");

    if (!active.length) {
        container.innerHTML =
            `<p class="ev-empty" style="font-size:0.83rem;">Aún no tienes inscripciones activas.</p>`;
        return;
    }

    container.innerHTML = active.map(r => `
        <div class="ev-my-reg">
            <div>
                <div class="ev-my-reg-title">${r.eventTitle || r.eventId || "Evento"}</div>
                <div class="ev-my-reg-date">${r.registeredAt ? fmtDate(r.registeredAt) : ""}</div>
            </div>
            <div style="display:flex; align-items:center; gap:0.65rem;">
                <span class="ev-badge ev-badge-${(r.status||"").toLowerCase()}">${r.status}</span>
                <a class="ev-my-reg-link" href="/events/detail?id=${r.eventId}">Ver →</a>
            </div>
        </div>
    `).join("");
}

/* ─────────────────────────────────────────────
   HELPERS
───────────────────────────────────────────── */
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
