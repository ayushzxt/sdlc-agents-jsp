import * as api from '../api/leave-requests.js';
import { mountLeaveRequestForm } from '../components/leave-request-form.js';
import { renderHistory } from '../components/audit-history.js';
import { renderRequestList } from '../components/leave-request-list.js';
import { clear, element, formatDate, formatDateTime, showError, statusBadge, announce } from '../components/dom.js';

let cleanup = () => {};
function stateMessage(text, className = 'empty-state') { return element('div', className, text); }
function field(label, value) { const wrapper = element('div', 'detail-field'); wrapper.append(element('dt', '', label), element('dd', '', value || '—')); return wrapper; }

async function listView(root) {
    const content = root.querySelector('#requests-content'); const status = root.querySelector('#requests-status');
    status.textContent = 'Loading requests…'; content.append(stateMessage('Loading…', 'loading-state'));
    const controller = new AbortController(); cleanup = () => controller.abort();
    try {
        const response = await api.list(controller.signal); clear(content); status.textContent = `${response.items.length} request${response.items.length === 1 ? '' : 's'}`;
        if (!response.items.length) content.append(stateMessage('No leave requests yet. Your next request starts here.'));
        else renderRequestList(content, response.items);
    } catch (error) { if (error.name !== 'AbortError') { clear(content); showError(content.appendChild(element('div', 'form-error')), error); } }
}

async function formView(root, id) {
    const content = root; let existing = null; const controller = new AbortController();
    if (id) { content.append(stateMessage('Loading request…', 'loading-state')); try { existing = await api.detail(id, controller.signal); if (existing.status !== 'PENDING') throw new Error('Only pending requests can be edited.'); clear(content); } catch (error) { if (error.name !== 'AbortError') { clear(content); content.append(stateMessage(error.message, 'form-error')); } return; } }
    const cleanupForm = mountLeaveRequestForm(content, existing, (result) => { window.location.hash = `#/leave-requests/${result.id}`; });
    cleanup = () => { controller.abort(); cleanupForm(); };
}

async function detailView(root, id, approval = false) {
    const content = root.querySelector('#detail-content') || root; content.append(stateMessage('Loading request…', 'loading-state'));
    const controller = new AbortController(); cleanup = () => controller.abort();
    try {
        const request = await api.detail(id, controller.signal); clear(content);
        const summary = element('div', 'detail-summary'); summary.append(statusBadge(request.status), element('p', '', `${formatDate(request.startDate)} — ${formatDate(request.endDate)}`));
        const details = element('dl', 'detail-grid'); [['Leave type', request.leaveType], ['Employee', request.employee], ['Approver', request.approver], ['Submitted', formatDateTime(request.submittedAt)], ['Reason', request.reason], ['Partial day', request.partialDay || 'Full day']].forEach(([label, value]) => details.append(field(label, value)));
        content.append(summary, details);
        if (request.decisionComment) content.append(element('p', 'decision-note', `Decision note: ${request.decisionComment}`));
        const actions = element('div', 'form-actions');
        if (!approval && request.status === 'PENDING') { const edit = element('a', 'button button-secondary', 'Edit request'); edit.href = `#/leave-requests/${id}/edit`; actions.append(edit); }
        if (!approval && ['PENDING', 'APPROVED'].includes(request.status)) { const cancel = element('button', 'button button-danger', 'Cancel request'); cancel.type = 'button'; cancel.addEventListener('click', async () => { cancel.disabled = true; try { const updated = await api.cancel(id); announce('Request cancelled.'); window.location.hash = `#/leave-requests/${updated.id}`; } catch (error) { showError(content.insertBefore(element('div', 'form-error'), content.firstChild), error); cancel.disabled = false; } }); actions.append(cancel); }
        const historyLink = element('a', 'button button-quiet', 'View history'); historyLink.href = `#/leave-requests/${id}/history`; actions.append(historyLink); content.append(actions);
        if (approval && request.status === 'PENDING') mountDecision(content, id);
    } catch (error) { if (error.name !== 'AbortError') { clear(content); content.append(stateMessage(error.message, 'form-error')); } }
}

function mountDecision(root, id) {
    const section = element('section', 'decision-panel'); section.append(element('h2', '', 'Record a decision'));
    const comment = element('textarea'); comment.id = 'decision-comment'; comment.maxLength = 1000; comment.rows = 3; comment.placeholder = 'Optional note for approval'; const label = element('label', '', 'Comment (optional)'); label.htmlFor = comment.id;
    const rejectComment = element('textarea'); rejectComment.id = 'reject-comment'; rejectComment.maxLength = 1000; rejectComment.rows = 3; rejectComment.placeholder = 'Explain why this request is rejected'; const rejectLabel = element('label', '', 'Rejection comment (required)'); rejectLabel.htmlFor = rejectComment.id;
    const approve = element('button', 'button button-primary', 'Approve'); approve.type = 'button'; const reject = element('button', 'button button-danger', 'Reject'); reject.type = 'button';
    const error = element('div', 'form-error'); error.hidden = true; const approveBox = element('div', 'field'); approveBox.append(label, comment); const rejectBox = element('div', 'field'); rejectBox.append(rejectLabel, rejectComment); const buttons = element('div', 'form-actions'); buttons.append(approve, reject); section.append(approveBox, rejectBox, error, buttons); root.append(section);
    approve.addEventListener('click', async () => { approve.disabled = reject.disabled = true; try { await api.approve(id, comment.value.trim()); window.location.hash = '#/approvals'; } catch (e) { showError(error, e); approve.disabled = reject.disabled = false; } });
    reject.addEventListener('click', async () => { error.hidden = true; if (!rejectComment.value.trim()) { error.textContent = 'A rejection comment is required.'; error.hidden = false; rejectComment.focus(); return; } approve.disabled = reject.disabled = true; try { await api.reject(id, rejectComment.value.trim()); window.location.hash = '#/approvals'; } catch (e) { showError(error, e); approve.disabled = reject.disabled = false; } });
}

async function historyView(root, id) {
    const content = root.querySelector('#detail-content') || root; content.append(stateMessage('Loading history…', 'loading-state')); const controller = new AbortController(); cleanup = () => controller.abort();
    try { const response = await api.history(id, controller.signal); clear(content); if (!response.items.length) content.append(stateMessage('No history recorded yet.')); else renderHistory(content, response.items); } catch (error) { if (error.name !== 'AbortError') { clear(content); content.append(stateMessage(error.message, 'form-error')); } }
}

/** @param {HTMLElement} root @param {{id?: string, mode?: string}} params @returns {Promise<void>} */
export async function mount(root, params) {
    const templateId = params.mode === 'list' ? 'tpl-leave-requests' : params.mode === 'form' ? 'tpl-request-form' : 'tpl-request-detail';
    root.append(document.getElementById(templateId).content.cloneNode(true));
    if (params.mode === 'list') await listView(root); else if (params.mode === 'form') await formView(root.querySelector('.page-section'), params.id); else if (params.mode === 'history') await historyView(root, params.id); else await detailView(root, params.id, params.approval);
}
export function unmount() { cleanup(); cleanup = () => {}; }
