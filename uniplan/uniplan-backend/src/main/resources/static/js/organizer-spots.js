const token = sessionStorage.getItem("token");
let allEvents = [];

loadMyEvents();

async function loadMyEvents() {
    try {
        const res = await fetch("/events/my", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) { sessionStorage.clear(); window.location.href = "/login"; return; }
        if (!res.ok) throw new Error();
        allEvents = await res.json();
        applyFilter();
    } catch {
        document.getElementById("tableWrap").innerHTML =
            `<p class="ev-empty">No se pudieron cargar los eventos.</p>`;
    }
}

function applyFilter() {
    const q      = document.getElementById("searchInput").value.toLowerCase();
    const status = document.getElementById("statusFilter").value;

    const filtered = allEvents.filter(e => {
        const matchStatus = !status || e.status === status;
        const matchText   = !q || (e.title || "").toLowerCase().includes(q);
        return matchStatus && matchText;
    });

    renderTable(filtered);
}

function renderTable(events) {
    const wrap = document.getElementById("tableWrap");
    if (!events.length) {
        wrap.innerHTML = `<p class="ev-empty">No tienes eventos que coincidan.</p>`;
        return;
    }

    wrap.innerHTML = `
        <table style="width:100%; border-collapse:collapse; font-size:.85rem;">
            <thead>
                <tr style="border-bottom:1px solid rgba(255,255,255,.15); text-align:left;">
                    <th style="padding:.7rem 1rem; opacity:.7;">Evento</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Tipo</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Estado</th>
                    <th style="padding:.7rem 1rem; opacity:.7; text-align:center;">Total</th>
                    <th style="padding:.7rem 1rem; opacity:.7; text-align:center;">Inscritos</th>
                    <th style="padding:.7rem 1rem; opacity:.7; text-align:center;">Disponibles</th>
                    <th style="padding:.7rem 1rem; opacity:.7; text-align:center;">Ocupación</th>
                </tr>
            </thead>
            <tbody>
                ${events.map(e => {
                    const total      = e.totalCapacity  ?? 0;
                    const available  = e.availableSpots ?? 0;
                    const registered = total - available;
                    const pct        = total > 0 ? Math.round((registered / total) * 100) : 0;
                    const barColor   = pct >= 90 ? "#f87171" : pct >= 60 ? "#fbbf24" : "#4ade80";
                    const statusLabel = { ACTIVE: "Activo", FINISHED: "Finalizado",
                                          CANCELLED: "Cancelado" }[e.status] || e.status;
                    const badgeCls    = { ACTIVE: "active", FINISHED: "pending",
                                          CANCELLED: "cancelled" }[e.status] || "";
                    const typeLabel   = { ACADEMIC: "Académico", CULTURAL: "Cultural", SPORT: "Deporte",
                                          VOLUNTEER: "Voluntariado", WORKSHOP: "Taller",
                                          OTHER: "Otro" }[e.type] || e.type || "—";
                    return `
                    <tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                        <td style="padding:.7rem 1rem;">
                            <a href="/events/detail?id=${e.id}"
                               style="color:inherit; text-decoration:underline dotted;">${e.title || "—"}</a>
                        </td>
                        <td style="padding:.7rem 1rem;">${typeLabel}</td>
                        <td style="padding:.7rem 1rem;">
                            <span class="ev-badge ev-badge-${badgeCls}">${statusLabel}</span>
                        </td>
                        <td style="padding:.7rem 1rem; text-align:center;">${total || "—"}</td>
                        <td style="padding:.7rem 1rem; text-align:center;">${registered}</td>
                        <td style="padding:.7rem 1rem; text-align:center;">${available}</td>
                        <td style="padding:.7rem 1rem; min-width:120px;">
                            <div style="display:flex; align-items:center; gap:.5rem;">
                                <div style="flex:1; background:rgba(255,255,255,.1); border-radius:4px; height:6px; overflow:hidden;">
                                    <div style="width:${pct}%; background:${barColor}; height:100%; border-radius:4px;"></div>
                                </div>
                                <span style="font-size:.78rem; opacity:.8; min-width:2.5rem; text-align:right;">${pct}%</span>
                            </div>
                        </td>
                    </tr>`;
                }).join("")}
            </tbody>
        </table>`;
}
