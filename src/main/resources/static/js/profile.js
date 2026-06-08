const profileForm = document.getElementById("profileForm");
const fullName = document.getElementById("fullName");
const username = document.getElementById("username");
const contactNumber = document.getElementById("contactNumber");
const formStatus = document.getElementById("formStatus");

// Set an error message and styling for a specific input field
function setError(input, message) {
    const errorElement = document.getElementById(input.id + "Error");
    if (errorElement) {
        errorElement.textContent = message;
    }
    input.classList.add("input-error");
}

// Clear error message and styling for a specific input field
function clearError(input) {
    const errorElement = document.getElementById(input.id + "Error");
    if (errorElement) {
        errorElement.textContent = "";
    }
    input.classList.remove("input-error");
}

// Validate the full name field to ensure it is not empty
function validateFullName() {
    const value = fullName.value.trim();

    if (value === "") {
        setError(fullName, "Full name is required.");
        return false;
    }

    clearError(fullName);
    return true;
}

// Validate the username field to ensure it is not empty and meets minimum length requirements
function validateUsername() {
    const value = username.value.trim();

    if (value === "") {
        setError(username, "Username is required.");
        return false;
    }

    if (value.length < 3) {
        setError(username, "Username must be at least 3 characters.");
        return false;
    }

    clearError(username);
    return true;
}

// Validate the contact number field to ensure it is not empty
function validateContactNumber() {
    const value = contactNumber.value.trim();

    if (value === "") {
        setError(contactNumber, "Contact number is required.");
        return false;
    }

    clearError(contactNumber);
    return true;
}

// Clear the form status message and styling
function clearFormStatus() {
    if (!formStatus) return;
    formStatus.textContent = "";
    formStatus.classList.remove("success", "error");
}

// Display a form status message with appropriate styling based on the type (success or error)
function showFormStatus(message, type) {
    if (!formStatus) return;
    formStatus.textContent = message;
    formStatus.classList.remove("success", "error");
    formStatus.classList.add(type);
}

// Add event listeners for real-time validation and form submission
fullName.addEventListener("input", validateFullName);
username.addEventListener("input", validateUsername);
contactNumber.addEventListener("input", validateContactNumber);

// Validate the form on submission and prevent submission if there are validation errors
profileForm.addEventListener("submit", function (event) {
    clearFormStatus();

    const isFullNameValid = validateFullName();
    const isUsernameValid = validateUsername();
    const isContactValid = validateContactNumber();

    if (!isFullNameValid || !isUsernameValid || !isContactValid) {
        event.preventDefault();
        showFormStatus("Please fix the highlighted fields before saving.", "error");
    }
});