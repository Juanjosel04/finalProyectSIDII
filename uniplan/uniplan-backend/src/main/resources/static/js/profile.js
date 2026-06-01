const token = sessionStorage.getItem("token");
const role  = sessionStorage.getItem("role");

/* Redirigir el logo al home según rol */
const homeMap = { ADMIN: "/admin/home", ORGANIZER: "/organizer/home", STUDENT: "/student/home" };
document.getElementById("homeLink").href = homeMap[role] || "/login";

const ROLE_LABELS = {
    ADMIN:     "Administrador",
    ORGANIZER: "Organizador",
    STUDENT:   "Estudiante"
};

loadProfile();

async function loadProfile() {
    try {
        const res = await fetch("/users/me", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) { sessionStorage.clear(); window.location.href = "/login"; return; }
        if (!res.ok) throw new Error("No se pudo cargar el perfil");

        const u = await res.json();
        render(u);

    } catch (err) {
        document.getElementById("accountFields").innerHTML =
            `<p style="color:#f87171;">${err.message}</p>`;
    }
}

function render(u) {
    const firstName = u.firstName || "";
    const lastName  = u.lastName  || "";
    const fullName  = (firstName + " " + lastName).trim() || u.email;
    const initial   = fullName.charAt(0).toUpperCase();
    const roleLabel = ROLE_LABELS[u.role] || u.role || "—";

    /* Avatar y nombre */
    document.getElementById("bigAvatar").textContent   = initial;
    document.getElementById("fullName").textContent    = fullName;
    document.getElementById("roleLabel").textContent   = roleLabel;
    document.getElementById("navRole").textContent     = roleLabel;

    /* Datos de cuenta */
    const statusLabel = u.status === "ACTIVE" ? "Activa" : u.status === "INACTIVE" ? "Inactiva" : u.status;
    const statusColor = u.status === "ACTIVE" ? "#4ade80" : "#f87171";

    document.getElementById("accountFields").innerHTML = `
        ${field("Correo electrónico", u.email)}
        ${field("Rol",               roleLabel)}
        ${field("Estado de cuenta",  `<span style="color:${statusColor}; font-weight:600;">${statusLabel}</span>`)}
    `;

    /* Datos institucionales */
    const institutionalRows = buildInstitutionalRows(u);
    if (institutionalRows) {
        document.getElementById("institutionalCard").style.display = "block";
        document.getElementById("institutionalFields").innerHTML   = institutionalRows;
    }
}

function buildInstitutionalRows(u) {
    const rows = [];

    if (u.institutionalId) {
        const idLabel = u.role === "STUDENT" ? "Código de estudiante" : "ID de empleado";
        rows.push(field(idLabel, u.institutionalId));
    }

    if (u.firstName || u.lastName) {
        rows.push(field("Nombre completo", `${u.firstName || ""} ${u.lastName || ""}`.trim()));
    }

    if (u.campus)        rows.push(field("Sede / Campus",        u.campus));
    if (u.program)       rows.push(field("Programa académico",   u.program));
    if (u.employeeType)  rows.push(field("Tipo de empleado",     u.employeeType));
    if (u.contractType)  rows.push(field("Tipo de contrato",     u.contractType));

    return rows.join("") || null;
}

function field(label, value) {
    return `
        <div style="display:flex; justify-content:space-between; align-items:flex-start;
                    padding-bottom:.75rem; border-bottom:1px solid rgba(255,255,255,.08);">
            <span style="opacity:.6; font-size:.875rem; min-width:160px;">${label}</span>
            <span style="font-weight:500; text-align:right;">${value || "—"}</span>
        </div>`;
}
