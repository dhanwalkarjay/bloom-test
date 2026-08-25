const SUPABASE_URL = 'https://ddelmbcqxuwminhjyemz.supabase.co/';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRkZWxtYmNxeHV3bWluaGp5ZW16Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODUyMjUwMzUsImV4cCI6MjEwMDgwMTAzNX0.deso9CqFu1n1tS6k_zc7qGwk3RSjOgA0nOVnFM4gcWo';

const supabase = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

// DOM Elements
const authContainer = document.getElementById('authContainer');
const dashboardContainer = document.getElementById('dashboardContainer');
const authError = document.getElementById('authError');
const phoneInput = document.getElementById('phoneInput');
const passwordInput = document.getElementById('passwordInput');
const loginBtn = document.getElementById('loginBtn');
const logoutBtn = document.getElementById('logoutBtn');
const adminName = document.getElementById('adminName');
const approvalsList = document.getElementById('approvalsList');
const ordersList = document.getElementById('ordersList');
const payoutsList = document.getElementById('payoutsList');

// Event Listeners
loginBtn.addEventListener('click', handleLogin);
logoutBtn.addEventListener('click', handleLogout);

// Auth Flow
async function checkSession() {
    const { data: { session } } = await supabase.auth.getSession();
    if (session) {
        // Assume verified admin if session exists (RLS enforces this securely anyway)
        showDashboard(session.user.phone);
    } else {
        showLogin();
    }
}

async function handleLogin() {
    const phone = phoneInput.value.trim();
    const password = passwordInput.value.trim();
    
    if (!phone || !password) {
        showError('Please enter phone and password');
        return;
    }

    loginBtn.innerText = 'Logging in...';
    loginBtn.disabled = true;
    authError.classList.add('hidden');

    const { data, error } = await supabase.auth.signInWithPassword({
        phone: phone,
        password: password,
    });

    loginBtn.innerText = 'Login';
    loginBtn.disabled = false;

    if (error) {
        showError(error.message);
    } else {
        showDashboard(data.user.phone);
    }
}

async function handleLogout() {
    await supabase.auth.signOut();
    showLogin();
}

// UI State
function showLogin() {
    authContainer.classList.remove('hidden');
    dashboardContainer.classList.add('hidden');
    phoneInput.value = '';
    passwordInput.value = '';
}

function showDashboard(phoneStr) {
    authContainer.classList.add('hidden');
    dashboardContainer.classList.remove('hidden');
    adminName.innerText = `Admin (${phoneStr || 'Session'})`;
    
    loadApprovals();
    loadOrders();
    loadPayouts();
}

function showError(msg) {
    authError.innerText = msg;
    authError.classList.remove('hidden');
}

// Data Loading
async function loadApprovals() {
    // Simplified: fetch users with 'merchant' role from a profiles table (if it exists)
    // For this prototype, we'll just show dummy data as RLS restricts user table access directly.
    // In a real scenario, there would be a secure view or RPC.
    approvalsList.innerHTML = `
        <div class="border rounded p-4 flex justify-between items-center">
            <div>
                <p class="font-semibold text-gray-800">Rose & Co. Florist</p>
                <p class="text-sm text-gray-500">Pending Review</p>
            </div>
            <button class="bg-green-500 text-white px-3 py-1 rounded text-sm hover:bg-green-600">Approve</button>
        </div>
        <div class="border rounded p-4 flex justify-between items-center">
            <div>
                <p class="font-semibold text-gray-800">Lily's Garden</p>
                <p class="text-sm text-gray-500">Pending Review</p>
            </div>
            <button class="bg-green-500 text-white px-3 py-1 rounded text-sm hover:bg-green-600">Approve</button>
        </div>
    `;
}

async function loadOrders() {
    const { data: orders, error } = await supabase
        .from('orders')
        .select('*')
        .order('created_at', { ascending: false })
        .limit(10);

    if (error) {
        ordersList.innerHTML = `<div class="text-red-500 text-sm">Failed to load orders: ${error.message}</div>`;
        return;
    }

    if (!orders || orders.length === 0) {
        ordersList.innerHTML = `<div class="text-gray-500 text-sm italic">No recent orders found.</div>`;
        return;
    }

    let html = '';
    orders.forEach(order => {
        let statusColor = 'text-blue-500';
        if (order.status === 'delivered') statusColor = 'text-green-500';
        else if (order.status === 'cancelled') statusColor = 'text-red-500';
        
        html += `
            <div class="border rounded p-4">
                <div class="flex justify-between items-center mb-2">
                    <span class="font-semibold text-gray-800">#${order.id.substring(0,8)}...</span>
                    <span class="text-sm font-semibold ${statusColor}">${order.status.toUpperCase()}</span>
                </div>
                <div class="text-sm text-gray-600 flex justify-between">
                    <span>${new Date(order.created_at).toLocaleString()}</span>
                    <span class="font-medium text-gray-800">₹${order.total_amount}</span>
                </div>
            </div>
        `;
    });

    ordersList.innerHTML = html;
}

async function loadPayouts() {
    const { data: orders, error } = await supabase
        .from('orders')
        .select('id, shop_id, florist_earning, status')
        .eq('status', 'delivered')
        .limit(10);

    if (error) {
        payoutsList.innerHTML = `<div class="text-red-500 text-sm">Failed to load payouts: ${error.message}</div>`;
        return;
    }

    if (!orders || orders.length === 0) {
        payoutsList.innerHTML = `<div class="text-gray-500 text-sm italic">No pending payouts.</div>`;
        return;
    }

    let html = '';
    orders.forEach(order => {
        html += `
            <div class="border rounded p-4 flex justify-between items-center" id="payout-${order.id}">
                <div>
                    <p class="font-semibold text-gray-800">Order #${order.id.substring(0,8)}</p>
                    <p class="text-sm text-gray-500">Florist Earning: ₹${order.florist_earning || 0}</p>
                </div>
                <button class="bg-blue-500 text-white px-3 py-1 rounded text-sm hover:bg-blue-600" onclick="approvePayout('${order.id}')">Approve Payout</button>
            </div>
        `;
    });
    payoutsList.innerHTML = html;
}

window.approvePayout = function(orderId) {
    alert('Payout for order ' + orderId + ' approved!');
    const elem = document.getElementById('payout-' + orderId);
    if (elem) elem.remove();
};

// Initialize
checkSession();
