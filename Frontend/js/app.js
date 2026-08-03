/**
 * Vaultix Application Main Controller
 */

let activeMasterKey = null;
let currentCategory = 'ALL';
let currentVaultItems = [];
let currentFolders = [];
let currentFolderId = null;
let searchQuery = '';

document.addEventListener('DOMContentLoaded', () => {
    checkInitialState();
});

function checkInitialState() {
    const token = VaultixApi.getAccessToken();
    const salt = localStorage.getItem('vaultix_salt');

    if (token) {
        // Active session found — ask for Master Password if key not in memory
        if (!activeMasterKey) {
            showToast("Session active. Enter Master Password to decrypt vault.", "info");
        }
        // Stay on auth screen to prompt for master password (no plaintext email stored client-side)
        showAuthScreen();
    } else {
        showAuthScreen();
    }
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <i class="fa-solid ${type === 'success' ? 'fa-circle-check' : 'fa-triangle-exclamation'}"></i>
        <span>${message}</span>
    `;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

// ─── Auth Switch & Form Handlers ─────────────────────────────────────
function switchAuthTab(tab) {
    document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));

    document.getElementById(`tab-${tab}`).classList.add('active');
    document.getElementById(`form-${tab}`).classList.add('active');
}

function togglePasswordVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    const icon = btn.querySelector('i');
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fa-regular fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fa-regular fa-eye';
    }
}

function toggleTextareaVisibility(id, btn) {
    const el = document.getElementById(id);
    const icon = btn.querySelector('i');
    if (el.style.webkitTextSecurity === 'disc') {
        el.style.webkitTextSecurity = 'none';
        icon.className = 'fa-regular fa-eye-slash';
    } else {
        el.style.webkitTextSecurity = 'disc';
        icon.className = 'fa-regular fa-eye';
    }
}

function evaluatePasswordStrength(pwd) {
    const bar = document.getElementById('strength-bar');
    const label = document.getElementById('strength-label');

    let score = 0;
    if (pwd.length >= 8) score += 25;
    if (pwd.length >= 12) score += 25;
    if (/[A-Z]/.test(pwd)) score += 15;
    if (/[0-9]/.test(pwd)) score += 15;
    if (/[^A-Za-z0-9]/.test(pwd)) score += 20;

    bar.style.width = `${score}%`;
    if (score < 40) {
        bar.style.background = 'var(--accent-red)';
        label.innerText = 'Password strength: Weak';
    } else if (score < 75) {
        bar.style.background = 'var(--accent-gold)';
        label.innerText = 'Password strength: Moderate';
    } else {
        bar.style.background = 'var(--accent-green)';
        label.innerText = 'Password strength: Strong';
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    const btn = document.getElementById('btn-login-submit');

    try {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Authenticating...';

        const data = await VaultixApi.login(email, password);

        // Derive Client-Side Master CryptoKey using per-user salt (preferred) or fall back to email
        const salt = (data && data.salt) || localStorage.getItem('vaultix_salt');
        if (salt && VaultixCrypto.deriveKeyFromPassword) {
            activeMasterKey = await VaultixCrypto.deriveKeyFromPassword(password, salt);
        } else {
            activeMasterKey = await VaultixCrypto.deriveKey(password, email);
        }

        showToast("Successfully authenticated & derived Zero-Knowledge key!", "success");
        showAppScreen(email);
    } catch (err) {
        showToast(err.message, "error");
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<span>Unlock Vault</span> <i class="fa-solid fa-arrow-right"></i>';
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('reg-username').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const confirmPassword = document.getElementById('reg-confirm-password').value;
    const btn = document.getElementById('btn-reg-submit');

    if (password !== confirmPassword) {
        showToast("Master passwords do not match", "error");
        return;
    }

    try {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Initializing...';

        await VaultixApi.register(username, email, password);
        const data = await VaultixApi.login(email, password);

        const salt = (data && data.salt) || localStorage.getItem('vaultix_salt');
        if (salt && VaultixCrypto.deriveKeyFromPassword) {
            activeMasterKey = await VaultixCrypto.deriveKeyFromPassword(password, salt);
        } else {
            activeMasterKey = await VaultixCrypto.deriveKey(password, email);
        }

        showToast("Account created successfully!", "success");
        showAppScreen(email);
    } catch (err) {
        showToast(err.message, "error");
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<span>Initialize Zero-Knowledge Vault</span> <i class="fa-solid fa-shield-cat"></i>';
    }
}

async function handleLogout() {
    await VaultixApi.logout();
    activeMasterKey = null;
    showAuthScreen();
    showToast("Signed out safely.", "success");
}

function showAuthScreen() {
    document.getElementById('auth-screen').classList.remove('hidden');
    document.getElementById('app-screen').classList.add('hidden');
}

function showAppScreen(email) {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('app-screen').classList.remove('hidden');

    document.getElementById('display-user-email').innerText = email;
    document.getElementById('user-avatar-initials').innerText = email.substring(0, 2).toUpperCase();

    loadVaultItems();
    loadFolders();
}

// ─── Folder Management ─────────────────────────────────────────────
async function loadFolders() {
    try {
        currentFolders = await VaultixApi.getFolders();
        renderFolders();
        populateFolderSelectors();
        updateStats();
    } catch (err) {
        showToast("Failed to load folders: " + err.message, "error");
    }
}

function renderFolders() {
    const container = document.getElementById('folders-list');
    if (!container) return;

    container.innerHTML = '';

    if (!currentFolders || currentFolders.length === 0) {
        container.innerHTML = '<div class="folder-empty-state">No folders yet. Create one to start organizing your vault.</div>';
        return;
    }

    currentFolders.forEach(folder => {
        const card = document.createElement('div');
        card.className = 'folder-card';
        card.innerHTML = `
            <div class="folder-card-main">
                <div class="folder-icon"><i class="fa-solid fa-folder"></i></div>
                <div>
                    <div class="folder-name">${escapeHtml(folder.name)}</div>
                    <div class="folder-meta">${escapeHtml(folder.colorCode || '#6366f1')} · personal workspace</div>
                </div>
            </div>
            <div class="folder-card-actions">
                <button class="btn-secondary" onclick="selectFolder(${folder.folderId})">View</button>
                <button class="btn-icon" onclick="deleteFolder(${folder.folderId})" title="Delete folder">
                    <i class="fa-solid fa-trash-can"></i>
                </button>
            </div>
        `;
        container.appendChild(card);
    });
}

async function createFolderFromInput() {
    const input = document.getElementById('folder-name-input');
    const name = input ? input.value.trim() : '';

    if (!name) {
        showToast("Please enter a folder name.", "error");
        return;
    }

    try {
        await VaultixApi.createFolder(name);
        showToast("Folder created successfully.", "success");
        if (input) input.value = '';
        await loadFolders();
    } catch (err) {
        showToast("Failed to create folder: " + err.message, "error");
    }
}

async function deleteFolder(id) {
    if (!confirm("Delete this folder?")) return;

    try {
        await VaultixApi.deleteFolder(id);
        showToast("Folder deleted.", "success");
        if (currentFolderId === id) {
            currentFolderId = null;
        }
        await loadFolders();
        loadVaultItems();
    } catch (err) {
        showToast("Failed to delete folder: " + err.message, "error");
    }
}

function populateFolderSelectors() {
    const filterSelect = document.getElementById('folder-filter-select');
    const itemFolderSelect = document.getElementById('item-folder');
    if (!filterSelect || !itemFolderSelect) return;

    filterSelect.innerHTML = '<option value="">All folders</option>';
    itemFolderSelect.innerHTML = '<option value="">Unassigned</option>';

    currentFolders.forEach(folder => {
        const option = document.createElement('option');
        option.value = folder.folderId;
        option.textContent = folder.name;
        if (folder.folderId === currentFolderId) option.selected = true;
        filterSelect.appendChild(option);

        const itemOption = document.createElement('option');
        itemOption.value = folder.folderId;
        itemOption.textContent = folder.name;
        itemFolderSelect.appendChild(itemOption);
    });
}

function handleFolderFilterChange(folderId) {
    currentFolderId = folderId ? Number(folderId) : null;
    loadVaultItems();
}

function clearFolderFilter() {
    currentFolderId = null;
    const filterSelect = document.getElementById('folder-filter-select');
    if (filterSelect) filterSelect.value = '';
    loadVaultItems();
}

function selectFolder(folderId) {
    currentFolderId = folderId;
    const filterSelect = document.getElementById('folder-filter-select');
    if (filterSelect) filterSelect.value = folderId;
    loadVaultItems();
}

// ─── Vault Items Management & Category Filtering ──────────────────
async function loadVaultItems() {
    try {
        if (currentCategory === 'TRASH') {
            currentVaultItems = await VaultixApi.getTrashedItems();
        } else {
            currentVaultItems = await VaultixApi.getVaultItems(currentCategory, false, searchQuery, currentFolderId);
        }
        renderVaultGrid();
        updateStats();
    } catch (err) {
        showToast("Failed to load vault secrets: " + err.message, "error");
    }
}

function renderVaultGrid() {
    const container = document.getElementById('vault-items-grid');
    const emptyState = document.getElementById('empty-vault-state');
    container.innerHTML = '';

    if (!currentVaultItems || currentVaultItems.length === 0) {
        emptyState.classList.remove('hidden');
        document.getElementById('items-count-indicator').innerText = '0 items';
        return;
    }

    emptyState.classList.add('hidden');
    document.getElementById('items-count-indicator').innerText = `${currentVaultItems.length} items`;

    currentVaultItems.forEach(item => {
        const card = document.createElement('div');
        card.className = 'vault-item-card';

        const categoryIcon = getCategoryIcon(item.category);

        card.innerHTML = `
            <div class="item-card-header">
                <div class="item-category-icon">${categoryIcon}</div>
                <div class="item-card-info">
                    <div class="item-card-title">${escapeHtml(item.title)}</div>
                    <div class="item-card-user">${escapeHtml(item.usernameOrIdentifier || 'No identifier')}</div>
                    ${item.folderName ? `<div class="item-card-folder">Folder: ${escapeHtml(item.folderName)}</div>` : ''}
                </div>
            </div>
            <div class="item-card-actions">
                <button class="btn-icon star-btn ${item.isFavorite ? 'starred' : ''}" onclick="toggleFavorite(${item.vaultItemId})" title="Favorite">
                    <i class="fa-${item.isFavorite ? 'solid' : 'regular'} fa-star"></i>
                </button>
                <div style="display: flex; gap: 0.25rem;">
                    ${currentCategory === 'TRASH' ? `
                        <button class="btn-secondary" onclick="restoreItem(${item.vaultItemId})"><i class="fa-solid fa-rotate-left"></i> Restore</button>
                        <button class="btn-icon" style="color: var(--accent-red)" onclick="deletePermanently(${item.vaultItemId})"><i class="fa-solid fa-trash"></i></button>
                    ` : `
                        <button class="btn-secondary" onclick="revealAndCopySecret(${item.vaultItemId})"><i class="fa-regular fa-copy"></i> Copy</button>
                        <button class="btn-icon" onclick="editVaultItem(${item.vaultItemId})"><i class="fa-solid fa-pen-to-square"></i></button>
                        <button class="btn-icon" onclick="trashVaultItem(${item.vaultItemId})"><i class="fa-solid fa-trash-can"></i></button>
                    `}
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

function getCategoryIcon(cat) {
    switch (cat) {
        case 'CREDENTIAL': return '<i class="fa-solid fa-key"></i>';
        case 'SECURE_NOTE': return '<i class="fa-solid fa-note-sticky"></i>';
        case 'PAYMENT_CARD': return '<i class="fa-solid fa-credit-card"></i>';
        case 'API_KEY': return '<i class="fa-solid fa-code"></i>';
        case 'LICENSE': return '<i class="fa-solid fa-certificate"></i>';
        default: return '<i class="fa-solid fa-shield"></i>';
    }
}

function updateStats() {
    const total = currentVaultItems.length;
    const favs = currentVaultItems.filter(i => i.isFavorite).length;
    const folders = currentFolders.length;

    document.getElementById('stat-total-items').innerText = total;
    document.getElementById('stat-fav-items').innerText = favs;
    document.getElementById('stat-folder-count').innerText = folders;
    document.getElementById('badge-count-all').innerText = total;
    document.getElementById('badge-count-fav').innerText = favs;
}

function filterCategory(cat, btn) {
    currentCategory = cat;
    document.querySelectorAll('.sidebar-nav .nav-item').forEach(n => n.classList.remove('active'));
    if (btn) btn.classList.add('active');

    document.getElementById('content-vault').classList.remove('hidden');
    document.getElementById('content-audit').classList.add('hidden');

    document.getElementById('current-view-title').innerText = getCategoryTitle(cat);
    loadVaultItems();
}

function getCategoryTitle(cat) {
    switch (cat) {
        case 'ALL': return 'All Vault Secrets';
        case 'FAVORITE': return 'Starred Favorites';
        case 'CREDENTIAL': return 'Logins & Credentials';
        case 'SECURE_NOTE': return 'Secure Confidential Notes';
        case 'PAYMENT_CARD': return 'Payment Cards & Banking';
        case 'API_KEY': return 'API Keys & Developer Tokens';
        case 'LICENSE': return 'Software Licenses';
        case 'TRASH': return 'Trash Bin';
        default: return 'Vault Secrets';
    }
}

function handleSearch(q) {
    searchQuery = q;
    loadVaultItems();
}

async function revealAndCopySecret(id) {
    const item = currentVaultItems.find(i => i.vaultItemId === id);
    if (!item) return;

    if (!activeMasterKey) {
        showToast("Enter Master Password to decrypt secret.", "error");
        return;
    }

    const decryptedSecret = await VaultixCrypto.decrypt(item.encryptedPayload, item.iv, activeMasterKey);
    navigator.clipboard.writeText(decryptedSecret);
    showToast("Secret decrypted & copied to clipboard!", "success");
}

async function toggleFavorite(id) {
    try {
        await VaultixApi.toggleFavorite(id);
        loadVaultItems();
    } catch (err) {
        showToast(err.message, "error");
    }
}

async function trashVaultItem(id) {
    try {
        await VaultixApi.moveToTrash(id);
        showToast("Item moved to trash.", "info");
        loadVaultItems();
    } catch (err) {
        showToast(err.message, "error");
    }
}

async function restoreItem(id) {
    try {
        await VaultixApi.restoreFromTrash(id);
        showToast("Item restored from trash.", "success");
        loadVaultItems();
    } catch (err) {
        showToast(err.message, "error");
    }
}

async function deletePermanently(id) {
    if (!confirm("Are you sure you want to permanently delete this secret? This cannot be undone.")) return;
    try {
        await VaultixApi.deletePermanently(id);
        showToast("Item deleted permanently.", "success");
        loadVaultItems();
    } catch (err) {
        showToast(err.message, "error");
    }
}

// ─── Modal Editor (Zero-Knowledge Save/Update) ────────────────────
function openItemModal(item = null) {
    document.getElementById('modal-item').classList.remove('hidden');
    document.getElementById('form-item').reset();
    document.getElementById('item-id').value = '';
    const folderSelect = document.getElementById('item-folder');
    if (folderSelect) folderSelect.value = '';
 
    if (item) {
        document.getElementById('modal-item-title').innerHTML = '<i class="fa-solid fa-pen-to-square"></i> Edit Secret Item';
        document.getElementById('item-id').value = item.vaultItemId;
        document.getElementById('item-category').value = item.category;
        document.getElementById('item-title').value = item.title;
        document.getElementById('item-identifier').value = item.usernameOrIdentifier || '';
        document.getElementById('item-url').value = item.url || '';
        if (folderSelect) folderSelect.value = item.folderId || '';
        decryptItemSecretToField(item);
    } else {
        document.getElementById('modal-item-title').innerHTML = '<i class="fa-solid fa-shield-plus"></i> Add Secret Item';
    }
}

async function decryptItemSecretToField(item) {
    if (activeMasterKey) {
        const plaintext = await VaultixCrypto.decrypt(item.encryptedPayload, item.iv, activeMasterKey);
        document.getElementById('item-secret').value = plaintext;
    }
}

function closeItemModal() {
    document.getElementById('modal-item').classList.add('hidden');
}

function adaptItemFields(cat) {
    const labelIdent = document.getElementById('label-identifier');
    if (cat === 'PAYMENT_CARD') labelIdent.innerText = 'Cardholder Name / Card Number';
    else if (cat === 'API_KEY') labelIdent.innerText = 'Key Name / Scope';
    else labelIdent.innerText = 'Username / Identity';
}

async function saveVaultItem(e) {
    e.preventDefault();

    if (!activeMasterKey) {
        showToast("Master Password missing. Please re-login.", "error");
        return;
    }

    const id = document.getElementById('item-id').value;
    const category = document.getElementById('item-category').value;
    const title = document.getElementById('item-title').value.trim();
    const usernameOrIdentifier = document.getElementById('item-identifier').value.trim();
    const url = document.getElementById('item-url').value.trim();
    const secretPlaintext = document.getElementById('item-secret').value;

    // Encrypt secret payload locally before transmission (Zero-Knowledge)
    const { encryptedPayload, iv } = await VaultixCrypto.encrypt(secretPlaintext, activeMasterKey);

    const folderId = document.getElementById('item-folder').value || null;
    const payload = {
        category,
        title,
        usernameOrIdentifier,
        url,
        encryptedPayload,
        iv,
        isFavorite: false,
        folderId: folderId ? Number(folderId) : null
    };

    try {
        if (id) {
            await VaultixApi.updateVaultItem(id, payload);
            showToast("Vault secret updated successfully!", "success");
        } else {
            await VaultixApi.createVaultItem(payload);
            showToast("Vault secret encrypted & saved!", "success");
        }
        closeItemModal();
        loadVaultItems();
    } catch (err) {
        showToast("Failed to save secret: " + err.message, "error");
    }
}

// ─── Secret Generator ─────────────────────────────────────────────
function openGeneratorModal() {
    document.getElementById('modal-generator').classList.remove('hidden');
    generateSecret();
}

function closeGeneratorModal() {
    document.getElementById('modal-generator').classList.add('hidden');
}

function generateSecret() {
    const length = parseInt(document.getElementById('gen-length').value);
    const upper = document.getElementById('gen-uppercase').checked;
    const lower = document.getElementById('gen-lowercase').checked;
    const nums = document.getElementById('gen-numbers').checked;
    const syms = document.getElementById('gen-symbols').checked;

    let chars = '';
    if (upper) chars += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    if (lower) chars += 'abcdefghijklmnopqrstuvwxyz';
    if (nums) chars += '0123456789';
    if (syms) chars += '!@#$%^&*()_+-=[]{}|;:,.<>?';

    if (!chars) chars = 'abcdefghijklmnopqrstuvwxyz0123456789';

    let result = '';
    const randomValues = new Uint32Array(length);
    window.crypto.getRandomValues(randomValues);

    for (let i = 0; i < length; i++) {
        result += chars[randomValues[i] % chars.length];
    }

    document.getElementById('gen-output').value = result;
}

function copyGeneratedSecret() {
    const secret = document.getElementById('gen-output').value;
    navigator.clipboard.writeText(secret);
    showToast("Generated secret copied to clipboard!", "success");
}

// ─── Audit Trail View ─────────────────────────────────────────────
async function switchViewTab(view, btn) {
    document.querySelectorAll('.sidebar-nav .nav-item').forEach(n => n.classList.remove('active'));
    if (btn) btn.classList.add('active');

    if (view === 'AUDIT') {
        document.getElementById('content-vault').classList.add('hidden');
        document.getElementById('content-audit').classList.remove('hidden');
        loadAuditLogs();
    }
}

async function loadAuditLogs() {
    try {
        const logs = await VaultixApi.getAuditLogs();
        const tbody = document.getElementById('audit-logs-tbody');
        tbody.innerHTML = '';

        if (!logs || logs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No audit logs recorded yet.</td></tr>';
            return;
        }

        logs.forEach(log => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><i class="fa-regular fa-clock"></i> ${new Date(log.createdAt).toLocaleString()}</td>
                <td><span class="badge" style="background: var(--primary-light); color: var(--primary); font-weight: 700;">${log.eventType}</span></td>
                <td>${escapeHtml(log.description)}</td>
                <td><code>${log.ipAddress || '127.0.0.1'}</code></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        showToast("Failed to load audit logs: " + err.message, "error");
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/'/g, "&#039;");
}
