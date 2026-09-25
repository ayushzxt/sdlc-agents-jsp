import { me } from './api/auth.js';
import { ApiError } from './api/client.js';
import * as loginView from './views/login.view.js';
import * as leaveView from './views/leave-requests.view.js';
import * as approvalsView from './views/approvals.view.js';

const routes = { login: loginView, leave: leaveView, approvals: approvalsView };
let active = loginView; let user = null; let navigating = false;
function route() {
    const parts = window.location.hash.replace(/^#\/?/, '').split('/').filter(Boolean);
    if (!parts.length) return { name: 'leave', mode: 'list' };
    if (parts[0] === 'login') return { name: 'login' };
    if (parts[0] === 'approvals') return parts[1] ? { name: 'leave', mode: 'detail', id: parts[1], approval: true } : { name: 'approvals' };
    if (parts[0] === 'leave-requests') {
        if (parts[1] === 'new') return { name: 'leave', mode: 'form' };
        if (parts[1]) return { name: 'leave', mode: parts[2] === 'edit' ? 'form' : parts[2] === 'history' ? 'history' : 'detail', id: parts[1] };
        return { name: 'leave', mode: 'list' };
    }
    return { name: 'leave', mode: 'list' };
}
function setNav() {
    const nav = document.getElementById('main-nav'); nav.hidden = !user;
    nav.querySelectorAll('[data-nav]').forEach((link) => { link.hidden = link.dataset.nav === 'approvals' && !user?.roles?.includes('APPROVER'); link.toggleAttribute('aria-current', window.location.hash.includes(link.dataset.nav)); });
}
function showLogin() { window.location.hash = '#/login'; }
/** Start session-aware hash navigation. @returns {Promise<void>} */
export async function start() {
    try { user = await me(); if (window.location.hash === '#/login') window.location.hash = '#/leave-requests'; }
    catch (error) { if (!(error instanceof ApiError) || error.status !== 401) console.error(error); user = null; showLogin(); }
    window.addEventListener('hashchange', navigate); document.getElementById('logout-button').addEventListener('click', async () => { try { const { logout } = await import('./api/auth.js'); await logout(); } finally { user = null; showLogin(); navigate(); } }); await navigate();
}
async function navigate() {
    if (navigating) return; navigating = true;
    try {
        const selected = route(); if (!user && selected.name !== 'login') { showLogin(); return; }
        if ((selected.name === 'approvals' || selected.approval) && !user?.roles?.includes('APPROVER')) { window.location.hash = '#/leave-requests'; return; }
        active.unmount(); const root = document.getElementById('app'); while (root.firstChild) root.removeChild(root.firstChild);
        active = routes[selected.name]; document.title = selected.name === 'login' ? 'Sign in · Leave desk' : selected.name === 'approvals' ? 'Approvals · Leave desk' : 'My requests · Leave desk'; setNav(); await active.mount(root, { ...selected, onLogin: (signedInUser) => { user = signedInUser; window.location.hash = '#/leave-requests'; } }); root.focus();
    } finally { navigating = false; }
}
