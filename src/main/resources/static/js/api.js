// API Service - Handles all HTTP requests with authentication
const API = {
    getAuthHeader() {
        const authHeader = localStorage.getItem('authHeader');
        if (!authHeader) {
            console.error('API: No authentication token found');
            throw new Error('No authentication token found');
        }
        console.log('API: Auth header present (length:', authHeader.length, ')');
        return authHeader;
    },

    async request(endpoint, options = {}) {
        console.log(`API.request: ${options.method || 'GET'} ${endpoint}`);

        const headers = {
            'Authorization': this.getAuthHeader(),
            ...(options.headers || {})
        };

        // Only add Content-Type for non-FormData requests
        if (!(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
            console.log('API.request: Setting Content-Type to application/json');
        } else {
            console.log('API.request: Using FormData, letting browser set Content-Type');
            // Log FormData contents for debugging
            console.log('API.request: FormData contents:');
            for (let pair of options.body.entries()) {
                if (pair[0] === 'file') {
                    console.log(`  - ${pair[0]}: ${pair[1].name} (${pair[1].size} bytes, type: ${pair[1].type})`);
                } else {
                    console.log(`  - ${pair[0]}: ${pair[1]}`);
                }
            }
        }

        const config = {
            ...options,
            headers
        };

        try {
            console.log('API.request: Sending request...');
            const response = await fetch(endpoint, config);
            console.log(`API.request: Response status: ${response.status} ${response.statusText}`);

            // Log response headers
            console.log('API.request: Response headers:', {
                'content-type': response.headers.get('content-type'),
                'content-length': response.headers.get('content-length')
            });

            // Handle unauthorized
            if (response.status === 401) {
                console.error('API.request: Unauthorized - clearing session');
                localStorage.clear();
                window.location.href = 'index.html';
                throw new Error('Session expired. Please login again.');
            }

            return response;
        } catch (error) {
            console.error('API.request: Network error:', error);
            throw error;
        }
    },

    async get(endpoint) {
        console.log(`API.get: ${endpoint}`);
        return this.request(endpoint);
    },

    async post(endpoint, data) {
        console.log(`API.post: ${endpoint}`);
        const options = {
            method: 'POST',
            body: data instanceof FormData ? data : JSON.stringify(data)
        };
        return this.request(endpoint, options);
    },

    async put(endpoint, data) {
        console.log(`API.put: ${endpoint}`);
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    async delete(endpoint) {
        console.log(`API.delete: ${endpoint}`);
        return this.request(endpoint, { method: 'DELETE' });
    }
};