import { element, formatDate, statusBadge } from './dom.js';

/** Render a safe request summary list. @param {HTMLElement} root @param {Array} items @param {boolean} approval @returns {void} */
export function renderRequestList(root, items, approval = false) {
    const list = element('div', 'request-list');
    items.forEach((item) => {
        const article = element('article', 'request-card');
        const heading = element('h2');
        const link = element('a', 'request-card-title', item.leaveType || 'Leave request');
        link.href = `#/${approval ? 'approvals' : 'leave-requests'}/${item.id}`;
        heading.append(link);
        const meta = element('p', 'request-meta', `${formatDate(item.startDate)} — ${formatDate(item.endDate)}`);
        const details = element('div', 'request-card-details');
        if (approval && item.employee) details.append(element('span', 'request-person', item.employee));
        details.append(meta, statusBadge(item.status));
        article.append(heading, details);
        list.append(article);
    });
    root.append(list);
}
