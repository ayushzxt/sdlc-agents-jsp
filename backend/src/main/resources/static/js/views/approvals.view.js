import * as api from '../api/leave-requests.js';
import { renderRequestList } from '../components/leave-request-list.js';
import { clear, element, showError } from '../components/dom.js';

let cleanup = () => {};
/** @param {HTMLElement} root @param {object} params @returns {Promise<void>} */
export async function mount(root, params) {
    root.append(document.getElementById('tpl-approvals').content.cloneNode(true));
    const content = root.querySelector('#approvals-content'); const status = root.querySelector('#approvals-status'); const form = root.querySelector('#approval-filters'); const controller = new AbortController(); cleanup = () => { controller.abort(); form.removeEventListener('submit', submit); };
    async function load() {
        clear(content); content.append(element('div', 'loading-state', 'Loading requests…')); status.textContent = '';
        try { const response = await api.approvals(form.status.value, form.startDate.value, form.endDate.value, controller.signal); clear(content); status.textContent = `${response.items.length} request${response.items.length === 1 ? '' : 's'}`; if (!response.items.length) content.append(element('div', 'empty-state', 'No requests match these filters.')); else renderRequestList(content, response.items, true); }
        catch (error) { if (error.name !== 'AbortError') { clear(content); showError(content.appendChild(element('div', 'form-error')), error); } }
    }
    function submit(event) { event.preventDefault(); load(); }
    form.addEventListener('submit', submit); await load();
}
export function unmount() { cleanup(); cleanup = () => {}; }
