// register.js — handles the registration form submission
// Translated from the original Python project's static/js/register.js

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('registerForm');
    const messageEl = document.getElementById('message');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        hideMessage();

        const username = document.getElementById('username').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Client-side validation
        if (password !== confirmPassword) {
            showMessage('Passwords do not match.', 'error');
            return;
        }
        if (password.length < 6) {
            showMessage('Password must be at least 6 characters.', 'error');
            return;
        }

        try {
            const res = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password })
            });

            const data = await res.json();

            if (res.status === 201) {
                showMessage('Account created! Redirecting to login...', 'success');
                setTimeout(() => { window.location.href = '/login'; }, 2000);
            } else {
                showMessage(data.detail || 'Registration failed. Please try again.', 'error');
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
