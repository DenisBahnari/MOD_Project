// API Service - Handles all HTTP requests with authentication
const API = {
    getAuthHeader() {
        const authHeader = localStorage.getItem('authHeader');
        if (!authHeader) {
            console.error('API: No authentication token found');
            throw new Error('No authentication token found');
        }
        return authHeader;
    },

    getCurrentUserId() {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            console.error('API: No user ID found');
            throw new Error('No user ID found');
        }
        return parseInt(userId);
    },

    // Helper to add requesterId to URL if needed
    addRequesterId(url, method = 'GET') {
        // Only add requesterId to GET and DELETE requests
        if (method !== 'GET' && method !== 'DELETE') {
            return url;
        }

        // Check if URL already has requesterId or userId
        if (url.includes('requesterId') || url.includes('userId')) {
            return url;
        }

        // For /albums/shared, backend expects 'userId' not 'requesterId'
        if (url.includes('/albums/shared')) {
            const separator = url.includes('?') ? '&' : '?';
            return `${url}${separator}userId=${this.getCurrentUserId()}`;
        }

        const separator = url.includes('?') ? '&' : '?';
        return `${url}${separator}requesterId=${this.getCurrentUserId()}`;
    },

    async request(endpoint, options = {}) {
        const method = options.method || 'GET';

        let finalEndpoint = this.addRequesterId(endpoint, method);
        console.log(`API.request: ${method} ${finalEndpoint}`);

        const headers = {
            'Authorization': this.getAuthHeader()
        };

        if (!(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        if (options.headers) {
            Object.assign(headers, options.headers);
        }

        const config = {
            ...options,
            headers,
            credentials: 'include'
        };

        try {
            const response = await fetch(finalEndpoint, config);

            if (response.status === 401) {
                console.error('API.request: Unauthorized - clearing session');
                localStorage.clear();
                window.location.href = 'index.html';
                throw new Error('Session expired. Please login again.');
            }

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText.substring(0, 200)}`);
            }

            return response;
        } catch (error) {
            console.error('API.request error:', error);
            throw error;
        }
    },

    async get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    async post(endpoint, data) {
        const options = {
            method: 'POST',
            body: data instanceof FormData ? data : JSON.stringify(data)
        };
        return this.request(endpoint, options);
    },

    async put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    async delete(endpoint, data = null) {
        const options = { method: 'DELETE' };
        if (data) {
            options.body = JSON.stringify(data);
            options.headers = { 'Content-Type': 'application/json' };
        }
        return this.request(endpoint, options);
    }
};