// Main Application Logic
const App = {
    state: {
        currentAlbumId: null,
        currentPhotoId: null,
        albums: [],
        photos: []
    },

    async init() {
        // Check authentication
        const authHeader = localStorage.getItem('authHeader');
        const userId = localStorage.getItem('userId');
        const username = localStorage.getItem('username');

        if (!authHeader || !userId) {
            window.location.href = 'index.html';
            return;
        }

        // Set user info
        document.getElementById('userNameEl').textContent = username || 'User';
        document.getElementById('userAvatar').textContent = (username || 'U')[0].toUpperCase();

        // Bind events
        this.bindEvents();

        // Load initial data
        await this.loadAlbums();
    },

    bindEvents() {
        // Sidebar buttons
        document.getElementById('newAlbumBtn').addEventListener('click', () => this.openNewAlbumModal());
        document.getElementById('signOutBtn').addEventListener('click', () => this.logout());

        // Topbar buttons
        document.getElementById('editAlbumBtn').addEventListener('click', () => this.openEditAlbumModal());
        document.getElementById('deleteAlbumBtn').addEventListener('click', () => this.deleteCurrentAlbum());
        document.getElementById('uploadPhotoBtn').addEventListener('click', () => this.openUploadModal());

        // Modal buttons
        document.getElementById('createAlbumBtn').addEventListener('click', () => this.createAlbum());
        document.getElementById('cancelNewAlbumBtn').addEventListener('click', () => this.closeModal('newAlbumModal'));
        document.getElementById('saveAlbumBtn').addEventListener('click', () => this.saveAlbumEdit());
        document.getElementById('cancelEditAlbumBtn').addEventListener('click', () => this.closeModal('editAlbumModal'));
        document.getElementById('uploadBtn').addEventListener('click', () => this.uploadPhoto());
        document.getElementById('cancelUploadBtn').addEventListener('click', () => this.closeModal('uploadModal'));
        document.getElementById('deletePhotoBtn').addEventListener('click', () => this.deleteCurrentPhoto());
        document.getElementById('lightboxCloseBtn').addEventListener('click', () => this.closeLightbox());

        // Upload area
        const uploadArea = document.getElementById('uploadArea');
        const fileInput = document.getElementById('fileInput');

        uploadArea.addEventListener('click', () => fileInput.click());
        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.classList.add('dragover');
        });
        uploadArea.addEventListener('dragleave', () => uploadArea.classList.remove('dragover'));
        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
            const file = e.dataTransfer.files[0];
            if (file && file.type.startsWith('image/')) {
                this.selectedFile = file;
                document.getElementById('uploadPreview').textContent = file.name;
            }
        });

        fileInput.addEventListener('change', (e) => {
            if (e.target.files[0]) {
                this.selectedFile = e.target.files[0];
                document.getElementById('uploadPreview').textContent = this.selectedFile.name;
            }
        });

        // Close modals on overlay click
        document.querySelectorAll('.modal-overlay').forEach(overlay => {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) overlay.classList.remove('open');
            });
        });

        // Lightbox click outside
        document.getElementById('lightbox').addEventListener('click', (e) => {
            if (e.target === document.getElementById('lightbox')) this.closeLightbox();
        });

        // Escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                document.querySelectorAll('.modal-overlay.open').forEach(m => m.classList.remove('open'));
                this.closeLightbox();
            }
        });
    },

    async loadAlbums() {
        try {
            const userId = localStorage.getItem('userId');
            const response = await API.get(`/albums?ownerId=${userId}`);
            if (!response.ok) throw new Error('Failed to load albums');

            this.state.albums = await response.json();
            this.renderAlbumList();
        } catch (error) {
            this.showToast('Failed to load albums', 'error');
            console.error('Error loading albums:', error);
        }
    },

    renderAlbumList() {
        const el = document.getElementById('albumList');
        if (!this.state.albums.length) {
            el.innerHTML = '<div style="padding:20px 10px;color:var(--muted);font-size:13px;text-align:center;">No albums yet.<br/>Click + to create one.</div>';
            return;
        }

        el.innerHTML = this.state.albums.map(album => `
            <div class="album-item ${album.id === this.state.currentAlbumId ? 'active' : ''}" data-album-id="${album.id}">
                <div class="album-dot"></div>
                <span class="album-name">${this.escapeHtml(album.name)}</span>
            </div>
        `).join('');

        // Add click handlers
        el.querySelectorAll('.album-item').forEach(item => {
            item.addEventListener('click', () => {
                const albumId = parseInt(item.dataset.albumId);
                this.selectAlbum(albumId);
            });
        });
    },

    async selectAlbum(id) {
        this.state.currentAlbumId = id;
        this.renderAlbumList();

        const album = this.state.albums.find(a => a.id === id);
        document.getElementById('mainTitle').textContent = album ? album.name : 'Album';
        document.getElementById('topbarActions').style.display = 'flex';

        await this.loadPhotos(id);
    },

    async loadPhotos(albumId) {
        const content = document.getElementById('mainContent');
        content.innerHTML = '<div style="padding:40px;color:var(--muted);font-size:14px;text-align:center;">Loading photos…</div>';

        try {
            const response = await API.get(`/photos/album/${albumId}`);
            if (!response.ok) throw new Error('Failed to load photos');

            this.state.photos = await response.json();
            document.getElementById('mainSub').textContent = this.state.photos.length
                ? `${this.state.photos.length} photo${this.state.photos.length !== 1 ? 's' : ''}`
                : '';

            if (!this.state.photos.length) {
                content.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-icon">🖼️</div>
                        <h3>No photos yet</h3>
                        <p>Upload your first photo using the button above.</p>
                    </div>`;
                return;
            }

            content.innerHTML = `<div class="photo-grid">${this.state.photos.map(photo => this.photoCard(photo)).join('')}</div>`;

            // Load thumbnails
            await this.loadPhotoThumbs();
        } catch (error) {
            content.innerHTML = '<div style="padding:40px;color:var(--muted);font-size:14px;text-align:center;">Failed to load photos.</div>';
            console.error('Error loading photos:', error);
        }
    },

    photoCard(photo) {
        const name = photo.metadata?.filename || `Photo #${photo.id}`;
        const size = photo.metadata?.size ? this.formatSize(photo.metadata.size) : '';
        return `
            <div class="photo-card" data-photo-id="${photo.id}" data-photo-name="${this.escapeHtml(name)}" data-photo-size="${size}">
                <div class="photo-thumb-placeholder" id="thumb-${photo.id}">⏳</div>
                <div class="photo-info">
                    <div class="photo-name">${this.escapeHtml(name)}</div>
                    <div class="photo-meta">${size}</div>
                </div>
            </div>`;
    },

    async loadPhotoThumbs() {
        for (const photo of this.state.photos) {
            const card = document.querySelector(`.photo-card[data-photo-id="${photo.id}"]`);
            if (!card) continue;

            card.addEventListener('click', () => {
                const name = card.dataset.photoName;
                const size = card.dataset.photoSize;
                this.openLightbox(photo.id, name, size);
            });

            const thumbEl = document.getElementById(`thumb-${photo.id}`);
            if (!thumbEl) continue;

            try {
                const response = await API.get(`/photos/${photo.id}/data`);
                if (response.ok) {
                    const blob = await response.blob();
                    const url = URL.createObjectURL(blob);
                    thumbEl.outerHTML = `<img class="photo-thumb" src="${url}" alt="" loading="lazy" />`;
                }
            } catch (error) {
                console.error('Error loading thumbnail:', error);
                thumbEl.textContent = '🖼️';
            }
        }
    },

    async createAlbum() {
        const name = document.getElementById('newAlbumName').value.trim();
        if (!name) {
            this.showToast('Album name is required', 'error');
            return;
        }

        const description = document.getElementById('newAlbumDesc').value.trim();
        const userId = localStorage.getItem('userId');

        try {
            const response = await API.post('/albums', {
                name,
                description,
                ownerId: parseInt(userId)
            });

            if (!response.ok) throw new Error('Failed to create album');

            this.closeModal('newAlbumModal');
            this.showToast('Album created', 'success');
            await this.loadAlbums();

            // Select the new album
            const newAlbum = this.state.albums.find(a => a.name === name);
            if (newAlbum) this.selectAlbum(newAlbum.id);
        } catch (error) {
            this.showToast('Could not create album', 'error');
            console.error('Error creating album:', error);
        }
    },

    async saveAlbumEdit() {
        const name = document.getElementById('editAlbumName').value.trim();
        const description = document.getElementById('editAlbumDesc').value.trim();

        if (!name) {
            this.showToast('Name is required', 'error');
            return;
        }

        try {
            const response = await API.put(`/albums/${this.state.currentAlbumId}`, {
                name,
                description
            });

            if (!response.ok) throw new Error('Failed to update album');

            this.closeModal('editAlbumModal');
            this.showToast('Album updated', 'success');
            await this.loadAlbums();
            document.getElementById('mainTitle').textContent = name;
        } catch (error) {
            this.showToast('Could not update album', 'error');
            console.error('Error updating album:', error);
        }
    },

    async deleteCurrentAlbum() {
        if (!confirm('Delete this album and all its photos? This cannot be undone.')) return;

        try {
            const response = await API.delete(`/albums/${this.state.currentAlbumId}`);
            if (!response.ok) throw new Error('Failed to delete album');

            this.state.currentAlbumId = null;
            document.getElementById('mainTitle').textContent = 'Select an album';
            document.getElementById('mainSub').textContent = '';
            document.getElementById('topbarActions').style.display = 'none';
            document.getElementById('mainContent').innerHTML = `
                <div class="empty-state">
                    <div class="empty-icon">📷</div>
                    <h3>Your photos live here</h3>
                    <p>Select an album from the sidebar, or create a new one to get started.</p>
                </div>`;

            this.showToast('Album deleted', 'success');
            await this.loadAlbums();
        } catch (error) {
            this.showToast('Could not delete album', 'error');
            console.error('Error deleting album:', error);
        }
    },

    openNewAlbumModal() {
        document.getElementById('newAlbumName').value = '';
        document.getElementById('newAlbumDesc').value = '';
        this.openModal('newAlbumModal');
    },

    openEditAlbumModal() {
        const album = this.state.albums.find(a => a.id === this.state.currentAlbumId);
        if (!album) return;

        document.getElementById('editAlbumName').value = album.name;
        document.getElementById('editAlbumDesc').value = album.description || '';
        this.openModal('editAlbumModal');
    },

    openUploadModal() {
        this.selectedFile = null;
        document.getElementById('uploadPreview').textContent = '';
        document.getElementById('uploadDesc').value = '';
        document.getElementById('fileInput').value = '';
        this.openModal('uploadModal');
    },

    // Replace the uploadPhoto method in App object
    async uploadPhoto() {
        console.log('=== UPLOAD PHOTO START ===');
        console.log('Current album ID:', this.state.currentAlbumId);
        console.log('Selected file:', this.selectedFile);

        if (!this.selectedFile) {
            console.error('No file selected');
            this.showToast('Please select a photo first', 'error');
            return;
        }

        console.log('File details:', {
            name: this.selectedFile.name,
            size: this.selectedFile.size,
            type: this.selectedFile.type,
            lastModified: new Date(this.selectedFile.lastModified)
        });

        // Validate file type
        if (!this.selectedFile.type.startsWith('image/')) {
            console.error('Invalid file type:', this.selectedFile.type);
            this.showToast('Please select an image file', 'error');
            return;
        }

        // Validate file size (max 10MB)
        const maxSize = 10 * 1024 * 1024; // 10MB
        if (this.selectedFile.size > maxSize) {
            console.error('File too large:', this.selectedFile.size, 'bytes');
            this.showToast('File size must be less than 10MB', 'error');
            return;
        }

        const btn = document.getElementById('uploadBtn');
        btn.disabled = true;
        btn.textContent = 'Uploading…';

        const formData = new FormData();
        formData.append('file', this.selectedFile);
        formData.append('albumId', this.state.currentAlbumId);
        formData.append('ownerId', localStorage.getItem('userId'));

        const description = document.getElementById('uploadDesc').value.trim();
        if (description) {
            console.log('Adding description:', description);
            formData.append('description', description);
        }

        console.log('FormData created, sending to /photos');

        try {
            const response = await API.post('/photos', formData);
            console.log('Upload response received:', response);

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Upload failed with status:', response.status);
                console.error('Error response:', errorText);
                throw new Error(`Upload failed: ${response.status} - ${errorText}`);
            }

            const result = await response.json();
            console.log('Upload success:', result);

            this.closeModal('uploadModal');
            this.showToast('Photo uploaded!', 'success');
            await this.loadPhotos(this.state.currentAlbumId);
            console.log('=== UPLOAD PHOTO END (SUCCESS) ===');
        } catch (error) {
            console.error('=== UPLOAD PHOTO ERROR ===');
            console.error('Error details:', error);
            this.showToast(`Upload failed: ${error.message}`, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Upload';
        }
    },

    async openLightbox(photoId, name, meta) {
        this.state.currentPhotoId = photoId;
        document.getElementById('lightboxName').textContent = name;
        document.getElementById('lightboxMeta').textContent = meta;
        document.getElementById('lightboxImg').src = '';
        document.getElementById('lightbox').classList.add('open');

        try {
            const response = await API.get(`/photos/${photoId}/data`);
            if (response.ok) {
                const blob = await response.blob();
                document.getElementById('lightboxImg').src = URL.createObjectURL(blob);
            }
        } catch (error) {
            console.error('Error loading image:', error);
        }
    },

    closeLightbox() {
        document.getElementById('lightbox').classList.remove('open');
        this.state.currentPhotoId = null;
    },

    async deleteCurrentPhoto() {
        if (!confirm('Delete this photo? This cannot be undone.')) return;

        const userId = localStorage.getItem('userId');

        try {
            const response = await API.delete(`/photos/${this.state.currentPhotoId}?ownerId=${userId}`);
            if (!response.ok) throw new Error('Failed to delete photo');

            this.closeLightbox();
            this.showToast('Photo deleted', 'success');
            await this.loadPhotos(this.state.currentAlbumId);
        } catch (error) {
            this.showToast('Could not delete photo', 'error');
            console.error('Error deleting photo:', error);
        }
    },

    logout() {
        localStorage.clear();
        window.location.href = 'index.html';
    },

    openModal(id) {
        document.getElementById(id).classList.add('open');
    },

    closeModal(id) {
        document.getElementById(id).classList.remove('open');
    },

    showToast(message, type = '') {
        const toast = document.getElementById('toast');
        toast.textContent = message;
        toast.className = `toast ${type} show`;
        setTimeout(() => toast.classList.remove('show'), 2800);
    },

    escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;')
                  .replace(/</g, '&lt;')
                  .replace(/>/g, '&gt;')
                  .replace(/"/g, '&quot;');
    },

    formatSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }
};

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => App.init());