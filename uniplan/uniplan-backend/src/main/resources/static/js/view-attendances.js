const token = sessionStorage.getItem("token");
const role  = sessionStorage.getItem("role");

const homeMap       = { ADMIN: "/admin/home",       ORGANIZER: "/organizer/home" };
const attendanceMap = { ADMIN: "/admin/attendance",  ORGANIZER: "/organizer/attendance" };
const eventsUrl     = role === "ORGANIZER" ? "/events/my" : "/events";

document.getElementById("homeLink").href       = homeMap[role]       || "/login";
document.getElementById("attendanceLink").href = attendanceMap[role] || "#";

let allAttended = [];

loadEvents();

async function loadEvents() {
    const select = document.getElementById("eventSelect");
    try {
        const res = await fetch(eventsUrl, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) { sessionStorage.clear(); window.location.href = "/login"; return; }
        if (!res.ok) throw new Error();
        const events = await res.json();

        if (!events.length) {
            select.innerHTML = `<option value="">No hay eventos disponibles</option>`;
            return;
        }

        select.innerHTML = `<option value="">— Selecciona un evento —</option>` +
            events.map(e => {
                const label = { ACTIVE:"Activo", FINISHED:"Finalizado", CANCELLED:"Cancelado" }[e.status] || e.status;
                return `<option value="${e.id}">[${label}] ${e.title || e.id}</option>`;
            }).join("");

        // Si viene eventId por URL, seleccionarlo automáticamente
        const urlEventId = new URLSearchParams(window.location.search).get("eventId");
        if (urlEventId) {
            select.value = urlEventId;
            loadAttendances();
        }
    } catch {
        select.innerHTML = `<option value="">Error al cargar eventos</option>`;
    }
}

async function loadAttendances() {
    const eventId = document.getElementById("eventSelect").value;
    const wrap    = document.getElementById("tableWrap");
    const count   = document.getElementById("countLabel");

    if (!eventId) {
        allAttended = [];
        count.textContent = "";
        wrap.innerHTML = `<p class="ev-empty">Selecciona un evento para ver sus asistencias.</p>`;
        return;
    }

    wrap.innerHTML = `<p class="ev-empty">Cargando asistencias...</p>`;

    try {
        const res = await fetch(`/registrations/event/${eventId}/attended`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();
        allAttended = await res.json();
        applyFilter();
    } catch {
        wrap.innerHTML = `<p class="ev-empty">No se pudieron cargar las asistencias.</p>`;
    }
}

function applyFilter() {
    const q = document.getElementById("searchInput").value.toLowerCase();
    const filtered = q
        ? allAttended.filter(r => {
            const name  = `${r.studentFirstName || ""} ${r.studentLastName || ""}`.toLowerCase();
            const code  = (r.studentId    || "").toLowerCase();
            const email = (r.studentEmail || "").toLowerCase();
            return name.includes(q) || code.includes(q) || email.includes(q);
          })
        : allAttended;

    const count = document.getElementById("countLabel");
    count.textContent = filtered.length
        ? `${filtered.length} persona${filtered.length !== 1 ? "s" : ""} asistida${filtered.length !== 1 ? "s" : ""}`
        : "Ningún asistente encontrado";

    renderTable(filtered);
}

function renderTable(regs) {
    const wrap = document.getElementById("tableWrap");
    if (!regs.length) {
        wrap.innerHTML = `<p class="ev-empty">${
            allAttended.length ? "No hay asistentes que coincidan con la búsqueda."
                              : "Nadie ha asistido a este evento aún."
        }</p>`;
        return;
    }

    wrap.innerHTML = `
        <table style="width:100%; border-collapse:collapse; font-size:.88rem;">
            <thead>
                <tr style="border-bottom:1px solid rgba(255,255,255,.15); text-align:left;">
                    <th style="padding:.7rem 1rem; opacity:.7;">#</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Nombre completo</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Código</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Correo</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Asistió el</th>
                </tr>
            </thead>
            <tbody>
                ${regs.map((r, i) => {
                    const name = [r.studentFirstName, r.studentLastName].filter(Boolean).join(" ") || "—";
                    const date = r.attendedAt ? fmtDate(r.attendedAt) : "—";
                    return `
                    <tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                        <td style="padding:.7rem 1rem; opacity:.5; font-size:.8rem;">${i + 1}</td>
                        <td style="padding:.7rem 1rem; font-weight:500;">${name}</td>
                        <td style="padding:.7rem 1rem; font-family:monospace; font-size:.82rem;">
                            ${r.studentId || "—"}
                        </td>
                        <td style="padding:.7rem 1rem;">${r.studentEmail || "—"}</td>
                        <td style="padding:.7rem 1rem; opacity:.75; white-space:nowrap;">${date}</td>
                    </tr>`;
                }).join("")}
            </tbody>
        </table>`;
}

function fmtDate(dt) {
    return new Date(dt).toLocaleString("es-CO", {
        day: "2-digit", month: "short", year: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}
