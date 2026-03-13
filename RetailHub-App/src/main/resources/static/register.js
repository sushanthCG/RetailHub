/* ═══════════════════════════════════════════
   EYE TOGGLE
═══════════════════════════════════════════ */
function toggleEye(id, btn) {
    const inp = document.getElementById(id);
    inp.type = inp.type === 'password' ? 'text' : 'password';
    btn.textContent = inp.type === 'password' ? '👁' : '🙈';
}

/* ═══════════════════════════════════════════
   EMAIL VALIDATION
   Rule: @ must exist AND at least 5 chars before @
═══════════════════════════════════════════ */
document.getElementById('emailInput').addEventListener('input', function () {
    const val   = this.value;
    const atIdx = val.indexOf('@');
    const valid = atIdx >= 5 && val.length > atIdx + 1;
    document.getElementById('emailHint').style.display =
        (val.length > 0 && !valid) ? 'block' : 'none';
});

/* ═══════════════════════════════════════════
   PASSWORD STRENGTH METER
   Box is hidden — only shows when user types
═══════════════════════════════════════════ */
const COLORS = ['#dc3545', '#fd7e14', '#ffc107', '#28a745'];
const LABELS = ['Weak',    'Fair',    'Good',    'Strong' ];

const RULES = {
    'r-len':     { test: v => v.length >= 8,                     text: 'At least 8 characters'       },
    'r-upper':   { test: v => /[A-Z]/.test(v),                   text: 'At least 1 uppercase letter'  },
    'r-num':     { test: v => /[0-9]/.test(v),                   text: 'At least 1 number'            },
    'r-special': { test: v => /[!@#$%^&*(),.?":{}|<>]/.test(v), text: 'At least 1 special character' }
};

document.getElementById('password1').addEventListener('input', function () {
    const v    = this.value;
    const wrap = document.getElementById('strengthWrap');

    /* Show box when typing, hide when field is empty */
    wrap.classList.toggle('visible', v.length > 0);

    let score = 0;

    Object.entries(RULES).forEach(([id, rule]) => {
        const pass     = rule.test(v);
        if (pass) score++;
        const el       = document.getElementById(id);
        el.className   = pass ? 'ok' : 'bad';
        el.textContent = (pass ? '✓ ' : '✗ ') + rule.text;
    });

    const fill  = document.getElementById('strengthFill');
    const label = document.getElementById('strengthLabel');

    fill.style.width      = (score / 4 * 100) + '%';
    fill.style.background = COLORS[score - 1] || '#eee';
    label.textContent     = score > 0 ? LABELS[score - 1] : '';
    label.style.color     = COLORS[score - 1] || '#888';
});

/* ═══════════════════════════════════════════
   FORM SUBMIT — original logic untouched
═══════════════════════════════════════════ */
document.getElementById("registerForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const popup     = document.getElementById("popupMessage");
    const username  = document.querySelector("input[name='username']").value;
    const email     = document.querySelector("input[name='email']").value;
    const password1 = document.querySelector("input[name='password1']").value;
    const password2 = document.querySelector("input[name='password2']").value;

    if (password1 !== password2) {
        popup.innerText = "❌ Passwords do not match!";
        popup.classList.add("show", "error");
        setTimeout(() => popup.classList.remove("show"), 2000);
        return;
    }

    const user = { username, email, password: password1 };

    fetch("/api/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(user)
    })
    .then(res => res.json())
    .then(data => {
        popup.classList.remove("success", "error");

        if (data.message) {
            popup.innerText = "🎉 Registration Successful!";
            popup.classList.add("show", "success");
            setTimeout(() => window.location.href = "/auth/login", 1500);

        } else if (data.error) {
            popup.innerText = "❌ " + data.error;
            popup.classList.add("show", "error");
            setTimeout(() => popup.classList.remove("show"), 2000);
        }
    })
    .catch(() => {
        popup.innerText = "❌ Something went wrong!";
        popup.classList.add("show", "error");
        setTimeout(() => popup.classList.remove("show"), 2000);
    });
});