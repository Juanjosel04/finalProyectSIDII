const token = sessionStorage.getItem("token");
let allRegs = [];

loadRegistrations();

async function loadRegistrations() {
    try {
        const res = await fetch("/registrations/organizer", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) { sessionStorage.clear(); window.location.href = "/login"; return; }
        if (!res.ok) throw new Error();
        allRegs = await res.json();
        applyFilter();
    } catch {
        document.getElementById("tableWrap").innerHTML =
            `<p class="ev-empty">No se pudieron cargar las inscripciones.</p>`;
    }
}

function applyFilter() {
    const q      = document.getElementById("searchInput").value.toLowerCase();
    const status = document.getElementById("statusFilter").value;

    const filtered = allRegs.filter(r => {
        const matchStatus = !status || r.status === status;
        const name  = `${r.studentFirstName || ""} ${r.studentLastName || ""}`.toLowerCase();
        const email = (r.studentEmail || "").toLowerCase();
        const title = (r.eventTitle   || "").toLowerCase();
        const matchText = !q || name.includes(q) || email.includes(q) || title.includes(q);
        return matchStatus && matchText;
    });

    document.getElementById("totalCount").textContent =
        `${filtered.length} inscripción${filtered.length !== 1 ? "es" : ""} encontrada${filtered.length !== 1 ? "s" : ""}`;

    renderTable(filtered);
}

function renderTable(regs) {
    const wrap = document.getElementById("tableWrap");
    if (!regs.length) {
        wrap.innerHTML = `<p class="ev-empty">No hay inscripciones que coincidan.</p>`;
        return;
    }

    wrap.innerHTML = `
        <table style="width:100%; border-collapse:collapse; font-size:.85rem;">
            <thead>
                <tr style="border-bottom:1px solid rgba(255,255,255,.15); text-align:left;">
                    <th style="padding:.7rem 1rem; opacity:.7;">Estudiante</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Correo</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Evento</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Inscrito el</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Estado</th>
                </tr>
            </thead>
            <tbody>
                ${regs.map(r => {
                    const name = [r.studentFirstName, r.studentLastName].filter(Boolean).join(" ") || "—";
                    const date = r.registeredAt ? fmtDate(r.registeredAt) : "—";
                    const statusLabel = { REGISTERED: "Inscrito", WAITLIST: "En espera",
                                          CANCELLED: "Cancelado", ATTENDED: "Asistió" }[r.status] || r.status;
                    const badgeCls    = { REGISTERED: "active", WAITLIST: "pending",
                                          CANCELLED: "cancelled", ATTENDED: "active" }[r.status] || "";
                    return `
                    <tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                        <td style="padding:.7rem 1rem;">${name}</td>
                        <td style="padding:.7rem 1rem;">${r.studentEmail || "—"}</td>
                        <td style="padding:.7rem 1rem;">${r.eventTitle || r.eventCode || "—"}</td>
                        <td style="padding:.7rem 1rem; white-space:nowrap;">${date}</td>
                        <td style="padding:.7rem 1rem;">
                            <span class="ev-badge ev-badge-${badgeCls}">${statusLabel}</span>
                        </td>
                    </tr>`;
                }).join("")}
            </tbody>
        </table>`;
}

function fmtDate(dt) {
    return new Date(dt).toLocaleString("es-CO", {
        day:"2-digit", month:"short", year:"numeric",
        hour:"2-digit", minute:"2-digit"
    });
}
