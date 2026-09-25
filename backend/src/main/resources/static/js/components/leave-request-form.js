import { create, update } from '../api/leave-requests.js';
import { element, announce, showError } from './dom.js';

const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];

/** Mount a create/edit leave form. @param {HTMLElement} root @param {object|null} existing @param {Function} onSuccess @returns {Function} cleanup */
export function mountLeaveRequestForm(root, existing, onSuccess) {
    const form = root.querySelector('#request-form');
    const topError = root.querySelector('#request-form-error');
    const submit = root.querySelector('#request-submit');
    const values = {
        leaveType: root.querySelector('#leave-type'), startDate: root.querySelector('#start-date'),
        endDate: root.querySelector('#end-date'), reason: root.querySelector('#reason'),
        partialDay: root.querySelector('#partial-day'), attachment: root.querySelector('#attachment')
    };
    if (existing) {
        values.leaveType.value = existing.leaveType || '';
        values.startDate.value = existing.startDate || '';
        values.endDate.value = existing.endDate || '';
        values.reason.value = existing.reason || '';
        values.partialDay.value = existing.partialDay || '';
        root.querySelector('#form-title').textContent = 'Edit request';
        root.querySelector('#form-kicker').textContent = 'Pending request';
        submit.firstChild.textContent = 'Save changes ';
    }
    const clearErrors = () => {
        topError.hidden = true; topError.textContent = '';
        root.querySelectorAll('.field-error').forEach((field) => { field.textContent = ''; });
    };
    const submitForm = async (event) => {
        event.preventDefault(); clearErrors();
        const file = values.attachment.files[0];
        if (file && (!allowedTypes.includes(file.type) || file.size > 5 * 1024 * 1024)) {
            root.querySelector('#attachment-error').textContent = 'Choose a PDF, JPG, or PNG smaller than 5 MB.';
            values.attachment.focus(); return;
        }
        if (!form.reportValidity()) return;
        submit.disabled = true; submit.setAttribute('aria-busy', 'true');
        const data = { leaveType: values.leaveType.value.trim(), startDate: values.startDate.value, endDate: values.endDate.value, reason: values.reason.value.trim(), partialDay: values.partialDay.value, attachment: file || null };
        try { const result = existing ? await update(existing.id, data) : await create(data); announce(existing ? 'Request updated.' : 'Request submitted.'); onSuccess(result); }
        catch (error) {
            showError(topError, error);
            (error.fieldErrors || []).forEach((field) => { const target = root.querySelector(`#${field.field}-error`); if (target) target.textContent = field.message; });
        } finally { submit.disabled = false; submit.removeAttribute('aria-busy'); }
    };
    form.addEventListener('submit', submitForm);
    return () => form.removeEventListener('submit', submitForm);
}
