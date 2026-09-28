const csrfMeta = document.querySelector('meta[name="csrf-token"]');
let csrfToken = csrfMeta?.content || '';

/** Error returned by an API request. */
export class ApiError extends Error {
    constructor(status, message, fieldErrors = [], code = '') {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.fieldErrors = fieldErrors;
        this.code = code;
    }
}

async function readPayload(response) {
    if (response.status === 204) return null;
    const type = response.headers.get('content-type') || '';
    if (!type.includes('json')) return null;
    return response.json();
}

/**
 * Make an authenticated same-origin API request.
 * @param {string} path API path.
 * @param {{method?: string, body?: BodyInit, json?: object, signal?: AbortSignal}} [options] Request options.
 * @returns {Promise<any>} Parsed response body.
 */
export async function request(path, options = {}) {
    const method = options.method || 'GET';
    const headers = new Headers(options.body instanceof FormData ? {} : { Accept: 'application/json' });
    let body = options.body;
    if (options.json !== undefined) {
        headers.set('Content-Type', 'application/json');
        body = JSON.stringify(options.json);
    }
    if (method !== 'GET' && method !== 'HEAD' && csrfToken) headers.set('X-CSRF-TOKEN', csrfToken);
    const response = await fetch(path, { method, headers, body, credentials: 'same-origin', signal: options.signal });
    const payload = await readPayload(response);
    if (!response.ok) {
        const error = payload || {};
        throw new ApiError(response.status, error.message || 'Something went wrong. Please try again.', error.fieldErrors || [], error.code || 'API_ERROR');
    }
    if (payload?.csrfToken) setCsrfToken(payload.csrfToken);
    return payload;
}

/**
 * Store the CSRF token used for session mutations.
 * @param {string} token CSRF token.
 * @returns {void}
 */
export function setCsrfToken(token) {
    csrfToken = token || '';
    if (csrfMeta) csrfMeta.content = csrfToken;
}

/**
 * Load a fresh CSRF token for login or logout.
 * @returns {Promise<object>} CSRF response.
 */
export async function loadCsrf() {
    return request('/api/auth/csrf');
}
