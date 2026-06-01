const token = sessionStorage.getItem("token");
const hdrs  = { "Authorization": `Bearer ${token}` };

const TYPE_LABELS   = { ACADEMIC:"Académico", CULTURAL:"Cultural", SPORT:"Deporte",
                        VOLUNTEER:"Voluntariado", WORKSHOP:"Taller", OTHER:"Otro" };
const STATUS_LABELS = { ACTIVE:"Activo", FINISHED:"Finalizado", CANCELLED:"Cancelado" };

let loaded = { events: false, organizers: false, analytics: false, audit: false };

loadSummary();
loadEvents();

/* ── Tab navigation ── */
function showTab(name, btn) {
    ["events","organizers","analytics","audit"].forEach(t => {
        document.getElementById("tab-" + t).style.display = t === name ? "block" : "none";
    });
    document.querySelectorAll(".ev-chip").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    if (!loaded[name]) {
        if (name === "organizers") loadOrganizers();
        if (name === "analytics")  loadAnalytics();
        if (name === "audit")      loadAudit();
    }
}

/* ── Summary cards ── */
async function loadSummary() {
    try {
        const res = await fetch("/reports/summary", { headers: hdrs });
        if (!res.ok) return;
        const d = await res.json();
        document.getElementById("sTotal").textContent        = d.totalUsers        ?? "—";
        document.getElementById("sActiveEvents").textContent = d.activeEvents       ?? "—";
        document.getElementById("sRegs").textContent         = d.totalRegistrations ?? "—";
        document.getElementById("sAudit").textContent        = d.totalAuditLogs     ?? "—";
    } catch {}
}

/* ── Events tab ── */
async function loadEvents() {
    try {
        const res = await fetch("/reports/events", { headers: hdrs });
        if (!res.ok) throw new Error();
        const data = await res.json();
        loaded.events = true;
        document.getElementById("eventsLoading").style.display = "none";
        if (!data.length) { document.getElementById("eventsTable").innerHTML = `<p class="ev-empty">Sin datos.</p>`; return; }

        document.getElementById("eventsTable").innerHTML = `
        <table style="width:100%;border-collapse:collapse;font-size:.83rem;">
          <thead><tr style="border-bottom:1px solid rgba(255,255,255,.15);text-align:left;">
            <th style="padding:.6rem .9rem;opacity:.7;">Evento</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Tipo</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Estado</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Cap.</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Inscritos</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Asistidos</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Espera</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Cancelados</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Tasa Asist.</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Asistencia CSV</th>
          </tr></thead>
          <tbody>
          ${data.map(e => {
              const rate  = e.attendanceRate != null ? e.attendanceRate.toFixed(1) + "%" : "—";
              const rateC = e.attendanceRate >= 75 ? "#4ade80" : e.attendanceRate >= 40 ? "#fbbf24" : "#f87171";
              const bc    = {ACTIVE:"active",FINISHED:"pending",CANCELLED:"cancelled"}[e.eventStatus]||"";
              return `<tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                <td style="padding:.6rem .9rem;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"
                    title="${e.eventTitle||''}">${e.eventTitle||"—"}</td>
                <td style="padding:.6rem .9rem;">${TYPE_LABELS[e.eventType]||e.eventType||"—"}</td>
                <td style="padding:.6rem .9rem;"><span class="ev-badge ev-badge-${bc}">${STATUS_LABELS[e.eventStatus]||e.eventStatus||"—"}</span></td>
                <td style="padding:.6rem .9rem;text-align:center;">${e.totalCapacity??'—'}</td>
                <td style="padding:.6rem .9rem;text-align:center;">${e.registered??0}</td>
                <td style="padding:.6rem .9rem;text-align:center;">${e.attended??0}</td>
                <td style="padding:.6rem .9rem;text-align:center;">${e.waitlist??0}</td>
                <td style="padding:.6rem .9rem;text-align:center;">${e.cancelled??0}</td>
                <td style="padding:.6rem .9rem;text-align:center;font-weight:600;color:${rateC};">${rate}</td>
                <td style="padding:.6rem .9rem;text-align:center;">
                  <button class="logout-btn" style="font-size:.75rem;padding:.25rem .6rem;"
                    onclick="downloadCsv('/reports/attendance/export?eventId=${e.eventId}','asistencia-${(e.eventCode||e.eventId||'evt').replace(/[^a-z0-9]/gi,'-')}.csv')">⬇</button>
                </td>
              </tr>`;
          }).join("")}
          </tbody>
        </table>`;
    } catch { document.getElementById("eventsLoading").textContent = "Error al cargar reporte."; }
}

/* ── Organizers tab ── */
async function loadOrganizers() {
    try {
        const res = await fetch("/reports/organizers", { headers: hdrs });
        if (!res.ok) throw new Error();
        const data = await res.json();
        loaded.organizers = true;
        document.getElementById("orgsLoading").style.display = "none";
        if (!data.length) { document.getElementById("orgsTable").innerHTML = `<p class="ev-empty">Sin organizadores.</p>`; return; }

        document.getElementById("orgsTable").innerHTML = `
        <table style="width:100%;border-collapse:collapse;font-size:.83rem;">
          <thead><tr style="border-bottom:1px solid rgba(255,255,255,.15);text-align:left;">
            <th style="padding:.6rem .9rem;opacity:.7;">Nombre</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Correo</th>
            <th style="padding:.6rem .9rem;opacity:.7;">ID Empleado</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Eventos</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Inscritos</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Asistidos</th>
            <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Cancelados</th>
          </tr></thead>
          <tbody>
          ${data.map(o => {
              const name = [o.firstName, o.lastName].filter(Boolean).join(" ") || "—";
              return `<tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                <td style="padding:.6rem .9rem;">${name}</td>
                <td style="padding:.6rem .9rem;">${o.email||"—"}</td>
                <td style="padding:.6rem .9rem;">${o.employeeId||"—"}</td>
                <td style="padding:.6rem .9rem;text-align:center;font-weight:600;">${o.eventsCount??0}</td>
                <td style="padding:.6rem .9rem;text-align:center;">${o.totalRegistered??0}</td>
                <td style="padding:.6rem .9rem;text-align:center;color:#4ade80;">${o.totalAttended??0}</td>
                <td style="padding:.6rem .9rem;text-align:center;color:#f87171;">${o.totalCancelled??0}</td>
              </tr>`;
          }).join("")}
          </tbody>
        </table>`;
    } catch { document.getElementById("orgsLoading").textContent = "Error al cargar reporte."; }
}

/* ── Analytics tab — REPORTE INNOVADOR (PostgreSQL event_statistics) ── */
async function loadAnalytics() {
    try {
        const res = await fetch("/reports/analytics", { headers: hdrs });
        if (!res.ok) throw new Error();
        const d = await res.json();
        loaded.analytics = true;
        document.getElementById("analyticsLoading").style.display = "none";

        let html = "";

        /* By type */
        html += `<h3 style="margin:0 0 .75rem;font-size:.95rem;opacity:.75;text-transform:uppercase;letter-spacing:.04em;">
                   Demanda por tipo de evento <span style="font-size:.75rem;opacity:.5;">(fuente: PostgreSQL · event_statistics)</span></h3>`;
        if (d.byType && d.byType.length) {
            html += `<div style="overflow-x:auto;margin-bottom:1.75rem;">
            <table style="width:100%;border-collapse:collapse;font-size:.83rem;">
              <thead><tr style="border-bottom:1px solid rgba(255,255,255,.15);text-align:left;">
                <th style="padding:.6rem .9rem;opacity:.7;">Tipo</th>
                <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Eventos</th>
                <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Total inscritos</th>
                <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Total asistidos</th>
                <th style="padding:.6rem .9rem;opacity:.7;">Avg ocupación %</th>
                <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Avg asistencia %</th>
                <th style="padding:.6rem .9rem;opacity:.7;">Nivel demanda</th>
              </tr></thead>
              <tbody>
              ${d.byType.map(t => {
                  const occ = t.avgOccupancy ?? 0;
                  const att = t.avgAttendanceRate ?? 0;
                  const occC = occ >= 70 ? "#4ade80" : occ >= 40 ? "#fbbf24" : "#f87171";
                  const attC = att >= 70 ? "#4ade80" : att >= 40 ? "#fbbf24" : "#f87171";
                  const bar  = `<div style="width:${Math.min(occ,100)}%;background:${occC};height:5px;border-radius:3px;"></div>`;
                  const demanda = occ >= 80 ? `<span class="ev-badge ev-badge-cancelled">Alta</span>`
                                : occ >= 40 ? `<span class="ev-badge ev-badge-pending">Media</span>`
                                : `<span style="opacity:.45;font-size:.78rem;">Baja</span>`;
                  return `<tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                    <td style="padding:.6rem .9rem;font-weight:600;">${TYPE_LABELS[t.type]||t.type}</td>
                    <td style="padding:.6rem .9rem;text-align:center;">${t.totalEvents}</td>
                    <td style="padding:.6rem .9rem;text-align:center;">${t.totalRegistered}</td>
                    <td style="padding:.6rem .9rem;text-align:center;color:#4ade80;">${t.totalAttended}</td>
                    <td style="padding:.6rem .9rem;">
                      <div style="display:flex;align-items:center;gap:.5rem;">
                        <div style="flex:1;background:rgba(255,255,255,.08);border-radius:3px;height:5px;">${bar}</div>
                        <span style="font-size:.78rem;min-width:2.8rem;color:${occC};">${occ.toFixed(1)}%</span>
                      </div>
                    </td>
                    <td style="padding:.6rem .9rem;text-align:center;color:${attC};font-weight:600;">${att.toFixed(1)}%</td>
                    <td style="padding:.6rem .9rem;">${demanda}</td>
                  </tr>`;
              }).join("")}
              </tbody>
            </table></div>`;
        }

        /* Top events */
        if (d.topEvents && d.topEvents.length) {
            html += `<h3 style="margin:0 0 .75rem;font-size:.95rem;opacity:.75;text-transform:uppercase;letter-spacing:.04em;">Top 5 eventos más populares</h3>
            <div style="overflow-x:auto;margin-bottom:1.75rem;">
            <table style="width:100%;border-collapse:collapse;font-size:.83rem;">
              <thead><tr style="border-bottom:1px solid rgba(255,255,255,.15);text-align:left;">
                <th style="padding:.6rem .9rem;opacity:.7;">#</th>
                <th style="padding:.6rem .9rem;opacity:.7;">Evento</th>
                <th style="padding:.6rem .9rem;opacity:.7;">Tipo</th>
                <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Inscritos</th>
                <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Asistidos</th>
                <th style="padding:.6rem .9rem;opacity:.7;text-align:center;">Ocupación</th>
              </tr></thead>
              <tbody>
              ${d.topEvents.map((e,i) => `
                <tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                  <td style="padding:.6rem .9rem;font-weight:700;color:#fbbf24;">${i+1}</td>
                  <td style="padding:.6rem .9rem;">${e.eventTitle||"—"}</td>
                  <td style="padding:.6rem .9rem;">${TYPE_LABELS[e.eventType]||e.eventType||"—"}</td>
                  <td style="padding:.6rem .9rem;text-align:center;">${e.registered??0}</td>
                  <td style="padding:.6rem .9rem;text-align:center;color:#4ade80;">${e.attended??0}</td>
                  <td style="padding:.6rem .9rem;text-align:center;">${(e.occupancyPercentage??0).toFixed(1)}%</td>
                </tr>`).join("")}
              </tbody>
            </table></div>`;
        }

        /* Alerts */
        const noRegs = d.noRegistrations || [];
        const lowAtt = d.lowAttendance   || [];
        if (noRegs.length || lowAtt.length) {
            html += `<h3 style="margin:0 0 .75rem;font-size:.95rem;color:#f87171;text-transform:uppercase;letter-spacing:.04em;">⚠ Alertas de participación</h3>`;
            if (noRegs.length) {
                html += `<div class="glass-card" style="padding:1rem 1.25rem;margin-bottom:1rem;border:1px solid rgba(248,113,113,.2);">
                  <p style="font-weight:600;margin:0 0 .5rem;color:#f87171;">Eventos activos sin ningún inscrito (${noRegs.length})</p>
                  <ul style="margin:0;padding-left:1.2rem;font-size:.83rem;">
                  ${noRegs.map(e => `<li style="margin:.2rem 0;">${e.eventTitle||e.eventId} <span style="opacity:.5;">— ${e.organizerEmail||"sin organizador"}</span></li>`).join("")}
                  </ul></div>`;
            }
            if (lowAtt.length) {
                html += `<div class="glass-card" style="padding:1rem 1.25rem;border:1px solid rgba(251,191,36,.2);">
                  <p style="font-weight:600;margin:0 0 .5rem;color:#fbbf24;">Eventos con tasa de asistencia &lt; 30% (${lowAtt.length})</p>
                  <ul style="margin:0;padding-left:1.2rem;font-size:.83rem;">
                  ${lowAtt.map(e => `<li style="margin:.2rem 0;">${e.eventTitle||e.eventId} — <span style="color:#fbbf24;">${(e.attendanceRate??0).toFixed(1)}%</span></li>`).join("")}
                  </ul></div>`;
            }
        }

        if (!html) html = `<p class="ev-empty">Sin datos de estadísticas aún. Haz clic en "↻ Sincronizar BD" para generar.</p>`;
        document.getElementById("analyticsContent").innerHTML = html;

    } catch (err) {
        document.getElementById("analyticsLoading").textContent = "Error al cargar análisis.";
    }
}

/* ── Audit tab ── */
async function loadAudit() {
    try {
        const res = await fetch("/reports/audit", { headers: hdrs });
        if (!res.ok) throw new Error();
        const data = await res.json();
        loaded.audit = true;
        document.getElementById("auditLoading").style.display = "none";
        if (!data.length) { document.getElementById("auditTable").innerHTML = `<p class="ev-empty">Sin registros.</p>`; return; }

        document.getElementById("auditTable").innerHTML = `
        <table style="width:100%;border-collapse:collapse;font-size:.83rem;">
          <thead><tr style="border-bottom:1px solid rgba(255,255,255,.15);text-align:left;">
            <th style="padding:.6rem .9rem;opacity:.7;">Fecha</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Entidad</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Código</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Acción</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Realizado por</th>
            <th style="padding:.6rem .9rem;opacity:.7;">Rol</th>
          </tr></thead>
          <tbody>
          ${data.map(a => {
              const pb   = a.performedBy || {};
              const date = a.createdAt ? new Date(a.createdAt).toLocaleString("es-CO",
                  {day:"2-digit",month:"short",year:"numeric",hour:"2-digit",minute:"2-digit"}) : "—";
              return `<tr style="border-bottom:1px solid rgba(255,255,255,.07);">
                <td style="padding:.6rem .9rem;white-space:nowrap;">${date}</td>
                <td style="padding:.6rem .9rem;">${a.entity||"—"}</td>
                <td style="padding:.6rem .9rem;font-size:.78rem;opacity:.65;">${a.entityCode||a.entityId||"—"}</td>
                <td style="padding:.6rem .9rem;font-weight:600;">${a.action||"—"}</td>
                <td style="padding:.6rem .9rem;">${pb.email||"—"}</td>
                <td style="padding:.6rem .9rem;opacity:.7;">${pb.role||"—"}</td>
              </tr>`;
          }).join("")}
          </tbody>
        </table>`;
    } catch { document.getElementById("auditLoading").textContent = "Error al cargar auditoría."; }
}

/* ── CSV download ── */
async function downloadCsv(url, filename) {
    try {
        const res = await fetch(url, { headers: hdrs });
        if (!res.ok) { alert("Error al generar el CSV"); return; }
        const blob = await res.blob();
        const a = document.createElement("a");
        a.href = URL.createObjectURL(blob);
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(a.href);
    } catch { alert("Error de conexión al descargar CSV"); }
}

/* ── Manual sync ── */
async function triggerSync() {
    const btn = document.querySelector('.add-btn[onclick="triggerSync()"]');
    if (btn) { btn.disabled = true; btn.textContent = "Sincronizando..."; }
    try {
        const res = await fetch("/reports/sync", { method: "POST", headers: hdrs });
        const d = await res.json();
        alert(d.message || "Sincronización completa");
        loaded = { events: false, organizers: false, analytics: false, audit: false };
        loadSummary();
        loadEvents();
    } catch { alert("Error al sincronizar"); }
    finally { if (btn) { btn.disabled = false; btn.textContent = "↻ Sincronizar BD"; } }
}
