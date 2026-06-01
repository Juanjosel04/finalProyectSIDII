/* =========================================================
   ORGANIZER HOME — stats en tiempo real
   IDs: statActive, statAttendees, statFinished, statNext
========================================================= */

const token = sessionStorage.getItem("token");

async function loadStats() {
    try {
        const res = await fetch("/events/my", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            if (res.status === 401) {
                sessionStorage.clear();
                window.location.href = "/login";
            }
            return;
        }
        const events = await res.json();

        const now      = new Date();
        let active     = 0;
        let finished   = 0;
        let attendees  = 0;
        let nextEvent  = null;

        events.forEach(e => {
            if (e.status === "ACTIVE")    active++;
            if (e.status === "FINISHED")  finished++;
            attendees += (e.totalCapacity ?? 0) - (e.availableSpots ?? 0);

            if (e.status === "ACTIVE" && e.startDate) {
                const start = new Date(e.startDate);
                if (start >= now) {
                    if (!nextEvent || start < new Date(nextEvent.startDate)) {
                        nextEvent = e;
                    }
                }
            }
        });

        document.getElementById("statActive").textContent    = active;
        document.getElementById("statAttendees").textContent = attendees;
        document.getElementById("statFinished").textContent  = finished;
        document.getElementById("statNext").textContent      = nextEvent
            ? nextEvent.title
            : "Sin próximos";

    } catch (err) {
        console.error("organizer-home stats:", err);
    }
}

loadStats();
