import { request } from './client.js';

/** @typedef {{id: string, employee: string, approver: string, leaveType: string, startDate: string, endDate: string, reason: string, partialDay: string|null, attachment: object|null, status: string, submittedAt: string, updatedAt: string, decidedAt: string|null, decidedBy: string|null, decisionComment: string|null}} LeaveRequest */
/** @typedef {{id: string, employee: string, leaveType: string, startDate: string, endDate: string, status: string}} LeaveRequestSummary */
/** @typedef {{id: string, eventType: string, actor: string, eventTime: string, comment: string|null}} AuditEvent */

function formBody(values) {
    const body = new FormData();
    ['leaveType', 'startDate', 'endDate', 'reason', 'partialDay'].forEach((key) => {
        if (values[key]) body.append(key, values[key]);
    });
    if (values.attachment) body.append('attachment', values.attachment);
    return body;
}

/** @param {AbortSignal} [signal] @returns {Promise<{items: LeaveRequestSummary[]}>} */
export function list(signal) { return request('/api/leave-requests', { signal }); }
/** @param {string} id @param {AbortSignal} [signal] @returns {Promise<LeaveRequest>} */
export function detail(id, signal) { return request(`/api/leave-requests/${encodeURIComponent(id)}`, { signal }); }
/** @param {AbortSignal} [signal] @returns {Promise<{items: AuditEvent[]}>} */
export function history(id, signal) { return request(`/api/leave-requests/${encodeURIComponent(id)}/history`, { signal }); }
/** @param {object} values @returns {Promise<LeaveRequest>} */
export function create(values) { return request('/api/leave-requests', { method: 'POST', body: formBody(values) }); }
/** @param {string} id @param {object} values @returns {Promise<LeaveRequest>} */
export function update(id, values) { return request(`/api/leave-requests/${encodeURIComponent(id)}`, { method: 'PATCH', body: formBody(values) }); }
/** @param {string} id @returns {Promise<LeaveRequest>} */
export function cancel(id) { return request(`/api/leave-requests/${encodeURIComponent(id)}/cancel`, { method: 'POST' }); }
/** @param {string} status @param {string} startDate @param {string} endDate @param {AbortSignal} [signal] @returns {Promise<{items: LeaveRequestSummary[]}>} */
export function approvals(status, startDate, endDate, signal) {
    const params = new URLSearchParams();
    if (status) params.set('status', status);
    if (startDate) params.set('startDate', startDate);
    if (endDate) params.set('endDate', endDate);
    return request(`/api/leave-requests${params.toString() ? `?${params}` : ''}`, { signal });
}
/** @param {string} id @param {string} comment @returns {Promise<LeaveRequest>} */
export function approve(id, comment) { return request(`/api/approvals/leave-requests/${encodeURIComponent(id)}/approve`, { method: 'POST', json: comment ? { comment } : {} }); }
/** @param {string} id @param {string} comment @returns {Promise<LeaveRequest>} */
export function reject(id, comment) { return request(`/api/approvals/leave-requests/${encodeURIComponent(id)}/reject`, { method: 'POST', json: { comment } }); }
