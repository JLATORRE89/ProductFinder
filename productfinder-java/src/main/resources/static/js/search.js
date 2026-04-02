// search.js — main product search page logic
// Translated from the original Python project's static/js/search.js

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');

    // Redirect unauthenticated users to login
    if (!token) {
        window.location.href = '/login';
        return;
    }

    const searchForm    = document.getElementById('searchForm');
    const searchInput   = document.getElementById('searchQuery');
    const resultsEl     = document.getElementById('results');
    const historyEl     = document.getElementById('history');
    const loadingEl     = document.getElementById('loading');
    const messageEl     = document.getElementById('message');
    const logoutBtn     = document.getElementById('logoutBtn');
    const userInfoEl    = document.getElementById('userInfo');

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    async function apiRequest(url, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token,
            ...(options.headers || {})
        };
        const res = await fetch(url, { ...options, headers });
        if (res.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return res;
    }

    function showMessage(text, type) {
        messageEl.textContent = text;
        messageEl.className = 'message ' + type;
        messageEl.style.display = 'block';
    }

    function hideMessage() {
        messageEl.style.display = 'none';
    }

    function formatDate(isoString) {
        const d = new Date(isoString);
        return d.toLocaleDateString() + ' ' + d.toLocaleTimeString();
    }

    // ----------------------------------------------------------------
    // Load user info
    // ----------------------------------------------------------------

    async function loadUserInfo() {
        try {
            const res = await apiRequest('/api/auth/me');
            if (res.ok) {
                const user = await res.json();
                userInfoEl.textContent = 'Welcome, ' + user.username + '!';
            }
        } catch (err) {
            // Non-critical — just don't show the welcome message
        }
    }

    // ----------------------------------------------------------------
    // Search history
    // ----------------------------------------------------------------

    async function loadSearchHistory() {
        try {
            const res = await apiRequest('/api/search/history?limit=20');
            if (res.ok) {
                const history = await res.json();
                displaySearchHistory(history);
            }
        } catch (err) {
            historyEl.innerHTML = '<p class="no-history">Could not load history.</p>';
        }
    }

    function displaySearchHistory(history) {
        if (!history.length) {
            historyEl.innerHTML = '<p class="no-history">No search history yet.</p>';
            return;
        }

        historyEl.innerHTML = history.map(item => `
            <div class="history-item" onclick="loadSearch(${item.id})">
                <button class="delete-btn"
                        onclick="deleteSearch(event, ${item.id})"
                        title="Delete this search">&#x2715;</button>
                <div class="history-query">${escapeHtml(item.query)}</div>
                <div class="history-meta">
                    ${item.productCount} product${item.productCount !== 1 ? 's' : ''} &middot;
                    ${formatDate(item.createdAt)}
                </div>
            </div>
        `).join('');
    }

    // ----------------------------------------------------------------
    // Load a saved search
    // ----------------------------------------------------------------

    window.loadSearch = async function(searchId) {
        try {
            const res = await apiRequest('/api/search/' + searchId);
            if (res.ok) {
                const data = await res.json();
                searchInput.value = data.query;
                hideMessage();
                displayResults(data.products, data.query);
            }
        } catch (err) {
            showMessage('Could not load search results.', 'error');
        }
    };

    // ----------------------------------------------------------------
    // Delete a saved search
    // ----------------------------------------------------------------

    window.deleteSearch = async function(event, searchId) {
        event.stopPropagation(); // Don't trigger loadSearch
        try {
            const res = await apiRequest('/api/search/' + searchId, { method: 'DELETE' });
            if (res.status === 204) {
                loadSearchHistory();
                // Clear results if the displayed search was deleted
                if (resultsEl.dataset.searchId == searchId) {
                    resultsEl.innerHTML = '';
                    resultsEl.dataset.searchId = '';
                }
            }
        } catch (err) {
            showMessage('Could not delete search.', 'error');
        }
    };

    // ----------------------------------------------------------------
    // Display product results
    // ----------------------------------------------------------------

    function displayResults(products, query) {
        resultsEl.innerHTML = '';

        if (!products.length) {
            resultsEl.innerHTML = `<p class="no-results">No products found for "<strong>${escapeHtml(query)}</strong>".<br>
                Walmart may have blocked the request — try a different query or check back later.</p>`;
            return;
        }

        resultsEl.innerHTML = products.map(p => `
            <div class="product-card">
                <div class="product-title">${escapeHtml(p.title)}</div>
                ${p.price && p.price !== 'N/A' ? `<div class="product-price">${escapeHtml(p.price)}</div>` : ''}
                ${p.status ? `<div class="product-status">${escapeHtml(p.status)}</div>` : ''}
                <a href="${escapeHtml(p.url)}" target="_blank" rel="noopener noreferrer">
                    View on Walmart &rarr;
                </a>
            </div>
        `).join('');
    }

    // ----------------------------------------------------------------
    // Search form submit
    // ----------------------------------------------------------------

    searchForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideMessage();
        resultsEl.innerHTML = '';

        const query = searchInput.value.trim();
        if (!query) return;

        loadingEl.style.display = 'block';

        try {
            const res = await apiRequest('/api/search/', {
                method: 'POST',
                body: JSON.stringify({ query })
            });

            loadingEl.style.display = 'none';

            if (res.status === 201) {
                const data = await res.json();
                resultsEl.dataset.searchId = data.id;
                displayResults(data.products, query);
                loadSearchHistory();
            } else {
                const err = await res.json();
                showMessage(err.detail || 'Search failed.', 'error');
            }
        } catch (err) {
            loadingEl.style.display = 'none';
            showMessage('Network error. Is the server running?', 'error');
        }
    });

    // ----------------------------------------------------------------
    // Logout
    // ----------------------------------------------------------------

    logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('token');
        window.location.href = '/';
    });

    // ----------------------------------------------------------------
    // XSS prevention helper
    // ----------------------------------------------------------------

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // ----------------------------------------------------------------
    // Init
    // ----------------------------------------------------------------

    loadUserInfo();
    loadSearchHistory();
});
