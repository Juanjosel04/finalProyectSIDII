/* =========================================================
   EDIT EVENT — carga evento y hace PUT /events/{id}
   IDs: title, type, description, startDate, endDate,
        modality, venue, campus, room, address, meetingUrlGroup, meetingUrl,
        capacity, waitlistEnabled, details, message, editEventForm
========================================================= */

const token   = sessionStorage.getItem("token");
const role    = sessionStorage.getItem("role");
const eventId = new URLSearchParams(window.location.search).get("id");
const msgDiv  = document.getElementById("message");
let currentCapacity = { registered: 0, waitlist: 0 };

/* Ajustar links de volver según rol */
const eventsUrl = role === "ORGANIZER" ? "/organizer/events" : "/admin/events";
document.getElementById("navBackBrand").href = eventsUrl;
document.getElementById("backBtn").href      = eventsUrl;

if (!eventId) window.location.href = "/admin/events";

/* ── Mostrar/ocultar link de reunión ── */
document.getElementById("modality").addEventListener("change", toggleMeetingUrl);

/* ── Mostrar campos específicos según tipo ── */
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

function toggleMeetingUrl() {
    const v = document.getElementById("modality").value;
    document.getElementById("meetingUrlGroup").style.display =
        (v === "VIRTUAL" || v === "HYBRID") ? "block" : "none";
}

/* ── Helpers ── */
function showMsg(text, type) {
    msgDiv.className     = `ev-msg ev-msg-${type}`;
    msgDiv.textContent   = text;
    msgDiv.style.display = "block";
}

function toLocal(iso) {
    if (!iso) return "";
    return iso.substring(0, 16);
}

/* ── Cargar evento ── */
async function loadEvent() {
    try {
        const res = await fetch(`/events/${eventId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Evento no encontrado");
        const e = await res.json();

        document.getElementById("title").value       = e.title       || "";
        document.getElementById("type").value        = e.type        || "";
        document.getElementById("description").value = e.description || "";

        if (e.schedule) {
            document.getElementById("startDate").value = toLocal(e.schedule.startDate);
            document.getElementById("endDate").value   = toLocal(e.schedule.endDate);
        }
        if (e.location) {
            document.getElementById("modality").value   = e.location.modality   || "";
            document.getElementById("venue").value      = e.location.venue      || "";
            document.getElementById("campus").value     = e.location.campus     || "";
            document.getElementById("room").value       = e.location.room       || "";
            document.getElementById("address").value   = e.location.address    || "";
            document.getElementById("meetingUrl").value = e.location.meetingUrl || "";
            toggleMeetingUrl();
        }
        if (e.capacity) {
            document.getElementById("capacity").value          = e.capacity.total || "";
            document.getElementById("waitlistEnabled").checked = !!e.capacity.waitlistEnabled;
            currentCapacity = {
                registered: e.capacity.registered ?? 0,
                waitlist:   e.capacity.waitlist   ?? 0
            };
        }
        populateDetails(e.type, e.details);
    } catch (err) {
        showMsg(err.message, "error");
    }
}

/* ── Submit ── */
document.getElementById("editEventForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    msgDiv.style.display = "none";

    const title       = document.getElementById("title").value.trim();
    const type        = document.getElementById("type").value;
    const description = document.getElementById("description").value.trim();
    const startDate   = document.getElementById("startDate").value + ":00";
    const endDate     = document.getElementById("endDate").value   + ":00";
    const modality    = document.getElementById("modality").value;
    const venue       = document.getElementById("venue").value.trim();
    const campus      = document.getElementById("campus").value.trim();
    const room        = document.getElementById("room").value.trim();
    const address     = document.getElementById("address").value.trim();
    const meetingUrl  = document.getElementById("meetingUrl").value.trim();
    const capacity    = parseInt(document.getElementById("capacity").value);
    const waitlist    = document.getElementById("waitlistEnabled").checked;
    const timezone    = Intl.DateTimeFormat().resolvedOptions().timeZone || "America/Bogota";

    if (!title || !type || !description || !startDate || !endDate || !modality || !venue || !capacity) {
        showMsg("Completa todos los campos requeridos (*)", "error"); return;
    }
    if (new Date(endDate) <= new Date(startDate)) {
        showMsg("La fecha de finalización debe ser posterior a la de inicio.", "error"); return;
    }
    if ((modality === "VIRTUAL" || modality === "HYBRID") && !meetingUrl) {
        showMsg("Ingresa el enlace de reunión.", "error"); return;
    }

    let details;
    try {
        details = collectDetails(type);
    } catch (err) {
        showMsg(err.message, "error"); return;
    }

    const durationMinutes = Math.round((new Date(endDate) - new Date(startDate)) / 60000);
    const newAvailable    = Math.max(0, capacity - currentCapacity.registered);

    const body = {
        title, description, type,
        schedule: { startDate, endDate, durationMinutes, timezone },
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
            registered:      currentCapacity.registered,
            available:       newAvailable,
            waitlist:        currentCapacity.waitlist,
            waitlistEnabled: waitlist
        },
        details
    };

    const btn = document.querySelector(".ev-btn-primary");
    const orig = btn.textContent;
    btn.disabled = true; btn.textContent = "Guardando...";

    try {
        const res = await fetch(`/events/${eventId}`, {
            method: "PUT",
            headers: { "Content-Type":"application/json", "Authorization":`Bearer ${token}` },
            body: JSON.stringify(body)
        });
        if (!res.ok) { const d = await res.text(); throw new Error(d); }

        showMsg("Evento actualizado correctamente.", "success");
        setTimeout(() => { window.location.href = eventsUrl; }, 1500);
    } catch (err) {
        showMsg(err.message?.length < 200 ? err.message : "No se pudo actualizar el evento.", "error");
    } finally {
        btn.disabled = false; btn.textContent = orig;
    }
});

loadEvent();

/* ── Helpers para campos de tipo ── */
function g(id)    { return document.getElementById(id)?.value?.trim() ?? ""; }
function gInt(id) { const n = parseInt(document.getElementById(id)?.value); return isNaN(n) ? null : n; }
function setVal(id, val) {
    const el = document.getElementById(id);
    if (el && val !== null && val !== undefined) el.value = val;
}

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
            const su = g("streamingUrl"); if (su) d.streamingUrl = su;
            const ru = g("resourcesUrl"); if (ru) d.resourcesUrl = ru;
            break;
        }
        case "SPORT": {
            const sport = g("sport");          if (sport) d.sport = sport;
            const rules = g("tournamentRules"); if (rules) d.rules = rules;
            const tc  = gInt("teamsCount");            if (tc)  d.teamsCount = tc;
            const ppt = gInt("participantsPerTeam");   if (ppt) d.participantsPerTeam = ppt;
            const str = g("tournamentStructure");      if (str) d.tournamentStructure = str;
            break;
        }
        case "VOLUNTEER": {
            const hours = gInt("minimumHours"); if (hours) d.minimumHours = hours;
            const cause = g("cause");           if (cause) d.cause = cause;
            const raw   = document.getElementById("activities")?.value?.trim();
            if (raw) d.activities = raw.split("\n").map(s => s.trim()).filter(Boolean);
            const mp    = g("meetingPoints");   if (mp)    d.meetingPoints = mp;
            const coord = g("coordinators");    if (coord) d.coordinators = coord;
            break;
        }
        default: {
            const raw = g("detailsJson");
            if (raw) {
                try { return JSON.parse(raw); }
                catch { throw new Error("El JSON de detalles no es válido."); }
            }
        }
    }
    return d;
}

function populateDetails(type, details) {
    document.querySelectorAll(".ev-type-details").forEach(el => el.style.display = "none");
    if (!type) return;

    if (type === "CULTURAL" || type === "OTHER") {
        document.getElementById("details-FLEXIBLE").style.display = "block";
        if (details && Object.keys(details).length > 0)
            setVal("detailsJson", JSON.stringify(details, null, 2));
        return;
    }

    const section = document.getElementById("details-" + type);
    if (section) section.style.display = "block";

    if (!details) return;

    switch (type) {
        case "WORKSHOP":
            setVal("prereqSubjectCode", details.prerequisiteSubjectCode);
            setVal("minSemester",       details.minimumSemester);
            setVal("materialsList",     Array.isArray(details.materialsList)
                ? details.materialsList.join("\n") : details.materialsList);
            break;
        case "ACADEMIC":
            if (details.speaker) {
                setVal("speakerName",        details.speaker.name);
                setVal("speakerProfile",     details.speaker.profile);
                setVal("speakerAffiliation", details.speaker.affiliation);
            }
            setVal("streamingUrl", details.streamingUrl);
            setVal("resourcesUrl", details.resourcesUrl);
            break;
        case "SPORT":
            setVal("sport",                details.sport);
            setVal("tournamentRules",      details.rules);
            setVal("teamsCount",           details.teamsCount);
            setVal("participantsPerTeam",  details.participantsPerTeam);
            setVal("tournamentStructure",  details.tournamentStructure);
            break;
        case "VOLUNTEER":
            setVal("minimumHours",  details.minimumHours);
            setVal("cause",         details.cause);
            setVal("activities",    Array.isArray(details.activities)
                ? details.activities.join("\n") : details.activities);
            setVal("meetingPoints", details.meetingPoints);
            setVal("coordinators",  details.coordinators);
            break;
    }
}
