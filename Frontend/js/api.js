/**
 * Vaultix REST API Client
 */

// API base URL: configurable at runtime via window.__VAULTIX_CONFIG__.API_BASE_URL
// Falls back to localhost for development, otherwise a secure HTTPS default for production.
const API_BASE_URL = (window.__VAULTIX_CONFIG__ && window.__VAULTIX_CONFIG__.API_BASE_URL) || (location.hostname === 'localhost' ? 'http://localhost:8080/api' : 'https://api.vaultix.example.com/api');

class VaultixApi {

    // Guard promise to prevent concurrent refresh requests
    static refreshPromise = null;

    static getAccessToken() {
        return localStorage.getItem('vaultix_access_token');
    }

    // Refresh token is now stored as an HttpOnly cookie — cannot be read from JS.
    static getRefreshToken() {
        return null; // kept for backwards compatibility but always returns null
    }

    static setTokens(accessToken, refreshToken) {
        if (accessToken) localStorage.setItem('vaultix_access_token', accessToken);
        // refresh token intentionally not stored in localStorage (HttpOnly cookie used)
    }

    static clearTokens() {
        localStorage.removeItem('vaultix_access_token');
        // Can't clear HttpOnly cookie from JS — server will be asked to clear it on logout
        localStorage.removeItem('vaultix_salt');
    }

    static async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        const token = VaultixApi.getAccessToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        const config = {
            ...options,
            headers,
            // Ensure cookies (HttpOnly refresh cookie) are sent for refresh/logout flows
            credentials: 'include'
        };

        let response = await fetch(url, config);

        // Auto token refresh if 401 Unauthorized occurs
        if (response.status === 401) {
            const refreshed = await VaultixApi.refreshAccessToken();
            if (refreshed) {
                const newToken = VaultixApi.getAccessToken();
                if (newToken) headers['Authorization'] = 'Bearer ' + newToken;
                response = await fetch(url, { ...options, headers, credentials: 'include' });
            } else {
                VaultixApi.clearTokens();
                window.location.reload();
                throw new Error("Session expired. Please log in again.");
            }
        }

        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(data.message || data.error || `HTTP error ${response.status}`);
        }

        return data;
    }

    static async refreshAccessToken() {
        // Prevent concurrent refresh calls: reuse in-flight promise
        if (VaultixApi.refreshPromise) {
            return VaultixApi.refreshPromise;
        }

        VaultixApi.refreshPromise = (async () => {
            try {
                // Send the refresh request — cookie will be sent via credentials: 'include'
                const res = await fetch(`${API_BASE_URL}/auth/refresh`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include'
                });

                if (res.ok) {
                    const data = await res.json();
                    // Update access token; refresh token is stored in HttpOnly cookie by the server
                    VaultixApi.setTokens(data.accessToken, null);
                    return true;
                }
                return false;
            } catch (e) {
                return false;
            } finally {
                VaultixApi.refreshPromise = null;
            }
        })();

        return VaultixApi.refreshPromise;
    }

    // ─── Auth Endpoints ──────────────────────────────────────────────
    static async register(username, email, password) {
        // Generate a per-user random salt on the client if crypto helper is available.
        const salt = (typeof VaultixCrypto !== 'undefined' && VaultixCrypto.generateSaltBase64) ? VaultixCrypto.generateSaltBase64() : null;
        return VaultixApi.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ username, email, password, salt })
        });
    }

    static async login(email, password) {
        const data = await VaultixApi.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        // Server sets refresh token as an HttpOnly cookie. Only store access token here.
        VaultixApi.setTokens(data.accessToken, null);
        // Store salt (non-secret) if server returns it so the client can derive keys consistently.
        if (data && data.salt) { localStorage.setItem('vaultix_salt', data.salt); }
        return data;
    }

    static async logout() {
        // Ask server to revoke the refresh cookie/token; cookie is sent via credentials
        await VaultixApi.request('/auth/logout', { method: 'POST' }).catch(() => {});
        VaultixApi.clearTokens();
    }

    // ─── Vault Endpoints ─────────────────────────────────────────────
    static async getVaultItems(category = null, favorite = false, query = null) {
        let url = '/vault';
        const params = new URLSearchParams();
        if (category && category !== 'ALL' && category !== 'FAVORITE' && category !== 'TRASH') params.append('category', category);
        if (favorite || category === 'FAVORITE') params.append('favorite', 'true');
        if (query) params.append('query', query);

        if ([...params].length > 0) url += `?${params.toString()}`;
        return VaultixApi.request(url);
    }

    static async getTrashedItems() {
        return VaultixApi.request('/vault/trash');
    }

    static async createVaultItem(itemData) {
        return VaultixApi.request('/vault', {
            method: 'POST',
            body: JSON.stringify(itemData)
        });
    }

    static async updateVaultItem(id, itemData) {
        return VaultixApi.request(`/vault/${id}`, {
            method: 'PUT',
            body: JSON.stringify(itemData)
        });
    }

    static async toggleFavorite(id) {
        return VaultixApi.request(`/vault/${id}/favorite`, { method: 'PATCH' });
    }

    static async moveToTrash(id) {
        return VaultixApi.request(`/vault/${id}/trash`, { method: 'PATCH' });
    }

    static async restoreFromTrash(id) {
        return VaultixApi.request(`/vault/${id}/restore`, { method: 'PATCH' });
    }

    static async deletePermanently(id) {
        return VaultixApi.request(`/vault/${id}`, { method: 'DELETE' });
    }

    // ─── Audit Endpoints ─────────────────────────────────────────────
    static async getAuditLogs() {
        return VaultixApi.request('/audit-logs');
    }

    // ─── Folder Endpoints ────────────────────────────────────────────
    static async getFolders() {
        return VaultixApi.request('/folders');
    }

    static async createFolder(name, colorCode = '#6366f1') {
        return VaultixApi.request('/folders', {
            method: 'POST',
            body: JSON.stringify({ name, colorCode })
        });
    }

    static async deleteFolder(id) {
        return VaultixApi.request(`/folders/${id}`, { method: 'DELETE' });
    }

    // ─── Version History Endpoints ───────────────────────────────────
    static async getItemVersions(id) {
        return VaultixApi.request(`/vault/${id}/versions`);
    }

    static async rollbackToVersion(id, versionId) {
        return VaultixApi.request(`/vault/${id}/versions/${versionId}/rollback`, { method: 'POST' });
    }
}

