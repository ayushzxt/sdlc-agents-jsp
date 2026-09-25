export function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined && text !== null) node.textContent = text;
    return node;
}

export function clear(node) {
    while (node.firstChild) node.removeChild(node.firstChild);
}

export function statusBadge(status) {
    return element('span', `status status-${String(status).toLowerCase()}`, status);
}

export function formatDate(value) {
    if (!value) return '—';
    return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`));
}

export function formatDateTime(value) {
    if (!value) return '—';
    return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
}

export function announce(message) {
    const region = document.getElementById('live-region');
    if (region) region.textContent = message;
}

export function showError(node, error) {
    node.hidden = false;
    node.textContent = error?.message || 'Something went wrong. Please try again.';
}
