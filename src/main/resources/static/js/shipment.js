const shipmentForm = document.getElementById("shipmentForm");
const pickupAddress = document.getElementById("pickupAddress");
const deliveryAddress = document.getElementById("deliveryAddress");
const packageType = document.getElementById("packageType");
const packageWeight = document.getElementById("packageWeight");
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

function validatePickupAddress() {
    const value = pickupAddress.value.trim();

    if (value === "") {
        setError(pickupAddress, "Pickup address is required.");
        return false;
    }

    clearError(pickupAddress);
    return true;
}

function validateDeliveryAddress() {
    const value = deliveryAddress.value.trim();

    if (value === "") {
        setError(deliveryAddress, "Delivery address is required.");
        return false;
    }

    clearError(deliveryAddress);
    return true;
}

function validatePackageType() {
    const value = packageType.value.trim();

    if (value === "") {
        setError(packageType, "Package type is required.");
        return false;
    }

    clearError(packageType);
    return true;
}

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

function clearFormStatus() {
    formStatus.textContent = "";
    formStatus.classList.remove("success", "error");
}

function showFormStatus(message, type) {
    formStatus.textContent = message;
    formStatus.classList.remove("success", "error");
    formStatus.classList.add(type);
}

pickupAddress.addEventListener("input", validatePickupAddress);
deliveryAddress.addEventListener("input", validateDeliveryAddress);
packageType.addEventListener("input", validatePackageType);
packageWeight.addEventListener("input", validatePackageWeight);

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