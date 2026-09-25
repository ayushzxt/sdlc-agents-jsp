import { element, formatDateTime } from './dom.js';

/** Render chronological audit events. @param {HTMLElement} root @param {Array} items @returns {void} */
export function renderHistory(root, items) {
    const list = element('ol', 'history-list');
    items.forEach((item) => {
        const entry = element('li', 'history-item');
        entry.append(element('strong', '', item.eventType), element('time', '', formatDateTime(item.eventTime)));
        entry.append(element('span', 'history-actor', `by ${item.actor}`));
        if (item.comment) entry.append(element('p', '', item.comment));
        list.append(entry);
    });
    root.append(list);
}
