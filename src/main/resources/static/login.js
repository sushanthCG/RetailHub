document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const popup = document.getElementById("popup");
    const user = {
        email:    document.querySelector("input[name='email']").value,
        password: document.querySelector("input[name='password']").value
    };

    fetch("/auth/login", {                             
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(user)
    })
    .then(res => res.json())
	.then(data => {
	    if (data.message) {
	        popup.innerText = "Login Successful!";
	        popup.classList.add("show", "success");

	        setTimeout(() => {
				if (data.role === 'ADMIN') {
				    window.location.href = '/admin/dashboard';
				} else {
				    window.location.href = '/api/customerhome';
				}
	        }, 1000);
	    } else if (data.error) {
	        popup.innerText = "❌ " + data.error;
	        popup.classList.add("show", "error");
	        setTimeout(() => popup.classList.remove("show"), 2500);
	    }
	})
    .catch(() => {
        popup.innerText = " Something went wrong!";
        popup.classList.add("show", "error");
        setTimeout(() => popup.classList.remove("show"), 2500);
    });
});