const trackForm = document.getElementById("trackForm");
const trackingNumber = document.getElementById("trackingNumber");
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

// Validate the tracking number field to ensure it is not empty
function validateTrackingNumber() {
    const value = trackingNumber.value.trim();

    if (value === "") {
        setError(trackingNumber, "Tracking number is required.");
        return false;
    }

    clearError(trackingNumber);
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

// Add event listeners for real-time validation and form submission
trackingNumber.addEventListener("input", validateTrackingNumber);

// Validate the form on submission and prevent submission if there are validation errors
trackForm.addEventListener("submit", function (event) {
    clearFormStatus();

    const isTrackingValid = validateTrackingNumber();

    if (!isTrackingValid) {
        event.preventDefault();
        showFormStatus("Please enter a valid tracking number.", "error");
    }
});