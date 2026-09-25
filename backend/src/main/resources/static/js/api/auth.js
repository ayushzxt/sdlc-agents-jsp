import { loadCsrf, request } from './client.js';

/** @typedef {{username: string, roles: string[], csrfToken: string}} CurrentUser */

/**
 * Sign in and establish the server session.
 * @param {string} username Username.
 * @param {string} password Password.
 * @returns {Promise<CurrentUser>} Current user.
 */
export async function login(username, password) {
    await loadCsrf();
    return request('/api/auth/login', { method: 'POST', json: { username, password } });
}

/**
 * Get the current session identity.
 * @param {AbortSignal} [signal] Abort signal.
 * @returns {Promise<CurrentUser>} Current user.
 */
export function me(signal) {
    return request('/api/auth/me', { signal });
}

/**
 * End the current session.
 * @returns {Promise<null>} Empty response.
 */
export function logout() {
    return request('/api/auth/logout', { method: 'POST' });
}
