const token = sessionStorage.getItem("token");
let allOrganizers = [];

loadOrganizers();

async function loadOrganizers() {
    try {
        const res = await fetch("/admin/users/organizers", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) { sessionStorage.clear(); window.location.href = "/login"; return; }
        if (!res.ok) throw new Error();
        allOrganizers = await res.json();
        renderTable(allOrganizers);
    } catch {
        document.getElementById("orgTableWrap").innerHTML =
            `<p class="ev-empty">No se pudieron cargar los organizadores.</p>`;
    }
}

function applyFilter() {
    const q      = document.getElementById("searchInput").value.toLowerCase();
    const status = document.getElementById("statusFilter").value;
    const filtered = allOrganizers.filter(o => {
        const matchText   = !q || o.email.toLowerCase().includes(q) || (o.employeeId || "").toLowerCase().includes(q);
        const matchStatus = !status || o.status === status;
        return matchText && matchStatus;
    });
    renderTable(filtered);
}

function renderTable(organizers) {
    const wrap = document.getElementById("orgTableWrap");
    if (!organizers.length) {
        wrap.innerHTML = `<p class="ev-empty">No hay organizadores que coincidan.</p>`;
        return;
    }

    wrap.innerHTML = `
        <table style="width:100%; border-collapse:collapse; font-size:.875rem;">
            <thead>
                <tr style="border-bottom:1px solid rgba(255,255,255,.15); text-align:left;">
                    <th style="padding:.75rem 1rem; opacity:.7;">Correo</th>
                    <th style="padding:.75rem 1rem; opacity:.7;">ID Empleado</th>
                    <th style="padding:.75rem 1rem; opacity:.7;">Estado</th>
                    <th style="padding:.75rem 1rem; opacity:.7;">Acciones</th>
                </tr>
            </thead>
            <tbody>
                ${organizers.map(o => `
                <tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                    <td style="padding:.75rem 1rem;">${o.email}</td>
                    <td style="padding:.75rem 1rem;">${o.employeeId || "—"}</td>
                    <td style="padding:.75rem 1rem;">
                        <span class="ev-badge ev-badge-${o.status === "ACTIVE" ? "active" : "cancelled"}">
                            ${o.status === "ACTIVE" ? "Activo" : "Inactivo"}
                        </span>
                    </td>
                    <td style="padding:.75rem 1rem;">
                        ${o.status === "ACTIVE"
                            ? `<button class="logout-btn" style="font-size:.8rem; padding:.35rem .85rem;" onclick="toggleStatus('${o.id}', 'deactivate')">Desactivar</button>`
                            : `<button class="add-btn"    style="font-size:.8rem; padding:.35rem .85rem;" onclick="toggleStatus('${o.id}', 'activate')">Activar</button>`
                        }
                    </td>
                </tr>`).join("")}
            </tbody>
        </table>`;
}

async function toggleStatus(id, action) {
    try {
        const res = await fetch(`/admin/users/${id}/${action}`, {
            method: "PATCH",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();
        const updated = await res.json();
        allOrganizers = allOrganizers.map(o => o.id === id ? updated : o);
        applyFilter();
    } catch {
        alert("No se pudo actualizar el estado del organizador.");
    }
}
