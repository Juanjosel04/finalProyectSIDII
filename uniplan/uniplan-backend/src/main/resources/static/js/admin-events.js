/* =========================================================
   ADMIN EVENTS — listado, filtros, cancelar
   IDs: searchInput, statusFilter, typeFilter, eventsBody
========================================================= */

const token = sessionStorage.getItem("token");
let allEvents = [];

loadEvents();

async function loadEvents() {
    try {
        const res = await fetch("/events", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.error || "Error " + res.status);
        }
        allEvents = await res.json();
        renderTable(allEvents);
    } catch (err) {
        console.error("loadEvents:", err);
        document.getElementById("eventsBody").innerHTML =
            `<tr><td colspan="8" class="ev-empty" style="color:#fca5a5;">${err.message}</td></tr>`;
    }
}

function renderTable(events) {
    const tbody = document.getElementById("eventsBody");
    if (!events.length) {
        tbody.innerHTML = `<tr><td colspan="8" class="ev-empty">No hay eventos que coincidan.</td></tr>`;
        return;
    }
    tbody.innerHTML = events.map(e => `
        <tr>
            <td><strong>${e.title || "—"}</strong></td>
            <td>${labelType(e.type)}</td>
            <td>${labelModality(e.modality)}</td>
            <td>${e.venue || "—"}</td>
            <td>${e.startDate ? fmtDate(e.startDate) : "—"}</td>
            <td>${e.availableSpots ?? "—"} / ${e.totalCapacity ?? "—"}</td>
            <td><span class="ev-badge ev-badge-${(e.status||"").toLowerCase()}">${e.status || "—"}</span></td>
            <td style="display:flex; gap:0.4rem; flex-wrap:wrap; align-items:center;">
                <button class="ev-btn ev-btn-view"
                        onclick="location.href='/events/detail?id=${e.id}'">Ver</button>
                <button class="ev-btn ev-btn-edit"
                        onclick="location.href='/admin/events/edit?id=${e.id}'">Editar</button>
                ${e.status === "ACTIVE"
                    ? `<button class="ev-btn ev-btn-cancel" onclick="cancelEvent('${e.id}', this)">Cancelar</button>`
                    : ""}
            </td>
        </tr>
    `).join("");
}

function applyFilters() {
    const q        = document.getElementById("searchInput").value.toLowerCase();
    const status   = document.getElementById("statusFilter").value;
    const type     = document.getElementById("typeFilter").value;
    const dateFrom = document.getElementById("dateFrom").value;
    const dateTo   = document.getElementById("dateTo").value;

    const filtered = allEvents.filter(e => {
        const matchQ = !q ||
            (e.title  && e.title.toLowerCase().includes(q))  ||
            (e.venue  && e.venue.toLowerCase().includes(q))  ||
            (e.type   && e.type.toLowerCase().includes(q));
        const matchStatus = !status || e.status === status;
        const matchType   = !type   || e.type   === type;

        let matchDate = true;
        if (e.startDate) {
            const d = new Date(e.startDate);
            if (dateFrom && d < new Date(dateFrom)) matchDate = false;
            if (dateTo   && d > new Date(dateTo + "T23:59:59")) matchDate = false;
        } else if (dateFrom || dateTo) {
            matchDate = false;
        }

        return matchQ && matchStatus && matchType && matchDate;
    });
    renderTable(filtered);
}

async function cancelEvent(id, btn) {
    if (!confirm("¿Cancelar este evento?")) return;
    btn.disabled = true; btn.textContent = "...";
    try {
        const res = await fetch(`/events/${id}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();
        await loadEvents();
    } catch {
        btn.disabled = false; btn.textContent = "Cancelar";
        alert("No se pudo cancelar el evento.");
    }
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
             VOLUNTEER:"Voluntariado", WORKSHOP:"Taller", OTHER:"Otro" }[t] || t || "—";
}
function labelModality(m) {
    return { IN_PERSON:"Presencial", VIRTUAL:"Virtual", HYBRID:"Híbrido" }[m] || m || "—";
}
