/**
 * script.js — SecurePass Frontend Logic
 * 
 * Handles client-side user interactions and asynchronous HTTP fetch() communication
 * with the Java OOP backend server.
 */

// Dynamically determine backend base URL.
// If served from Java server, origin is http://localhost:8080.
// If opened via VS Code Live Server (e.g. port 5500), points to Java server on port 8080.
const API_BASE_URL = (window.location.port === "8080" || window.location.protocol === "file:")
    ? ""
    : "http://localhost:8080";

// DOM Elements - Password Checker
const passwordInput = document.getElementById("passwordInput");
const togglePasswordBtn = document.getElementById("togglePasswordBtn");
const eyeOpenIcon = document.getElementById("eyeOpenIcon");
const eyeClosedIcon = document.getElementById("eyeClosedIcon");
const clearPasswordBtn = document.getElementById("clearPasswordBtn");
const analyzeBtn = document.getElementById("analyzeBtn");
const checkerSpinner = document.getElementById("checkerSpinner");

const checkerResultsEmpty = document.getElementById("checkerResults");
const checkerResultsActive = document.getElementById("checkerResultsActive");
const strengthText = document.getElementById("strengthText");
const scoreTag = document.getElementById("scoreTag");
const meterFill = document.getElementById("meterFill");
const statEntropy = document.getElementById("statEntropy");
const statCrackTime = document.getElementById("statCrackTime");
const suggestionsList = document.getElementById("suggestionsList");

// Checklist Items
const reqLength = document.getElementById("reqLength");
const reqUpper = document.getElementById("reqUpper");
const reqLower = document.getElementById("reqLower");
const reqDigit = document.getElementById("reqDigit");
const reqSpecial = document.getElementById("reqSpecial");

// DOM Elements - Password Generator
const generatedPasswordDisplay = document.getElementById("generatedPasswordDisplay");
const copyPasswordBtn = document.getElementById("copyPasswordBtn");
const copiedToast = document.getElementById("copiedToast");
const quickRegenBtn = document.getElementById("quickRegenBtn");
const testInCheckerBtn = document.getElementById("testInCheckerBtn");
const lengthSlider = document.getElementById("lengthSlider");
const lengthDisplay = document.getElementById("lengthDisplay");
const optUpper = document.getElementById("optUpper");
const optLower = document.getElementById("optLower");
const optDigits = document.getElementById("optDigits");
const optSpecial = document.getElementById("optSpecial");
const generateBtn = document.getElementById("generateBtn");
const genSpinner = document.getElementById("genSpinner");
const genStrengthText = document.getElementById("genStrengthText");
const genScoreText = document.getElementById("genScoreText");

// Toast Container
const toastContainer = document.getElementById("toastContainer");

// Debounce timer for real-time checker typing
let typingDebounceTimer = null;

// =========================================================================
// Initialization
// =========================================================================

document.addEventListener("DOMContentLoaded", () => {
    setupEventListeners();
    // Generate an initial sample password on page load
    requestGeneratePassword();
});

function setupEventListeners() {
    // 1. Password Visibility Toggle
    togglePasswordBtn.addEventListener("click", () => {
        const isPassword = passwordInput.getAttribute("type") === "password";
        passwordInput.setAttribute("type", isPassword ? "text" : "password");
        eyeOpenIcon.classList.toggle("hidden", isPassword);
        eyeClosedIcon.classList.toggle("hidden", !isPassword);
    });

    // 2. Clear Password Input
    clearPasswordBtn.addEventListener("click", () => {
        passwordInput.value = "";
        passwordInput.focus();
        resetCheckerUI();
    });

    // 3. Live Typing Evaluation (Debounced 300ms)
    passwordInput.addEventListener("input", () => {
        clearTimeout(typingDebounceTimer);
        const val = passwordInput.value;
        if (!val.trim()) {
            resetCheckerUI();
            return;
        }
        typingDebounceTimer = setTimeout(() => {
            requestCheckPassword(val);
        }, 300);
    });

    // 4. Explicit Analyze Button
    analyzeBtn.addEventListener("click", () => {
        const val = passwordInput.value;
        if (!val.trim()) {
            showToast("Please enter a password to analyze.", "error");
            passwordInput.focus();
            return;
        }
        requestCheckPassword(val);
    });

    // 5. Length Slider Live Update
    lengthSlider.addEventListener("input", (e) => {
        const val = e.target.value;
        lengthDisplay.textContent = `${val} characters`;
    });

    // 6. Generate Password Button
    generateBtn.addEventListener("click", () => {
        requestGeneratePassword();
    });

    quickRegenBtn.addEventListener("click", () => {
        requestGeneratePassword();
    });

    // 7. Copy Generated Password
    copyPasswordBtn.addEventListener("click", async () => {
        const passwordToCopy = generatedPasswordDisplay.textContent.trim();
        if (!passwordToCopy || passwordToCopy === "Click Generate to create") {
            return;
        }

        try {
            await navigator.clipboard.writeText(passwordToCopy);
            copiedToast.classList.remove("hidden");
            setTimeout(() => {
                copiedToast.classList.add("hidden");
            }, 1800);
        } catch (err) {
            // Fallback for older browsers
            const textArea = document.createElement("textarea");
            textArea.value = passwordToCopy;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand("copy");
            document.body.removeChild(textArea);

            copiedToast.classList.remove("hidden");
            setTimeout(() => {
                copiedToast.classList.add("hidden");
            }, 1800);
        }
    });

    // 8. Test in Checker Shortcut
    testInCheckerBtn.addEventListener("click", () => {
        const currentPass = generatedPasswordDisplay.textContent.trim();
        if (currentPass && currentPass !== "Click Generate to create") {
            passwordInput.value = currentPass;
            requestCheckPassword(currentPass);
            // Smooth scroll to checker card
            document.getElementById("checker-section").scrollIntoView({ behavior: "smooth" });
        }
    });

    // 9. Prevent unchecking all character pool checkboxes
    [optUpper, optLower, optDigits, optSpecial].forEach(chk => {
        chk.addEventListener("change", () => {
            const anyChecked = optUpper.checked || optLower.checked || optDigits.checked || optSpecial.checked;
            if (!anyChecked) {
                chk.checked = true;
                showToast("At least one character pool must be selected.", "error");
            }
        });
    });
}

// =========================================================================
// Backend API Communication (Java HTTP Server)
// =========================================================================

/**
 * Calls Java backend POST /api/check-password
 */
async function requestCheckPassword(password) {
    setCheckerLoading(true);

    try {
        const response = await fetch(`${API_BASE_URL}/api/check-password`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ password: password })
        });

        if (!response.ok) {
            const errorJson = await response.json().catch(() => ({}));
            throw new Error(errorJson.error || `HTTP error ${response.status}`);
        }

        const data = await response.json();
        renderCheckerResults(data);
    } catch (error) {
        console.error("Backend Error (/api/check-password):", error);
        showToast("Cannot reach Java backend. Ensure PasswordServer is running on port 8080.", "error");
    } finally {
        setCheckerLoading(false);
    }
}

/**
 * Calls Java backend POST /api/generate-password
 */
async function requestGeneratePassword() {
    setGeneratorLoading(true);

    const payload = {
        length: parseInt(lengthSlider.value, 10),
        includeUpper: optUpper.checked,
        includeLower: optLower.checked,
        includeDigits: optDigits.checked,
        includeSpecial: optSpecial.checked
    };

    try {
        const response = await fetch(`${API_BASE_URL}/api/generate-password`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorJson = await response.json().catch(() => ({}));
            throw new Error(errorJson.error || `HTTP error ${response.status}`);
        }

        const data = await response.json();
        
        // Display generated password
        generatedPasswordDisplay.textContent = data.password;

        // Update Generator Strength indicator
        if (data.analysis) {
            genStrengthText.textContent = data.analysis.strengthLevel;
            genStrengthText.style.color = data.analysis.colorCode;
            genScoreText.textContent = `(${data.analysis.score}/100)`;
        }
    } catch (error) {
        console.error("Backend Error (/api/generate-password):", error);
        showToast("Cannot reach Java backend. Ensure PasswordServer is running on port 8080.", "error");
    } finally {
        setGeneratorLoading(false);
    }
}

// =========================================================================
// UI Rendering & State Management
// =========================================================================

function renderCheckerResults(result) {
    checkerResultsEmpty.classList.add("hidden");
    checkerResultsActive.classList.remove("hidden");

    // 1. Strength Level & Score
    strengthText.textContent = result.strengthLevel;
    scoreTag.textContent = `${result.score}/100`;
    strengthText.style.color = result.colorCode;

    // 2. Progress Meter Bar
    meterFill.style.width = `${result.score}%`;
    meterFill.style.backgroundColor = result.colorCode;
    meterFill.style.boxShadow = `0 0 14px ${result.colorCode}80`;

    // 3. Shannon Entropy & Crack Time
    statEntropy.textContent = `${result.entropyBits} bits`;
    statCrackTime.textContent = result.crackTimeEstimate;
    statCrackTime.style.color = result.colorCode;

    // 4. Checklist Items
    updateChecklistItem(reqLength, result.length >= 8);
    updateChecklistItem(reqUpper, result.hasUppercase);
    updateChecklistItem(reqLower, result.hasLowercase);
    updateChecklistItem(reqDigit, result.hasDigit);
    updateChecklistItem(reqSpecial, result.hasSpecial);

    // 5. Suggestions
    suggestionsList.innerHTML = "";
    if (result.suggestions && result.suggestions.length > 0) {
        result.suggestions.forEach(s => {
            const li = document.createElement("li");
            li.textContent = s;
            suggestionsList.appendChild(li);
        });
    } else {
        const li = document.createElement("li");
        li.textContent = "Your password meets all recommended cryptographic security parameters!";
        suggestionsList.appendChild(li);
    }
}

function updateChecklistItem(elem, isSatisfied) {
    if (isSatisfied) {
        elem.classList.add("satisfied");
    } else {
        elem.classList.remove("satisfied");
    }
}

function resetCheckerUI() {
    checkerResultsEmpty.classList.remove("hidden");
    checkerResultsActive.classList.add("hidden");
    meterFill.style.width = "0%";
}

function setCheckerLoading(isLoading) {
    if (isLoading) {
        checkerSpinner.classList.remove("hidden");
        analyzeBtn.disabled = true;
    } else {
        checkerSpinner.classList.add("hidden");
        analyzeBtn.disabled = false;
    }
}

function setGeneratorLoading(isLoading) {
    if (isLoading) {
        genSpinner.classList.remove("hidden");
        generateBtn.disabled = true;
    } else {
        genSpinner.classList.add("hidden");
        generateBtn.disabled = false;
    }
}

function showToast(message, type = "info") {
    const toast = document.createElement("div");
    toast.className = `app-toast toast-${type}`;
    toast.textContent = message;

    toastContainer.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateY(10px)";
        toast.style.transition = "all 0.3s ease";
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}
