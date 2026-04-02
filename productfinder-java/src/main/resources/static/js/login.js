// login.js — handles the login form submission
// Translated from the original Python project's static/js/login.js

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    const messageEl = document.getElementById('message');

    // If already logged in, go straight to search
    if (localStorage.getItem('token')) {
        window.location.href = '/search';
        return;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideMessage();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        if (!username || !password) {
            showMessage('Please fill in all fields.', 'error');
            return;
        }

        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            const data = await res.json();

            if (res.ok) {
                localStorage.setItem('token', data.accessToken);
                showMessage('Login successful! Redirecting...', 'success');
                setTimeout(() => { window.location.href = '/search'; }, 1000);
            } else {
                showMessage(data.detail || 'Login failed. Please try again.', 'error');
            }
        } catch (err) {
            showMessage('Network error. Is the server running?', 'error');
        }
    });

    function showMessage(text, type) {
        messageEl.textContent = text;
        messageEl.className = 'message ' + type;
        messageEl.style.display = 'block';
    }

    function hideMessage() {
        messageEl.style.display = 'none';
        messageEl.className = 'message';
    }
});
