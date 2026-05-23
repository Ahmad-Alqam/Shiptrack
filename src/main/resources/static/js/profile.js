const profileForm = document.getElementById("profileForm");
const fullName = document.getElementById("fullName");
const username = document.getElementById("username");
const contactNumber = document.getElementById("contactNumber");
const formStatus = document.getElementById("formStatus");

function setError(input, message) {
    const errorElement = document.getElementById(input.id + "Error");
    if (errorElement) {
        errorElement.textContent = message;
    }
    input.classList.add("input-error");
}

function clearError(input) {
    const errorElement = document.getElementById(input.id + "Error");
    if (errorElement) {
        errorElement.textContent = "";
    }
    input.classList.remove("input-error");
}

function validateFullName() {
    const value = fullName.value.trim();

    if (value === "") {
        setError(fullName, "Full name is required.");
        return false;
    }

    clearError(fullName);
    return true;
}

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

function validateContactNumber() {
    const value = contactNumber.value.trim();

    if (value === "") {
        setError(contactNumber, "Contact number is required.");
        return false;
    }

    clearError(contactNumber);
    return true;
}

function clearFormStatus() {
    if (!formStatus) return;
    formStatus.textContent = "";
    formStatus.classList.remove("success", "error");
}

function showFormStatus(message, type) {
    if (!formStatus) return;
    formStatus.textContent = message;
    formStatus.classList.remove("success", "error");
    formStatus.classList.add(type);
}

fullName.addEventListener("input", validateFullName);
username.addEventListener("input", validateUsername);
contactNumber.addEventListener("input", validateContactNumber);

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