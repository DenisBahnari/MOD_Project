// Authentication Module
const Auth = {
    async doLogin() {
        const username = document.getElementById('loginUsername').value.trim();
        const password = document.getElementById('loginPassword').value;
        const errEl = document.getElementById('loginError');
        const btn = document.getElementById('loginBtn');

        errEl.classList.remove('visible');

        if (!username || !password) {
            errEl.textContent = 'Please fill in all fields.';
            errEl.classList.add('visible');
            return;
        }

        btn.disabled = true;
        btn.textContent = 'Signing in…';

        try {
            // First, verify credentials using a protected endpoint
            const credentials = btoa(username + ':' + password);
            const authHeader = 'Basic ' + credentials;

            // Test if credentials work by trying to access a protected endpoint
            const testRes = await fetch('/albums?ownerId=0', {
                headers: { 'Authorization': authHeader }
            });

            if (testRes.status === 401) {
                errEl.textContent = 'Invalid username or password.';
                errEl.classList.add('visible');
                btn.disabled = false;
                btn.textContent = 'Sign in';
                return;
            }

            // Fetch user details to get the ID
            const userRes = await fetch('/users/by-username/' + encodeURIComponent(username), {
                headers: { 'Authorization': authHeader }
            });

            if (!userRes.ok) throw new Error('Could not fetch user info');

            const user = await userRes.json();

            // Store authentication data
            localStorage.setItem('authHeader', authHeader);
            localStorage.setItem('userId', user.id);
            localStorage.setItem('username', user.username);

            // Redirect to app
            window.location.href = 'app.html';

        } catch (e) {
            errEl.textContent = 'Could not reach the server. Please try again.';
            errEl.classList.add('visible');
            btn.disabled = false;
            btn.textContent = 'Sign in';
            console.error('Login error:', e);
        }
    },

    async doRegister() {
        const username = document.getElementById('regUsername').value.trim();
        const password = document.getElementById('regPassword').value;
        const errEl = document.getElementById('registerError');
        const btn = document.getElementById('registerBtn');

        errEl.classList.remove('visible');

        if (!username || !password) {
            errEl.textContent = 'Please fill in all fields.';
            errEl.classList.add('visible');
            return;
        }

        if (password.length < 6) {
            errEl.textContent = 'Password must be at least 6 characters long.';
            errEl.classList.add('visible');
            return;
        }

        btn.disabled = true;
        btn.textContent = 'Creating…';

        try {
            const res = await fetch('/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (res.ok) {
                // Registration successful, redirect to login
                this.showPanel('login');
                document.getElementById('loginUsername').value = username;
                document.getElementById('loginPassword').value = '';
                document.getElementById('loginPassword').focus();

                // Show success message
                const successMsg = document.createElement('div');
                successMsg.className = 'error-msg visible';
                successMsg.style.backgroundColor = 'rgba(40,180,100,0.1)';
                successMsg.style.borderColor = 'rgba(40,180,100,0.25)';
                successMsg.style.color = '#5dca8a';
                successMsg.textContent = 'Account created successfully! Please sign in.';

                const loginError = document.getElementById('loginError');
                loginError.parentNode.insertBefore(successMsg, loginError.nextSibling);
                setTimeout(() => successMsg.remove(), 3000);
            } else {
                const errorData = await res.text();
                errEl.textContent = errorData || 'Registration failed. Username may already exist.';
                errEl.classList.add('visible');
            }
        } catch (e) {
            errEl.textContent = 'Could not reach the server.';
            errEl.classList.add('visible');
            console.error('Registration error:', e);
        }

        btn.disabled = false;
        btn.textContent = 'Create account';
    },

    showPanel(name) {
        document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
        document.getElementById(name + 'Panel').classList.add('active');
    },

    init() {
        // Check if already logged in
        if (localStorage.getItem('authHeader') && localStorage.getItem('userId')) {
            window.location.href = 'app.html';
            return;
        }

        // Bind events
        document.getElementById('loginBtn').addEventListener('click', () => this.doLogin());
        document.getElementById('registerBtn').addEventListener('click', () => this.doRegister());
        document.getElementById('showRegisterLink').addEventListener('click', (e) => {
            e.preventDefault();
            this.showPanel('register');
        });
        document.getElementById('backToLoginBtn').addEventListener('click', () => this.showPanel('login'));

        // Enter key support
        document.addEventListener('keydown', (e) => {
            if (e.key !== 'Enter') return;
            if (document.getElementById('loginPanel').classList.contains('active')) {
                this.doLogin();
            } else {
                this.doRegister();
            }
        });
    }
};

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => Auth.init());