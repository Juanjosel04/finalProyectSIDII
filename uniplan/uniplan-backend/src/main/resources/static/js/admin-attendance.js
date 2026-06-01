const token = sessionStorage.getItem("token");

loadActiveEvents();

async function loadActiveEvents() {
    const select = document.getElementById("eventSelect");
    try {
        const res = await fetch("/events", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) { sessionStorage.clear(); window.location.href = "/login"; return; }
        if (!res.ok) throw new Error();
        const events = (await res.json()).filter(e => e.status === "ACTIVE");

        if (!events.length) {
            select.innerHTML = `<option value="">No hay eventos activos</option>`;
            return;
        }
        select.innerHTML = `<option value="">Selecciona un evento</option>` +
            events.map(e => {
                const date = e.startDate ? ` — ${fmtDateShort(e.startDate)}` : "";
                return `<option value="${e.id}">${e.title}${date}</option>`;
            }).join("");
    } catch {
        select.innerHTML = `<option value="">Error al cargar eventos</option>`;
    }
}

/* Carga la lista de asistencias cuando se elige un evento */
document.getElementById("eventSelect").addEventListener("change", function () {
    const eventId = this.value;
    if (eventId) {
        document.getElementById("attendanceSection").style.display = "block";
        document.getElementById("viewAllLink").href =
            `/admin/view-attendances?eventId=${eventId}`;
        loadAttendanceList(eventId);
    } else {
        document.getElementById("attendanceSection").style.display = "none";
    }
});

async function loadAttendanceList(eventId) {
    if (!eventId) return;
    const wrap  = document.getElementById("attendanceListWrap");
    const count = document.getElementById("attendanceCount");
    wrap.innerHTML = `<p class="ev-empty" style="font-size:.83rem;">Cargando...</p>`;

    try {
        const res = await fetch(`/registrations/event/${eventId}/attended`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();
        const regs = await res.json();

        count.textContent = regs.length ? `(${regs.length})` : "(0)";

        if (!regs.length) {
            wrap.innerHTML = `<p class="ev-empty" style="font-size:.83rem;">
                Nadie ha asistido a este evento aún.</p>`;
            return;
        }

        wrap.innerHTML = `
            <table style="width:100%; border-collapse:collapse; font-size:.83rem;">
                <thead>
                    <tr style="border-bottom:1px solid rgba(255,255,255,.12); text-align:left;">
                        <th style="padding:.5rem .8rem; opacity:.65;">#</th>
                        <th style="padding:.5rem .8rem; opacity:.65;">Nombre</th>
                        <th style="padding:.5rem .8rem; opacity:.65;">Código</th>
                        <th style="padding:.5rem .8rem; opacity:.65;">Correo</th>
                        <th style="padding:.5rem .8rem; opacity:.65;">Asistió el</th>
                    </tr>
                </thead>
                <tbody>
                    ${regs.map((r, i) => {
                        const name = [r.studentFirstName, r.studentLastName].filter(Boolean).join(" ") || "—";
                        return `<tr style="border-bottom:1px solid rgba(255,255,255,.06);">
                            <td style="padding:.5rem .8rem; opacity:.45; font-size:.78rem;">${i + 1}</td>
                            <td style="padding:.5rem .8rem; font-weight:500;">${name}</td>
                            <td style="padding:.5rem .8rem; font-family:monospace; font-size:.8rem;">${r.studentId || "—"}</td>
                            <td style="padding:.5rem .8rem;">${r.studentEmail || "—"}</td>
                            <td style="padding:.5rem .8rem; opacity:.7; white-space:nowrap;">${r.attendedAt ? fmtDate(r.attendedAt) : "—"}</td>
                        </tr>`;
                    }).join("")}
                </tbody>
            </table>`;
    } catch {
        wrap.innerHTML = `<p class="ev-empty" style="font-size:.83rem; color:#f87171;">
            Error al cargar asistencias.</p>`;
    }
}

async function submitAttendance(e) {
    e.preventDefault();

    const eventId     = document.getElementById("eventSelect").value;
    const studentCode = document.getElementById("studentCode").value.trim();

    if (!eventId)     { showMsg("Selecciona un evento.", false); return; }
    if (!studentCode) { showMsg("Ingresa el código del estudiante.", false); return; }

    showMsg("", null);

    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true; btn.textContent = "Registrando...";

    try {
        const res = await fetch("/registrations/attendance", {
            method: "POST",
            headers: {
                "Content-Type":  "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ eventId, studentCode })
        });

        const data = await res.json();

        if (!res.ok) {
            showMsg(data.error || "No se pudo registrar la asistencia.", false);
            return;
        }

        const name = [data.studentFirstName, data.studentLastName].filter(Boolean).join(" ")
                     || studentCode;
        showMsg(`✓ Asistencia registrada para ${name} en "${data.eventTitle || eventId}".`, true);
        document.getElementById("studentCode").value = "";

        // Refresca la lista inmediatamente
        loadAttendanceList(eventId);

    } catch {
        showMsg("Error de conexión. Intenta de nuevo.", false);
    } finally {
        btn.disabled = false; btn.textContent = "Registrar asistencia";
    }
}

function showMsg(text, success) {
    const el = document.getElementById("formMsg");
    el.style.display = text ? "block" : "none";
    el.style.color   = success === true  ? "#4ade80"
                     : success === false ? "#f87171"
                     : "inherit";
    el.textContent   = text;
}

function fmtDate(dt) {
    return new Date(dt).toLocaleString("es-CO", {
        day: "2-digit", month: "short", year: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}

function fmtDateShort(dt) {
    return new Date(dt).toLocaleString("es-CO", {
        day: "2-digit", month: "short", year: "numeric"
    });
}
