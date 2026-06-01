const token = sessionStorage.getItem("token");

async function submitForm(e) {
    e.preventDefault();

    const email      = document.getElementById("email").value.trim();
    const password   = document.getElementById("password").value;
    const employeeId = document.getElementById("employeeId").value.trim();
    const msgEl      = document.getElementById("formMsg");

    showMsg("", "");

    try {
        const res = await fetch("/admin/users/organizers", {
            method: "POST",
            headers: {
                "Content-Type":  "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ email, password, employeeId: employeeId || null })
        });

        const data = await res.json();

        if (!res.ok) {
            showMsg(data.error || "Error al registrar el organizador.", false);
            return;
        }

        showMsg(`Organizador ${data.email} registrado correctamente.`, true);
        document.getElementById("registerForm").reset();

        setTimeout(() => { window.location.href = "/admin/organizers"; }, 1500);

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
