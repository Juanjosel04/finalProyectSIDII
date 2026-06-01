const token = sessionStorage.getItem("token");

loadMyActiveEvents();

async function loadMyActiveEvents() {
    const select = document.getElementById("eventSelect");
    try {
        const res = await fetch("/events/my", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) { sessionStorage.clear(); window.location.href = "/login"; return; }
        if (!res.ok) throw new Error();
        const events = await res.json();
        const active = events.filter(e => e.status === "ACTIVE");

        if (!active.length) {
            select.innerHTML = `<option value="">No tienes eventos activos</option>`;
            return;
        }

        select.innerHTML = `<option value="">Selecciona un evento</option>` +
            active.map(e => {
                const date  = e.startDate ? ` — ${fmtDate(e.startDate)}` : "";
                const spots = e.availableSpots != null ? ` (${e.availableSpots} cupos)` : "";
                return `<option value="${e.id}">${e.title}${date}${spots}</option>`;
            }).join("");
    } catch {
        select.innerHTML = `<option value="">Error al cargar eventos</option>`;
    }
}

async function submitForm(e) {
    e.preventDefault();

    const eventId      = document.getElementById("eventSelect").value;
    const studentEmail = document.getElementById("studentEmail").value.trim();

    if (!eventId) { showMsg("Selecciona un evento.", false); return; }

    showMsg("", "");

    try {
        const res = await fetch("/registrations/organizer", {
            method: "POST",
            headers: {
                "Content-Type":  "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ eventId, studentEmail })
        });

        const data = await res.json();

        if (!res.ok) {
            showMsg(data.error || "No se pudo inscribir al estudiante.", false);
            return;
        }

        const spots = data.availableSpotsAfter != null
            ? ` Cupos restantes: ${data.availableSpotsAfter}.`
            : "";

        showMsg(`Estudiante inscrito correctamente en "${data.eventTitle || eventId}".${spots}`, true);
        document.getElementById("attendanceForm").reset();
        loadMyActiveEvents();

    } catch {
        showMsg("Error de conexión. Intenta de nuevo.", false);
    }
}

function showMsg(text, success) {
    const el = document.getElementById("formMsg");
    el.style.display = text ? "block" : "none";
    el.style.color   = success ? "#4ade80" : "#f87171";
    el.textContent   = text;
}

function fmtDate(dt) {
    return new Date(dt).toLocaleString("es-CO", {
        day:"2-digit", month:"short", year:"numeric"
    });
}
