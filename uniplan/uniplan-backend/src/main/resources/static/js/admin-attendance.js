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
                const date  = e.startDate ? ` — ${fmtDate(e.startDate)}` : "";
                return `<option value="${e.id}">${e.title}${date}</option>`;
            }).join("");
    } catch {
        select.innerHTML = `<option value="">Error al cargar eventos</option>`;
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
        document.getElementById("attendanceForm").reset();
        loadActiveEvents();

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
        day:"2-digit", month:"short", year:"numeric"
    });
}
