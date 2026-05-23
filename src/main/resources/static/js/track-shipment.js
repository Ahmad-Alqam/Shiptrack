const trackForm = document.getElementById("trackForm");
const trackingNumber = document.getElementById("trackingNumber");
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

function validateTrackingNumber() {
    const value = trackingNumber.value.trim();

    if (value === "") {
        setError(trackingNumber, "Tracking number is required.");
        return false;
    }

    clearError(trackingNumber);
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

trackingNumber.addEventListener("input", validateTrackingNumber);

trackForm.addEventListener("submit", function (event) {
    clearFormStatus();

    const isTrackingValid = validateTrackingNumber();

    if (!isTrackingValid) {
        event.preventDefault();
        showFormStatus("Please enter a valid tracking number.", "error");
    }
});