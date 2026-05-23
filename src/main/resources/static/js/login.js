const loginForm = document.getElementById("loginForm");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const formStatus = document.getElementById("formStatus");
const togglePasswordBtn = document.getElementById("togglePassword");

function setError(input, message) {
    const errorElement = document.getElementById(input.id + "Error");
    errorElement.textContent = message;
    input.classList.add("input-error");
}

function clearError(input) {
    const errorElement = document.getElementById(input.id + "Error");
    errorElement.textContent = "";
    input.classList.remove("input-error");
}

function validateUsername() {
    const value = usernameInput.value.trim();

    if (value === "") {
        setError(usernameInput, "Username is required.");
        return false;
    }

    if (value.length < 3) {
        setError(usernameInput, "Username must be at least 3 characters.");
        return false;
    }

    clearError(usernameInput);
    return true;
}

function validatePassword() {
    const value = passwordInput.value;

    if (value === "") {
        setError(passwordInput, "Password is required.");
        return false;
    }

    if (value.length < 1) {
        setError(passwordInput, "Password is required.");
        return false;
    }

    clearError(passwordInput);
    return true;
}

function clearFormStatus() {
    formStatus.textContent = "";
    formStatus.classList.remove("success", "error");
}

function showFormStatus(message, type) {
    formStatus.textContent = message;
    formStatus.classList.remove("success", "error");
    formStatus.classList.add(type);
}

usernameInput.addEventListener("input", validateUsername);
passwordInput.addEventListener("input", validatePassword);

togglePasswordBtn.addEventListener("click", function () {
    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        togglePasswordBtn.textContent = "Hide";
    } else {
        passwordInput.type = "password";
        togglePasswordBtn.textContent = "Show";
    }
});

loginForm.addEventListener("submit", function (event) {
    clearFormStatus();

    const isUsernameValid = validateUsername();
    const isPasswordValid = validatePassword();

    if (!isUsernameValid || !isPasswordValid) {
        event.preventDefault();
        showFormStatus("Please fix the highlighted fields before logging in.", "error");
    }
});