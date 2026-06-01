const token = sessionStorage.getItem("token");

loadParticipation();

async function loadParticipation() {
    try {
        const res = await fetch("/reports/my-participation", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) { sessionStorage.clear(); window.location.href = "/login"; return; }
        if (!res.ok) throw new Error("No se pudo cargar tu reporte");
        const d = await res.json();
        render(d);
    } catch (err) {
        document.getElementById("regsWrap").innerHTML =
            `<p style="color:#f87171;">${err.message}</p>`;
    }
}

function render(d) {
    document.getElementById("sRegistered").textContent = d.totalRegistered ?? 0;
    document.getElementById("sAttended").textContent   = d.totalAttended   ?? 0;
    document.getElementById("sCancelled").textContent  = d.totalCancelled  ?? 0;
    document.getElementById("sScore").textContent      = d.participationScore ?? 0;

    const regs = d.recentRegistrations || [];
    if (!regs.length) {
        document.getElementById("regsWrap").innerHTML =
            `<p class="ev-empty">Aún no tienes inscripciones.</p>`;
        return;
    }

    const STATUS = { REGISTERED:"Inscrito", ATTENDED:"Asistió", CANCELLED:"Cancelado",
                     WAITLIST:"En espera" };
    const BADGE  = { REGISTERED:"active", ATTENDED:"active", CANCELLED:"cancelled",
                     WAITLIST:"pending" };

    document.getElementById("regsWrap").innerHTML = `
        <table style="width:100%;border-collapse:collapse;font-size:.85rem;">
          <thead><tr style="border-bottom:1px solid rgba(255,255,255,.15);text-align:left;">
            <th style="padding:.6rem .9rem;opacity:.7;">Código Evento</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Estado</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Fecha inscripción</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Fecha asistencia</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Detalle</th>
          </tr></thead>
          <tbody>
          ${regs.map(r => {
              const label = STATUS[r.status] || r.status;
              const cls   = BADGE[r.status]  || "";
              return `<tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                <td style="padding:.6rem .9rem;">${r.eventCode || r.eventId || "—"}</td>
                <td style="padding:.6rem .9rem;"><span class="ev-badge ev-badge-${cls}">${label}</span></td>
                <td style="padding:.6rem .9rem;">${fmtDate(r.registeredAt)}</td>
                <td style="padding:.6rem .9rem;">${r.attendedAt ? fmtDate(r.attendedAt) : "—"}</td>
                <td style="padding:.6rem .9rem;">
                  <a href="/events/detail?id=${r.eventId}"
                     style="color:#a78bfa;font-size:.8rem;">Ver →</a>
                </td>
              </tr>`;
          }).join("")}
          </tbody>
        </table>`;
}

function fmtDate(dt) {
    if (!dt) return "—";
    return new Date(dt).toLocaleString("es-CO", {
        day:"2-digit", month:"short", year:"numeric"
    });
}
