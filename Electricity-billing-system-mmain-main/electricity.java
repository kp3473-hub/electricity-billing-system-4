const CURRENT_USER_KEY = 'currentUser';
const USER_DATA_KEY = 'userData'; // Key for all user data
const RATE_PER_UNIT = 8.5; // Example rate: $8.50 per unit (KWh)

// --- Initial Setup and Data Management ---

// Initialize user data structure in localStorage if it doesn't exist
function initializeUsers() {
    let users = localStorage.getItem(USER_DATA_KEY);
    if (!users) {
        let initialData = {};
        for (let i = 1; i <= 12; i++) {
            // User ID 1 to 12. Bills stored as an array.
            initialData[i] = { id: i, name: 'User ' + i, bills: [] };
        }
        localStorage.setItem(USER_DATA_KEY, JSON.stringify(initialData));
    }
}

// Get all user data from localStorage
function getAllUserData() {
    return JSON.parse(localStorage.getItem(USER_DATA_KEY));
}

// Save all user data back to localStorage
function saveAllUserData(data) {
    localStorage.setItem(USER_DATA_KEY, JSON.stringify(data));
}


// --- Login/Logout Functionality ---

function loginUser() {
    const userIdInput = document.getElementById('user-id');
    const userId = parseInt(userIdInput.value.trim());

    if (userId >= 1 && userId <= 12) {
        const allUserData = getAllUserData();
        const user = allUserData[userId];
        
        // Save the current user's ID to localStorage
        localStorage.setItem(CURRENT_USER_KEY, userId);

        document.getElementById('login-message').textContent = `Logged in as ${user.name}.`;
        showMainApp(user);
    } else {
        document.getElementById('login-message').textContent = 'Invalid User ID. Please use 1 to 12.';
    }
}

function logoutUser() {
    localStorage.removeItem(CURRENT_USER_KEY);
    document.getElementById('login-screen').style.display = 'block';
    document.getElementById('main-app').style.display = 'none';
}

function checkLogin() {
    const currentUserId = localStorage.getItem(CURRENT_USER_KEY);
    if (currentUserId) {
        const allUserData = getAllUserData();
        const user = allUserData[currentUserId];
        showMainApp(user);
    } else {
        document.getElementById('login-screen').style.display = 'block';
        document.getElementById('main-app').style.display = 'none';
    }
}

function showMainApp(user) {
    document.getElementById('login-screen').style.display = 'none';
    document.getElementById('main-app').style.display = 'block';
    document.getElementById('welcome-user').textContent = `Welcome, ${user.name} (ID: ${user.id})`;
    displayBillHistory(user);
}


// --- Calculation and Saving Functionality ---

function calculateAndSaveBill() {
    const unitsInput = document.getElementById('current-units');
    const units = parseFloat(unitsInput.value);
    const currentUserId = localStorage.getItem(CURRENT_USER_KEY);

    if (isNaN(units) || units < 0) {
        document.getElementById('calculation-result').textContent = 'Please enter a valid number of units.';
        return;
    }

    const billAmount = units * RATE_PER_UNIT;
    const billDate = new Date().toLocaleDateString();

    const newBill = {
        date: billDate,
        units: units.toFixed(2),
        amount: billAmount.toFixed(2)
    };

    // 1. Update data in JavaScript object
    let allUserData = getAllUserData();
    let currentUser = allUserData[currentUserId];
    
    currentUser.bills.push(newBill);
    allUserData[currentUserId] = currentUser;

    // 2. Save updated data back to localStorage
    saveAllUserData(allUserData);

    document.getElementById('calculation-result').textContent = 
        `Bill calculated and saved: ${units.toFixed(2)} units for $${billAmount.toFixed(2)}`;
    
    // 3. Update the history display
    displayBillHistory(currentUser);
    unitsInput.value = ''; // Clear input field
}

// --- Display History Functionality ---

function displayBillHistory(user) {
    const historyDiv = document.getElementById('bill-history');
    historyDiv.innerHTML = ''; // Clear previous history

    if (user.bills.length === 0) {
        historyDiv.innerHTML = '<p>No billing history found.</p>';
        return;
    }

    let html = '<table><thead><tr><th>Date</th><th>Units Used</th><th>Bill Amount ($)</th></tr></thead><tbody>';
    
    user.bills.forEach(bill => {
        html += `<tr><td>${bill.date}</td><td>${bill.units}</td><td>$${bill.amount}</td></tr>`;
    });

    html += '</tbody></table>';
    historyDiv.innerHTML = html;
}


// --- Execute on Page Load ---
document.addEventListener('DOMContentLoaded', () => {
    initializeUsers(); // Setup data on first run
    checkLogin();      // Check if a user is already logged in
});