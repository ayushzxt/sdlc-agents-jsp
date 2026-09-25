import { login } from '../api/auth.js';
import { ApiError } from '../api/client.js';
import { announce, showError } from '../components/dom.js';

let cleanup = () => {};
/** @param {HTMLElement} root @param {object} params @returns {Promise<void>} */
export async function mount(root, params) {
    const template = document.getElementById('tpl-login');
    root.append(template.content.cloneNode(true));
    const form = root.querySelector('#login-form'); const error = root.querySelector('#login-error');
    const submit = form.querySelector('button[type="submit"]');
    const onSubmit = async (event) => {
        event.preventDefault(); error.hidden = true;
        if (!form.reportValidity()) return;
        submit.disabled = true;
        try { const user = await login(form.username.value.trim(), form.password.value); announce('Signed in.'); params.onLogin(user); }
        catch (apiError) { showError(error, apiError instanceof ApiError ? apiError : { message: 'Unable to sign in right now.' }); }
        finally { submit.disabled = false; }
    };
    form.addEventListener('submit', onSubmit); cleanup = () => form.removeEventListener('submit', onSubmit);
}
export function unmount() { cleanup(); cleanup = () => {}; }
