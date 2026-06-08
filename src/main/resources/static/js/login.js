const loginForm = document.getElementById("loginForm");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const formStatus = document.getElementById("formStatus");
const togglePasswordBtn = document.getElementById("togglePassword");

// Client-side validation and interactivity for the login form
function setError(input, message) {
    const errorElement = document.getElementById(input.id + "Error");
    errorElement.textContent = message;
    input.classList.add("input-error");
}

// Clear error message and styling for a specific input field
function clearError(input) {
    const errorElement = document.getElementById(input.id + "Error");
    errorElement.textContent = "";
    input.classList.remove("input-error");
}

// Validate the username field to ensure it is not empty and meets minimum length requirements
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

// Validate the password field to ensure it is not empty and meets minimum length requirements
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

// Clear the form status message and styling
function clearFormStatus() {
    formStatus.textContent = "";
    formStatus.classList.remove("success", "error");
}

// Display a form status message with appropriate styling based on the type (success or error)
function showFormStatus(message, type) {
    formStatus.textContent = message;
    formStatus.classList.remove("success", "error");
    formStatus.classList.add(type);
}

// Add event listeners for real-time validation and interactivity
usernameInput.addEventListener("input", validateUsername);
passwordInput.addEventListener("input", validatePassword);

// Toggle the visibility of the password field when the toggle button is clicked
togglePasswordBtn.addEventListener("click", function () {
    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        togglePasswordBtn.textContent = "Hide";
    } else {
        passwordInput.type = "password";
        togglePasswordBtn.textContent = "Show";
    }
});

// Validate the form on submission and prevent submission if there are validation errors
loginForm.addEventListener("submit", function (event) {
    clearFormStatus();

    const isUsernameValid = validateUsername();
    const isPasswordValid = validatePassword();

    if (!isUsernameValid || !isPasswordValid) {
        event.preventDefault();
        showFormStatus("Please fix the highlighted fields before logging in.", "error");
    }
});