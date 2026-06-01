const token = sessionStorage.getItem("token");
let allUsers = [];

const ROLE_LABELS = { ADMIN: "Administrador", ORGANIZER: "Organizador", STUDENT: "Estudiante" };
const ROLE_COLORS = { ADMIN: "#fbbf24", ORGANIZER: "#a78bfa", STUDENT: "#60a5fa" };

loadUsers();

async function loadUsers() {
    try {
        const res = await fetch("/admin/users/all", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) { sessionStorage.clear(); window.location.href = "/login"; return; }
        if (!res.ok) throw new Error();
        allUsers = await res.json();
        updateChips();
        applyFilter();
    } catch {
        document.getElementById("tableWrap").innerHTML =
            `<p class="ev-empty">No se pudieron cargar los usuarios.</p>`;
    }
}

function updateChips() {
    const students   = allUsers.filter(u => u.role === "STUDENT").length;
    const organizers = allUsers.filter(u => u.role === "ORGANIZER").length;
    const admins     = allUsers.filter(u => u.role === "ADMIN").length;

    document.getElementById("chipTotal").textContent      = `Total: ${allUsers.length}`;
    document.getElementById("chipStudents").textContent   = `Estudiantes: ${students}`;
    document.getElementById("chipOrganizers").textContent = `Organizadores: ${organizers}`;
    document.getElementById("chipAdmins").textContent     = `Admins: ${admins}`;
}

function applyFilter() {
    const q      = document.getElementById("searchInput").value.toLowerCase();
    const role   = document.getElementById("roleFilter").value;
    const status = document.getElementById("statusFilter").value;

    const filtered = allUsers.filter(u => {
        const matchRole   = !role   || u.role   === role;
        const matchStatus = !status || u.status === status;
        const name = `${u.firstName || ""} ${u.lastName || ""}`.toLowerCase();
        const matchText = !q
            || u.email.toLowerCase().includes(q)
            || name.includes(q)
            || (u.institutionalId || "").toLowerCase().includes(q);
        return matchRole && matchStatus && matchText;
    });

    document.getElementById("countLabel").textContent =
        `${filtered.length} usuario${filtered.length !== 1 ? "s" : ""} encontrado${filtered.length !== 1 ? "s" : ""}`;

    renderTable(filtered);
}

function renderTable(users) {
    const wrap = document.getElementById("tableWrap");
    if (!users.length) {
        wrap.innerHTML = `<p class="ev-empty">No hay usuarios que coincidan.</p>`;
        return;
    }

    wrap.innerHTML = `
        <table style="width:100%; border-collapse:collapse; font-size:.85rem;">
            <thead>
                <tr style="border-bottom:1px solid rgba(255,255,255,.15); text-align:left;">
                    <th style="padding:.7rem 1rem; opacity:.7;">Nombre completo</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Correo</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Código institucional</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Rol</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Estado</th>
                    <th style="padding:.7rem 1rem; opacity:.7;">Detalle</th>
                </tr>
            </thead>
            <tbody>
                ${users.map(u => {
                    const name   = [u.firstName, u.lastName].filter(Boolean).join(" ") || "—";
                    const roleC  = ROLE_COLORS[u.role] || "inherit";
                    const roleL  = ROLE_LABELS[u.role] || u.role;
                    const statusL = u.status === "ACTIVE" ? "Activo" : "Inactivo";
                    const statusC = u.status === "ACTIVE" ? "#4ade80" : "#f87171";
                    const extra  = u.campus ? ` · ${u.campus}` : u.employeeType ? ` · ${u.employeeType}` : "";
                    return `
                    <tr style="border-bottom:1px solid rgba(255,255,255,.07);"
                        onclick="showDetail('${u.id}')" style="cursor:pointer;">
                        <td style="padding:.7rem 1rem; font-weight:500;">${name}</td>
                        <td style="padding:.7rem 1rem;">${u.email}</td>
                        <td style="padding:.7rem 1rem; font-family:monospace; font-size:.8rem;">
                            ${u.institutionalId || "—"}${extra}
                        </td>
                        <td style="padding:.7rem 1rem;">
                            <span style="color:${roleC}; font-weight:600;">${roleL}</span>
                        </td>
                        <td style="padding:.7rem 1rem;">
                            <span style="color:${statusC}; font-weight:600;">${statusL}</span>
                        </td>
                        <td style="padding:.7rem 1rem;">
                            <button class="add-btn" style="font-size:.75rem; padding:.3rem .7rem;"
                                onclick="showDetail('${u.id}')">Ver</button>
                        </td>
                    </tr>`;
                }).join("")}
            </tbody>
        </table>`;
}

/* ── Modal de detalle ── */
function showDetail(userId) {
    const u = allUsers.find(x => x.id === userId);
    if (!u) return;

    const name    = [u.firstName, u.lastName].filter(Boolean).join(" ") || "Sin nombre";
    const initial = name.charAt(0).toUpperCase();
    const roleL   = ROLE_LABELS[u.role]   || u.role;
    const roleC   = ROLE_COLORS[u.role]   || "#fff";
    const statusL = u.status === "ACTIVE" ? "Activo"  : "Inactivo";
    const statusC = u.status === "ACTIVE" ? "#4ade80" : "#f87171";

    const idLabel = u.role === "STUDENT" ? "Código de estudiante" : "ID de empleado";

    let extra = "";
    if (u.campus)        extra += row("Sede / Campus",      u.campus);
    if (u.program)       extra += row("Programa",           u.program);
    if (u.employeeType)  extra += row("Tipo de empleado",   u.employeeType);
    if (u.contractType)  extra += row("Tipo de contrato",   u.contractType);

    const html = `
    <div id="userModal" onclick="closeModal(event)"
         style="position:fixed;inset:0;background:rgba(0,0,0,.65);display:flex;
                align-items:center;justify-content:center;z-index:9999;padding:1rem;">
        <div onclick="event.stopPropagation()"
             style="background:#1e1b2e;border:1px solid rgba(255,255,255,.12);border-radius:16px;
                    padding:2rem;max-width:480px;width:100%;box-shadow:0 20px 60px rgba(0,0,0,.5);">

            <div style="display:flex;align-items:center;gap:1.25rem;margin-bottom:1.5rem;">
                <div style="width:60px;height:60px;border-radius:50%;
                            background:linear-gradient(135deg,#7c3aed,#4f46e5);
                            display:flex;align-items:center;justify-content:center;
                            font-size:1.6rem;font-weight:700;flex-shrink:0;">${initial}</div>
                <div>
                    <h2 style="margin:0;font-size:1.15rem;">${name}</h2>
                    <span style="color:${roleC};font-size:.85rem;font-weight:600;">${roleL}</span>
                </div>
            </div>

            <div style="display:flex;flex-direction:column;gap:.65rem;">
                ${row("Correo",          u.email)}
                ${row("Estado",          `<span style="color:${statusC};font-weight:600;">${statusL}</span>`)}
                ${u.institutionalId ? row(idLabel, `<code style="font-size:.85rem;">${u.institutionalId}</code>`) : ""}
                ${extra}
                ${row("ID interno",      `<code style="font-size:.75rem;opacity:.5;">${u.id}</code>`)}
            </div>

            <div style="margin-top:1.5rem;text-align:right;">
                <button class="logout-btn" onclick="closeModal()">Cerrar</button>
            </div>
        </div>
    </div>`;

    document.body.insertAdjacentHTML("beforeend", html);
}

function closeModal(e) {
    if (e && e.target !== document.getElementById("userModal")) return;
    const m = document.getElementById("userModal");
    if (m) m.remove();
}

function row(label, value) {
    return `<div style="display:flex;justify-content:space-between;align-items:flex-start;
                         padding-bottom:.6rem;border-bottom:1px solid rgba(255,255,255,.07);">
                <span style="opacity:.6;font-size:.82rem;min-width:150px;">${label}</span>
                <span style="font-weight:500;text-align:right;font-size:.85rem;">${value || "—"}</span>
            </div>`;
}
