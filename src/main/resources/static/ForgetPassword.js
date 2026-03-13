let countdownInterval = null;

document.getElementById("sendOtpBtn").addEventListener("click", function () {

    const email      = document.getElementById("email").value;
    const errorMsg   = document.getElementById("errorMsg");
    const successMsg = document.getElementById("successMsg");

    if (!email) {
        errorMsg.style.display = "block";
        errorMsg.textContent   = "❌ Please enter your email";
        return;
    }

    fetch("/auth/forgot-password?email=" + encodeURIComponent(email), {  // ✅ /auth
        method: "POST"
    })
    .then(res => res.json())
    .then(data => {
        if (data.message) {
            localStorage.setItem("resetEmail", email);

            successMsg.style.display = "block";
            successMsg.textContent   = "✅ OTP sent to your email!";
            errorMsg.style.display   = "none";

            setTimeout(() => {
                document.getElementById("step1").style.display = "none";
                document.getElementById("step2").style.display = "block";
                document.getElementById("emailDisplay").value  = email;
                startCountdown(5 * 60);
            }, 1000);

        } else {
            errorMsg.style.display   = "block";
            errorMsg.textContent     = "❌ " + data.error;
            successMsg.style.display = "none";
        }
    })
    .catch(() => {
        errorMsg.style.display = "block";
        errorMsg.textContent   = "❌ Something went wrong";
    });
});

function startCountdown(seconds) {
    const timerBox     = document.getElementById("timerBox");
    const timerText    = document.getElementById("timerText");
    const submitOtpBtn = document.getElementById("submitOtpBtn");

    timerBox.style.display = "block";
    timerBox.classList.remove("urgent");
    submitOtpBtn.disabled  = false;

    let remaining = seconds;

    countdownInterval = setInterval(() => {
        const mins = String(Math.floor(remaining / 60)).padStart(2, "0");
        const secs = String(remaining % 60).padStart(2, "0");

        timerText.textContent = `⏳ OTP expires in: ${mins}:${secs}`;

        if (remaining <= 60) {
            timerBox.classList.add("urgent");
        }

        if (remaining <= 0) {
            clearInterval(countdownInterval);
            timerBox.style.display = "none";

            const otpErrorMsg         = document.getElementById("otpErrorMsg");
            otpErrorMsg.style.display = "block";
            otpErrorMsg.textContent   = "❌ OTP expired. Please request a new one.";
            submitOtpBtn.disabled     = true;

            document.getElementById("otp").value    = "";
            document.getElementById("otp").disabled = true;
        }

        remaining--;
    }, 1000);
}

document.getElementById("submitOtpBtn").addEventListener("click", function () {

    const email         = localStorage.getItem("resetEmail");
    const otp           = document.getElementById("otp").value;
    const otpErrorMsg   = document.getElementById("otpErrorMsg");
    const otpSuccessMsg = document.getElementById("otpSuccessMsg");

    if (otp.length !== 6) {
        otpErrorMsg.style.display = "block";
        otpErrorMsg.textContent   = "❌ OTP must be 6 digits";
        return;
    }

    fetch(`/auth/verify-reset-otp?email=${encodeURIComponent(email)}&otp=${otp}`, {  // ✅ /auth
        method: "POST"
    })
    .then(res => res.json())
    .then(data => {
        if (data.message) {
            clearInterval(countdownInterval);
            document.getElementById("timerBox").style.display = "none";

            otpSuccessMsg.style.display = "block";
            otpSuccessMsg.textContent   = "✅ OTP verified! Redirecting...";
            otpErrorMsg.style.display   = "none";

            setTimeout(() => window.location.href = "/auth/verify", 1500);  // ✅ check below

        } else {
            otpErrorMsg.style.display   = "block";
            otpErrorMsg.textContent     = "❌ " + data.error;
            otpSuccessMsg.style.display = "none";
        }
    })
    .catch(() => {
        otpErrorMsg.style.display = "block";
        otpErrorMsg.textContent   = "❌ Something went wrong";
    });
});