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
            const credentials = btoa(username + ':' + password);
            const authHeader = 'Basic ' + credentials;

            // Verify credentials by calling a protected endpoint
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

            // Fetch user details
            const userRes = await fetch('/users/by-username/' + encodeURIComponent(username), {
                headers: { 'Authorization': authHeader }
            });

            if (!userRes.ok) throw new Error('Could not fetch user info');
            const user = await userRes.json();

            localStorage.setItem('authHeader', authHeader);
            localStorage.setItem('userId', user.id);
            localStorage.setItem('username', user.username);

            window.location.href = 'app.html';
        } catch (e) {
            errEl.textContent = 'Could not reach the server. Please try again.';
            errEl.classList.add('visible');
            btn.disabled = false;
            btn.textContent = 'Sign in';
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
                Auth.showPanel('login');
                document.getElementById('loginUsername').value = username;
                document.getElementById('loginPassword').value = '';
                document.getElementById('loginPassword').focus();

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
        }

        btn.disabled = false;
        btn.textContent = 'Create account';
    },

    showPanel(name) {
        document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
        document.getElementById(name + 'Panel').classList.add('active');
    },

    init() {
        localStorage.removeItem('authHeader');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');

        const loginBtn = document.getElementById('loginBtn');
        const registerBtn = document.getElementById('registerBtn');
        const showRegisterLink = document.getElementById('showRegisterLink');
        const backToLoginBtn = document.getElementById('backToLoginBtn');

        if (loginBtn) loginBtn.addEventListener('click', () => Auth.doLogin());
        if (registerBtn) registerBtn.addEventListener('click', () => Auth.doRegister());
        if (showRegisterLink) {
            showRegisterLink.addEventListener('click', (e) => {
                e.preventDefault();
                Auth.showPanel('register');
            });
        }
        if (backToLoginBtn) {
            backToLoginBtn.addEventListener('click', () => Auth.showPanel('login'));
        }

        document.addEventListener('keydown', (e) => {
            if (e.key !== 'Enter') return;
            if (document.getElementById('loginPanel').classList.contains('active')) {
                Auth.doLogin();
            } else {
                Auth.doRegister();
            }
        });
    }
};

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => Auth.init());
} else {
    Auth.init();
}