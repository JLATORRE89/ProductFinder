// Check if user is logged in
const token = localStorage.getItem('token');
if (!token) {
    window.location.href = '/login';
}

// API helper function
async function apiRequest(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };

    const response = await fetch(url, { ...defaultOptions, ...options });

    if (response.status === 401) {
        localStorage.removeItem('token');
        window.location.href = '/login';
        return null;
    }

    return response;
}

// Load user info
async function loadUserInfo() {
    try {
        const response = await apiRequest('/api/auth/me');
        if (response && response.ok) {
            const user = await response.json();
            document.getElementById('userInfo').textContent = `Welcome, ${user.username}!`;
        }
    } catch (error) {
        console.error('Error loading user info:', error);
    }
}

// Load search history
async function loadSearchHistory() {
    try {
        const response = await apiRequest('/api/search/history');
        if (response && response.ok) {
            const history = await response.json();
            displaySearchHistory(history);
        }
    } catch (error) {
        console.error('Error loading search history:', error);
    }
}

// Display search history
function displaySearchHistory(history) {
    const historyDiv = document.getElementById('history');

    if (history.length === 0) {
        historyDiv.innerHTML = '<p style="color: #666; text-align: center;">No search history yet</p>';
        return;
    }

    historyDiv.innerHTML = history.map(item => {
        const date = new Date(item.created_at).toLocaleDateString();
        const time = new Date(item.created_at).toLocaleTimeString();
        return `
            <div class="history-item" onclick="loadSearch(${item.id})">
                <div class="query">${item.query}</div>
                <div class="meta">${item.product_count} products - ${date} ${time}</div>
            </div>
        `;
    }).join('');
}

// Load a specific search
async function loadSearch(searchId) {
    try {
        const response = await apiRequest(`/api/search/${searchId}`);
        if (response && response.ok) {
            const search = await response.json();
            displayResults(search.products, search.query);
            document.getElementById('searchQuery').value = search.query;
        }
    } catch (error) {
        console.error('Error loading search:', error);
    }
}

// Display search results
function displayResults(products, query) {
    const resultsDiv = document.getElementById('results');

    if (products.length === 0) {
        resultsDiv.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #666;">
                <p>No products found for "${query}"</p>
            </div>
        `;
        return;
    }

    resultsDiv.innerHTML = `
        <h3>Results for "${query}" (${products.length} products)</h3>
        ${products.map(product => `
            <div class="product-card">
                <h4>${product.title}</h4>
                <div class="product-info">
                    ${product.price ? `<span class="product-price">${product.price}</span>` : ''}
                    ${product.status ? `<span class="product-status">${product.status}</span>` : ''}
                </div>
                <a href="${product.url}" target="_blank">View on Walmart →</a>
            </div>
        `).join('')}
    `;
}

// Handle search form submission
document.getElementById('searchForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const query = document.getElementById('searchQuery').value;
    const messageDiv = document.getElementById('message');
    const loadingDiv = document.getElementById('loading');
    const resultsDiv = document.getElementById('results');

    // Clear previous messages and results
    messageDiv.className = 'message';
    messageDiv.textContent = '';
    resultsDiv.innerHTML = '';
    loadingDiv.style.display = 'block';

    try {
        const response = await apiRequest('/api/search/', {
            method: 'POST',
            body: JSON.stringify({ query })
        });

        loadingDiv.style.display = 'none';

        if (response && response.ok) {
            const search = await response.json();
            displayResults(search.products, query);
            loadSearchHistory(); // Refresh history
        } else {
            const data = await response.json();
            messageDiv.className = 'message error';
            messageDiv.textContent = data.detail || 'Search failed';
        }
    } catch (error) {
        loadingDiv.style.display = 'none';
        messageDiv.className = 'message error';
        messageDiv.textContent = 'An error occurred. Please try again.';
        console.error('Error:', error);
    }
});

// Handle logout
document.getElementById('logoutBtn').addEventListener('click', (e) => {
    e.preventDefault();
    localStorage.removeItem('token');
    window.location.href = '/';
});

// Initialize page
loadUserInfo();
loadSearchHistory();
