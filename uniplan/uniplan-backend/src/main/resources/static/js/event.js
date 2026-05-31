/* =========================================================
   CREATE EVENT
   HTML IDs: createEventForm, title, type, description,
             startDate, endDate, modality, venue, campus, room,
             meetingUrlGroup, meetingUrl, capacity, waitlistEnabled,
             details, message
   CSS: .ev-btn-primary (submit), .ev-msg (message)
========================================================= */

/* ── Mostrar/ocultar meetingUrl según modalidad ── */
document.getElementById("modality").addEventListener("change", () => {
    const v = document.getElementById("modality").value;
    document.getElementById("meetingUrlGroup").style.display =
        (v === "VIRTUAL" || v === "HYBRID") ? "block" : "none";
});

/* ── Mostrar campos específicos según tipo de evento ── */
document.getElementById("type").addEventListener("change", function () {
    document.querySelectorAll(".ev-type-details").forEach(el => el.style.display = "none");
    const v = this.value;
    if (v === "CULTURAL" || v === "OTHER") {
        document.getElementById("details-FLEXIBLE").style.display = "block";
    } else if (v) {
        const section = document.getElementById("details-" + v);
        if (section) section.style.display = "block";
    }
});

/* ── Helpers ── */
const msgDiv = document.getElementById("message");

function showMsg(text, type) {
    msgDiv.className     = `ev-msg ev-msg-${type}`;
    msgDiv.textContent   = text;
    msgDiv.style.display = "block";
}

function g(id)    { return document.getElementById(id)?.value?.trim() ?? ""; }
function gInt(id) { const n = parseInt(document.getElementById(id)?.value); return isNaN(n) ? null : n; }
function gChk(id) { return document.getElementById(id)?.checked ?? false; }

/* ── Submit ── */
document.getElementById("createEventForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    msgDiv.style.display = "none";

    const token = sessionStorage.getItem("token");
    if (!token) { window.location.href = "/login"; return; }

    /* Recoger valores */
    const title       = g("title");
    const type        = g("type");
    const description = g("description");
    const startDate   = g("startDate") + ":00";
    const endDate     = g("endDate")   + ":00";
    const modality    = g("modality");
    const venue       = g("venue");
    const campus      = g("campus");
    const room        = g("room");
    const address     = g("address");
    const meetingUrl  = g("meetingUrl");
    const capacity    = gInt("capacity");
    const waitlist    = gChk("waitlistEnabled");
    const timezone    = Intl.DateTimeFormat().resolvedOptions().timeZone || "America/Bogota";

    /* Validaciones */
    if (!title || !type || !description || !startDate || !endDate || !modality || !venue || !capacity) {
        showMsg("Completa todos los campos requeridos (*)", "error"); return;
    }
    if (capacity <= 0) {
        showMsg("Los cupos deben ser mayor que 0.", "error"); return;
    }
    if (new Date(endDate) <= new Date(startDate)) {
        showMsg("La fecha de finalización debe ser posterior a la de inicio.", "error"); return;
    }
    if ((modality === "VIRTUAL" || modality === "HYBRID") && !meetingUrl) {
        showMsg("Ingresa el enlace de reunión para eventos virtuales o híbridos.", "error"); return;
    }

    let details;
    try {
        details = collectDetails(type);
    } catch (err) {
        showMsg(err.message, "error");
        return;
    }

    const durationMinutes = Math.round((new Date(endDate) - new Date(startDate)) / 60000);

    /* Body — estructura exacta del modelo Event de MongoDB */
    const body = {
        title,
        description,
        type,

        schedule: {
            startDate,
            endDate,
            durationMinutes,
            timezone
        },

        location: {
            venue,
            campus:     campus     || null,
            room:       room       || null,
            address:    address    || null,
            modality,
            meetingUrl: meetingUrl || null
        },

        capacity: {
            total:           capacity,
            registered:      0,
            available:       capacity,
            waitlist:        0,
            waitlistEnabled: waitlist
        },

        /* organizer se auto-rellena en el backend desde el JWT */
        details
    };

    /* Submit */
    const btn  = document.querySelector(".ev-btn-primary");
    const orig = btn.textContent;
    btn.disabled    = true;
    btn.textContent = "Creando evento...";

    try {
        const res = await fetch("/events", {
            method: "POST",
            headers: {
                "Content-Type":  "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            const err = await res.text();
            throw new Error(err);
        }

        const data = await res.json();
        showMsg(`Evento "${data.title}" creado correctamente.`, "success");
        document.getElementById("createEventForm").reset();
        document.getElementById("meetingUrlGroup").style.display = "none";
        document.querySelectorAll(".ev-type-details").forEach(el => el.style.display = "none");

        setTimeout(() => {
            const role = sessionStorage.getItem("role");
            window.location.href = role === "ORGANIZER" ? "/organizer/home" : "/admin/home";
        }, 1800);

    } catch (err) {
        console.error("Error al crear evento:", err);
        const msg = err.message?.length < 300 ? err.message : "No se pudo crear el evento.";
        showMsg(msg, "error");
    } finally {
        btn.disabled    = false;
        btn.textContent = orig;
    }
});

/* Auto-ocultar mensaje al escribir */
document.addEventListener("input", () => { msgDiv.style.display = "none"; });

/* ── Construir objeto details según tipo de evento ── */
function collectDetails(type) {
    const d = {};

    switch (type) {
        case "WORKSHOP": {
            const prereq = g("prereqSubjectCode");
            if (prereq) d.prerequisiteSubjectCode = prereq;
            const minSem = gInt("minSemester");
            if (minSem) d.minimumSemester = minSem;
            const raw = document.getElementById("materialsList")?.value?.trim();
            if (raw) d.materialsList = raw.split("\n").map(s => s.trim()).filter(Boolean);
            break;
        }
        case "ACADEMIC": {
            const name = g("speakerName");
            if (name) {
                d.speaker = {
                    name,
                    profile:     g("speakerProfile")     || null,
                    affiliation: g("speakerAffiliation") || null
                };
            }
            const su = g("streamingUrl");
            if (su) d.streamingUrl = su;
            const ru = g("resourcesUrl");
            if (ru) d.resourcesUrl = ru;
            break;
        }
        case "SPORT": {
            const sport = g("sport");
            if (sport) d.sport = sport;
            const rules = g("tournamentRules");
            if (rules) d.rules = rules;
            const tc = gInt("teamsCount");
            if (tc) d.teamsCount = tc;
            const ppt = gInt("participantsPerTeam");
            if (ppt) d.participantsPerTeam = ppt;
            const structure = g("tournamentStructure");
            if (structure) d.tournamentStructure = structure;
            break;
        }
        case "VOLUNTEER": {
            const hours = gInt("minimumHours");
            if (hours) d.minimumHours = hours;
            const cause = g("cause");
            if (cause) d.cause = cause;
            const raw = document.getElementById("activities")?.value?.trim();
            if (raw) d.activities = raw.split("\n").map(s => s.trim()).filter(Boolean);
            const mp = g("meetingPoints");
            if (mp) d.meetingPoints = mp;
            const coord = g("coordinators");
            if (coord) d.coordinators = coord;
            break;
        }
        default: { // CULTURAL, OTHER
            const raw = g("detailsJson");
            if (raw) {
                try { return JSON.parse(raw); }
                catch { throw new Error("El JSON de detalles no es válido."); }
            }
        }
    }

    return d;
}
