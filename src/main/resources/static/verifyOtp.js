window.onload = function () {
    const email = localStorage.getItem("resetEmail");
    if (email) {
        document.getElementById("emailDisplay").textContent = "📧 " + email;
    }
};

document.getElementById("verifyOtpForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const email           = localStorage.getItem("resetEmail");
    const newPassword     = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const errorMsg        = document.getElementById("errorMsg");
    const successMsg      = document.getElementById("successMsg");

    if (newPassword !== confirmPassword) {
        errorMsg.style.display   = "block";
        errorMsg.textContent     = "❌ Passwords do not match";
        successMsg.style.display = "none";
        return;
    }

    fetch(`/auth/reset-password?email=${encodeURIComponent(email)}&newPassword=${encodeURIComponent(newPassword)}&confirmPassword=${encodeURIComponent(confirmPassword)}`, {  // ✅ /auth
        method: "POST"
    })
    .then(res => res.json())
    .then(data => {
        if (data.message) {
            successMsg.style.display = "block";
            successMsg.textContent   = "✅ Password reset! Redirecting to login...";
            errorMsg.style.display   = "none";

            localStorage.removeItem("resetEmail");

            setTimeout(() => window.location.href = "/auth/login", 1500);  // ✅ /api/login

        } else {
            errorMsg.style.display = "block";
            errorMsg.textContent   = "❌ " + data.error;
        }
    })
    .catch(() => {
        errorMsg.style.display = "block";
        errorMsg.textContent   = "❌ Something went wrong";
    });
});