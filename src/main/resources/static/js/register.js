const form = document.getElementById("registerForm");
const fullName = document.getElementById("fullName");
const idNumber = document.getElementById("idNumber");
const contactNumber = document.getElementById("contactNumber");
const username = document.getElementById("username");
const password = document.getElementById("password");
const confirmPassword = document.getElementById("confirmPassword");
const formStatus = document.getElementById("formStatus");

const ruleLength = document.getElementById("ruleLength");
const ruleUpper = document.getElementById("ruleUpper");
const ruleLower = document.getElementById("ruleLower");
const ruleDigit = document.getElementById("ruleDigit");
const ruleSpecial = document.getElementById("ruleSpecial");
const minLength = parseInt(document.getElementById("minLengthPolicy").value);
const minUpper = parseInt(document.getElementById("minUpperPolicy").value);
const minLower = parseInt(document.getElementById("minLowerPolicy").value);
const minDigit = parseInt(document.getElementById("minDigitPolicy").value);
const minSpecial = parseInt(document.getElementById("minSpecialPolicy").value);

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

function validateFullName() {
    const value = fullName.value.trim();

    if (value === "") {
        setError(fullName, "Full name is required.");
        return false;
    }

    if (value.length < 3) {
        setError(fullName, "Full name must be at least 3 characters.");
        return false;
    }

    clearError(fullName);
    return true;
}

function validateIdNumber() {
    const value = idNumber.value.trim();

    if (value === "") {
        setError(idNumber, "ID number is required.");
        return false;
    }

    if (!/^\d{6,20}$/.test(value)) {
        setError(idNumber, "ID number must contain only digits and be 6 to 20 characters.");
        return false;
    }

    clearError(idNumber);
    return true;
}

function validateContactNumber() {
    const value = contactNumber.value.trim();

    if (value === "") {
        setError(contactNumber, "Contact number is required.");
        return false;
    }

    if (!/^[0-9+\-\s]{8,20}$/.test(value)) {
        setError(contactNumber, "Enter a valid contact number.");
        return false;
    }

    clearError(contactNumber);
    return true;
}

function validateUsername() {
    const value = username.value.trim();

    if (value === "") {
        setError(username, "Username is required.");
        return false;
    }

    if (!/^[a-zA-Z0-9_]{4,20}$/.test(value)) {
        setError(username, "Username must be 4 to 20 characters using letters, digits, or underscore.");
        return false;
    }

    clearError(username);
    return true;
}

function updatePasswordRules() {
    const value = password.value;

    const upperCount = (value.match(/[A-Z]/g) || []).length;
    const lowerCount = (value.match(/[a-z]/g) || []).length;
    const digitCount = (value.match(/\d/g) || []).length;
    const specialCount = (value.match(/[^A-Za-z0-9]/g) || []).length;

    const hasLength = value.length >= minLength;
    const hasUpper = upperCount >= minUpper;
    const hasLower = lowerCount >= minLower;
    const hasDigit = digitCount >= minDigit;
    const hasSpecial = specialCount >= minSpecial;

    toggleRule(ruleLength, hasLength);
    toggleRule(ruleUpper, hasUpper);
    toggleRule(ruleLower, hasLower);
    toggleRule(ruleDigit, hasDigit);
    toggleRule(ruleSpecial, hasSpecial);

    return hasLength && hasUpper && hasLower && hasDigit && hasSpecial;
}

function toggleRule(element, isValid) {
    if (isValid) {
        element.classList.add("valid");
    } else {
        element.classList.remove("valid");
    }
}

function validatePassword() {
    const value = password.value;

    if (value === "") {
        setError(password, "Password is required.");
        return false;
    }

    if (!updatePasswordRules()) {
        setError(password, "Password does not meet the required rules.");
        return false;
    }

    clearError(password);
    return true;
}

function validateConfirmPassword() {
    const value = confirmPassword.value;

    if (value === "") {
        setError(confirmPassword, "Please confirm your password.");
        return false;
    }

    if (value !== password.value) {
        setError(confirmPassword, "Passwords do not match.");
        return false;
    }

    clearError(confirmPassword);
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

fullName.addEventListener("input", validateFullName);
idNumber.addEventListener("input", validateIdNumber);
contactNumber.addEventListener("input", validateContactNumber);
username.addEventListener("input", validateUsername);

password.addEventListener("input", () => {
    updatePasswordRules();
    validatePassword();
    if (confirmPassword.value.trim() !== "") {
        validateConfirmPassword();
    }
});

confirmPassword.addEventListener("input", validateConfirmPassword);

document.querySelectorAll(".toggle-password").forEach(button => {
    button.addEventListener("click", () => {
        const targetId = button.getAttribute("data-target");
        const input = document.getElementById(targetId);

        if (input.type === "password") {
            input.type = "text";
            button.textContent = "Hide";
        } else {
            input.type = "password";
            button.textContent = "Show";
        }
    });
});

form.addEventListener("submit", function (event) {
    clearFormStatus();

    const isFullNameValid = validateFullName();
    const isIdValid = validateIdNumber();
    const isContactValid = validateContactNumber();
    const isUsernameValid = validateUsername();
    const isPasswordValid = validatePassword();
    const isConfirmPasswordValid = validateConfirmPassword();

    const isFormValid =
        isFullNameValid &&
        isIdValid &&
        isContactValid &&
        isUsernameValid &&
        isPasswordValid &&
        isConfirmPasswordValid;

    if (!isFormValid) {
        event.preventDefault();
        showFormStatus("Please fix the highlighted fields before submitting.", "error");
    }
});