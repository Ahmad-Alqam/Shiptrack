const shipmentForm = document.getElementById("shipmentForm");
const pickupAddress = document.getElementById("pickupAddress");
const deliveryAddress = document.getElementById("deliveryAddress");
const packageType = document.getElementById("packageType");
const packageWeight = document.getElementById("packageWeight");
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

// Validate the pickup address field to ensure it is not empty
function validatePickupAddress() {
    const value = pickupAddress.value.trim();

    if (value === "") {
        setError(pickupAddress, "Pickup address is required.");
        return false;
    }

    clearError(pickupAddress);
    return true;
}

// Validate the delivery address field to ensure it is not empty
function validateDeliveryAddress() {
    const value = deliveryAddress.value.trim();

    if (value === "") {
        setError(deliveryAddress, "Delivery address is required.");
        return false;
    }

    clearError(deliveryAddress);
    return true;
}

// Validate the package type field to ensure it is not empty
function validatePackageType() {
    const value = packageType.value.trim();

    if (value === "") {
        setError(packageType, "Package type is required.");
        return false;
    }

    clearError(packageType);
    return true;
}

// Validate the package weight field to ensure it is not empty and is a valid number greater than 0
function validatePackageWeight() {
    const value = packageWeight.value.trim();

    if (value === "") {
        setError(packageWeight, "Package weight is required.");
        return false;
    }

    if (isNaN(value) || Number(value) <= 0) {
        setError(packageWeight, "Package weight must be greater than 0.");
        return false;
    }

    clearError(packageWeight);
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
pickupAddress.addEventListener("input", validatePickupAddress);
deliveryAddress.addEventListener("input", validateDeliveryAddress);
packageType.addEventListener("input", validatePackageType);
packageWeight.addEventListener("input", validatePackageWeight);

// Validate the form on submission and prevent submission if there are validation errors
shipmentForm.addEventListener("submit", function (event) {
    clearFormStatus();

    const isPickupValid = validatePickupAddress();
    const isDeliveryValid = validateDeliveryAddress();
    const isTypeValid = validatePackageType();
    const isWeightValid = validatePackageWeight();

    if (!isPickupValid || !isDeliveryValid || !isTypeValid || !isWeightValid) {
        event.preventDefault();
        showFormStatus("Please fix the highlighted fields before submitting.", "error");
    }
});