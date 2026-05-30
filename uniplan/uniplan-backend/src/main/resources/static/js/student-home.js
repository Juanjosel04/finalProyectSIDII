/* =========================================================
   STUDENT HOME — carga eventos y mis inscripciones
========================================================= */

const token = sessionStorage.getItem("token");

let allEvents     = [];
let myRegs        = [];
let activeType    = "";
let searchQuery   = "";

/* ── Iniciar ── */
loadEvents();
loadMyRegistrations();

/* ─────────────────────────────────────────────
   LOAD EVENTS
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
            `<p style="color:rgba(255,255,255,0.3);">No se pudieron cargar los eventos.</p>`;
    }
}

/* ─────────────────────────────────────────────
   RENDER EVENTS
───────────────────────────────────────────── */
function renderEvents(events) {
    const grid = document.getElementById("eventsGrid");

    const active = events.filter(e => e.status === "ACTIVE");

    if (!active.length) {
        grid.innerHTML = `<p style="color:rgba(255,255,255,0.3); padding:1rem;">No hay eventos disponibles.</p>`;
        return;
    }

    grid.innerHTML = active.map(e => {
        const available = e.availableSpots ?? "—";
        const total     = e.totalCapacity  ?? "—";
        const dateStr   = e.startDate ? formatDate(e.startDate) : "Fecha por confirmar";
        const venue     = e.venue || e.modality || "—";

        return `
            <a class="event-card" href="/events/detail?id=${e.id}">
                <div class="event-card-type">${labelType(e.type)}</div>
                <div class="event-card-title">${e.title || "Sin título"}</div>
                <div class="event-card-meta">📅 ${dateStr}</div>
                <div class="event-card-meta">📍 ${venue}</div>
                <div class="event-card-spots">
                    <span class="spots-text">Cupos disponibles</span>
                    <span class="spots-num">${available} / ${total}</span>
                </div>
            </a>
        `;
    }).join("");
}

/* ─────────────────────────────────────────────
   FILTER EVENTS
───────────────────────────────────────────── */
function setTypeFilter(type, btn) {
    activeType = type;
    document.querySelectorAll(".filter-chip").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    applyFilters();
}

function filterEvents(query) {
    searchQuery = query.toLowerCase();
    applyFilters();
}

function applyFilters() {
    let filtered = allEvents;
    if (activeType) {
        filtered = filtered.filter(e => e.type === activeType);
    }
    if (searchQuery) {
        filtered = filtered.filter(e =>
            (e.title && e.title.toLowerCase().includes(searchQuery)) ||
            (e.venue && e.venue.toLowerCase().includes(searchQuery)) ||
            (e.type  && e.type.toLowerCase().includes(searchQuery))
        );
    }
    renderEvents(filtered);
}

/* ─────────────────────────────────────────────
   LOAD MY REGISTRATIONS
───────────────────────────────────────────── */
async function loadMyRegistrations() {
    const container = document.getElementById("myRegsList");
    try {
        const res = await fetch("/registrations/my", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();
        myRegs = await res.json();
        renderMyRegistrations();
    } catch {
        container.innerHTML = `<p style="color:rgba(255,255,255,0.3); font-size:0.85rem;">No se pudieron cargar tus inscripciones.</p>`;
    }
}

function renderMyRegistrations() {
    const container = document.getElementById("myRegsList");

    if (!myRegs.length) {
        container.innerHTML = `<p style="color:rgba(255,255,255,0.3); font-size:0.85rem; padding:0.5rem 0;">Aún no tienes inscripciones.</p>`;
        return;
    }

    const active = myRegs.filter(r => r.status !== "CANCELLED");

    if (!active.length) {
        container.innerHTML = `<p style="color:rgba(255,255,255,0.3); font-size:0.85rem; padding:0.5rem 0;">No tienes inscripciones activas.</p>`;
        return;
    }

    container.innerHTML = active.map(r => `
        <div class="my-reg-item">
            <div>
                <div style="font-weight:600; font-size:0.9rem; margin-bottom:0.2rem;">
                    ${r.eventTitle || r.eventId || "Evento"}
                </div>
                <div style="font-size:0.75rem; color:rgba(255,255,255,0.35);">
                    ${r.registeredAt ? formatDate(r.registeredAt) : ""}
                </div>
            </div>
            <div style="display:flex; align-items:center; gap:0.75rem;">
                <span class="reg-status-chip chip-${(r.status||"").toLowerCase()}">${r.status}</span>
                <a href="/events/detail?id=${r.eventId}"
                   style="font-size:0.78rem; color:#a78bfa; text-decoration:none;">
                    Ver →
                </a>
            </div>
        </div>
    `).join("");
}

/* ─────────────────────────────────────────────
   HELPERS
───────────────────────────────────────────── */
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
