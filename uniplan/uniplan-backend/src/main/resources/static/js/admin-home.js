const token = sessionStorage.getItem("token");

async function loadAdminStats() {
    try {
        const [eventsRes, usersRes] = await Promise.all([
            fetch("/events",            { headers: { "Authorization": `Bearer ${token}` } }),
            fetch("/admin/users/count", { headers: { "Authorization": `Bearer ${token}` } })
        ]);

        if (eventsRes.ok) {
            const events = await eventsRes.json();
            let active        = 0;
            let participation = 0;
            events.forEach(e => {
                if (e.status === "ACTIVE") active++;
                participation += (e.totalCapacity ?? 0) - (e.availableSpots ?? 0);
            });
            document.getElementById("statActive").textContent        = active;
            document.getElementById("statParticipation").textContent = participation;
        }

        if (usersRes.ok) {
            const data = await usersRes.json();
            document.getElementById("statUsers").textContent = data.total ?? "—";
        }

    } catch (err) {
        console.error("admin-home stats:", err);
    }
}

loadAdminStats();
