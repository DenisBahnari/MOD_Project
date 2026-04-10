// Main Application Logic
const App = {
    state: {
        currentAlbumId: null,
        currentAlbumIsShared: false,
        currentPhotoId: null,
        albums: [],
        photos: []
    },
    userToShare: null,
    userToRemove: null,
    selectedFile: null,

    async init() {
        const authHeader = localStorage.getItem('authHeader');
        const userId = localStorage.getItem('userId');
        const username = localStorage.getItem('username');

        console.log('App.init - Checking auth:', { hasAuth: !!authHeader, hasUserId: !!userId });

        if (!authHeader || !userId) {
            window.location.href = 'index.html';
            return;
        }

        const userNameEl = document.getElementById('userNameEl');
        const userAvatar = document.getElementById('userAvatar');
        if (userNameEl) userNameEl.textContent = username || 'User';
        if (userAvatar) userAvatar.textContent = (username || 'U')[0].toUpperCase();

        this.bindEvents();
        await this.loadAlbums();
    },

    bindEvents() {
        document.getElementById('newAlbumBtn')?.addEventListener('click', () => this.openNewAlbumModal());
        document.getElementById('signOutBtn')?.addEventListener('click', () => this.logout());
        document.getElementById('editAlbumBtn')?.addEventListener('click', () => this.openEditAlbumModal());
        document.getElementById('deleteAlbumBtn')?.addEventListener('click', () => this.deleteCurrentAlbum());
        document.getElementById('uploadPhotoBtn')?.addEventListener('click', () => this.openUploadModal());
        document.getElementById('shareAlbumBtn')?.addEventListener('click', () => this.openShareModal());
        document.getElementById('viewSharedBtn')?.addEventListener('click', () => this.viewSharedUsers());

        document.getElementById('createAlbumBtn')?.addEventListener('click', () => this.createAlbum());
        document.getElementById('cancelNewAlbumBtn')?.addEventListener('click', () => this.closeModal('newAlbumModal'));
        document.getElementById('saveAlbumBtn')?.addEventListener('click', () => this.saveAlbumEdit());
        document.getElementById('cancelEditAlbumBtn')?.addEventListener('click', () => this.closeModal('editAlbumModal'));
        document.getElementById('uploadBtn')?.addEventListener('click', () => this.uploadPhoto());
        document.getElementById('cancelUploadBtn')?.addEventListener('click', () => this.closeModal('uploadModal'));
        document.getElementById('deletePhotoBtn')?.addEventListener('click', () => this.deleteCurrentPhoto());
        document.getElementById('lightboxCloseBtn')?.addEventListener('click', () => this.closeLightbox());

        // Share modal
        document.getElementById('confirmShareButton')?.addEventListener('click', () => this.confirmShare());
        document.getElementById('cancelShareButton')?.addEventListener('click', () => this.closeModal('shareAlbumModal'));

        // Remove user modal
        document.getElementById('confirmRemoveButton')?.addEventListener('click', () => this.confirmRemoveUser());
        document.getElementById('cancelRemoveButton')?.addEventListener('click', () => this.closeModal('removeUserModal'));

        document.getElementById('closeSharedUsersBtn')?.addEventListener('click', () => this.closeModal('sharedUsersModal'));

        // Upload area
        const uploadArea = document.getElementById('uploadArea');
        const fileInput = document.getElementById('fileInput');
        if (uploadArea) {
            uploadArea.addEventListener('click', () => fileInput?.click());
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
                    const preview = document.getElementById('uploadPreview');
                    if (preview) preview.textContent = file.name;
                }
            });
        }
        if (fileInput) {
            fileInput.addEventListener('change', (e) => {
                if (e.target.files?.[0]) {
                    this.selectedFile = e.target.files[0];
                    const preview = document.getElementById('uploadPreview');
                    if (preview) preview.textContent = this.selectedFile.name;
                }
            });
        }

        document.querySelectorAll('.modal-overlay').forEach(overlay => {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) overlay.classList.remove('open');
            });
        });

        const lightbox = document.getElementById('lightbox');
        if (lightbox) {
            lightbox.addEventListener('click', (e) => {
                if (e.target === lightbox) this.closeLightbox();
            });
        }

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                document.querySelectorAll('.modal-overlay.open').forEach(m => m.classList.remove('open'));
                this.closeLightbox();
            }
        });
    },

    async loadAlbums() {
        try {
            const userId = API.getCurrentUserId();
            console.log('Loading owned albums...');
            const ownedResponse = await API.get(`/albums?ownerId=${userId}`);
            const ownedAlbums = await ownedResponse.json();

            let sharedAlbums = [];
            try {
                const sharedResponse = await API.get(`/albums/shared?userId=${userId}`);
                if (sharedResponse.ok) {
                    sharedAlbums = await sharedResponse.json();
                }
            } catch (e) {
                console.warn('Could not load shared albums', e);
            }

            const allAlbums = [
                ...ownedAlbums.map(a => ({ ...a, isShared: false })),
                ...sharedAlbums.map(a => ({ ...a, isShared: true }))
            ];
            this.state.albums = allAlbums;
            this.renderAlbumList();
        } catch (error) {
            this.showToast('Failed to load albums', 'error');
            console.error(error);
        }
    },

    renderAlbumList() {
        const el = document.getElementById('albumList');
        if (!el) return;

        if (!this.state.albums.length) {
            el.innerHTML = '<div style="padding:20px 10px;color:var(--muted);font-size:13px;text-align:center;">No albums yet.<br/>Click + to create one.</div>';
            return;
        }

        el.innerHTML = this.state.albums.map(album => `
            <div class="album-item ${album.id === this.state.currentAlbumId ? 'active' : ''} ${album.isShared ? 'shared' : ''}" data-album-id="${album.id}" data-is-shared="${album.isShared}">
                <div class="album-dot"></div>
                <span class="album-name">${this.escapeHtml(album.name)}</span>
                ${album.isShared ? '<span class="album-type">shared</span>' : ''}
            </div>
        `).join('');

        el.querySelectorAll('.album-item').forEach(item => {
            item.addEventListener('click', () => {
                const albumId = parseInt(item.dataset.albumId);
                const isShared = item.dataset.isShared === 'true';
                this.selectAlbum(albumId, isShared);
            });
        });
    },

    async selectAlbum(id, isShared = false) {
        this.state.currentAlbumId = id;
        this.state.currentAlbumIsShared = isShared;
        this.renderAlbumList();

        try {
            const response = await API.get(`/albums/${id}`);
            const album = await response.json();
            const mainTitle = document.getElementById('mainTitle');
            if (mainTitle) mainTitle.textContent = album.name;

            const topbarActions = document.getElementById('topbarActions');
            const editBtn = document.getElementById('editAlbumBtn');
            const deleteBtn = document.getElementById('deleteAlbumBtn');
            const uploadBtn = document.getElementById('uploadPhotoBtn');
            const shareBtn = document.getElementById('shareAlbumBtn');
            const viewSharedBtn = document.getElementById('viewSharedBtn');

            if (!isShared) {
                if (topbarActions) topbarActions.style.display = 'flex';
                if (editBtn) editBtn.style.display = 'inline-block';
                if (deleteBtn) deleteBtn.style.display = 'inline-block';
                if (uploadBtn) uploadBtn.style.display = 'inline-block';
                if (shareBtn) shareBtn.style.display = 'inline-block';
                if (viewSharedBtn) viewSharedBtn.style.display = 'inline-block';
            } else {
                if (topbarActions) topbarActions.style.display = 'flex';
                if (editBtn) editBtn.style.display = 'none';
                if (deleteBtn) deleteBtn.style.display = 'none';
                if (uploadBtn) uploadBtn.style.display = 'none';
                if (shareBtn) shareBtn.style.display = 'none';
                if (viewSharedBtn) viewSharedBtn.style.display = 'none';
            }

            await this.loadPhotos(id);
        } catch (error) {
            this.showToast('Could not load album', 'error');
            console.error(error);
        }
    },

    async openShareModal() {
        this.userToShare = null;
        const usernameInput = document.getElementById('shareUsernameInput');
        const infoPanel = document.getElementById('shareUserInfoPanel');
        if (usernameInput) usernameInput.value = '';
        if (infoPanel) infoPanel.style.display = 'none';

        this.openModal('shareAlbumModal');

        const input = document.getElementById('shareUsernameInput');
        if (input) {
            let timeout;
            input.oninput = () => {
                clearTimeout(timeout);
                timeout = setTimeout(() => this.lookupUserForShare(input.value), 500);
            };
        }
    },

    async lookupUserForShare(username) {
        if (!username || username.length < 3) {
            document.getElementById('shareUserInfoPanel').style.display = 'none';
            this.userToShare = null;
            return;
        }
        try {
            const response = await API.get(`/users/by-username/${encodeURIComponent(username)}`);
            if (response.ok) {
                const user = await response.json();
                document.getElementById('shareUsernameDisplay').textContent = user.username;
                document.getElementById('shareUserIdDisplay').textContent = user.id;
                document.getElementById('shareUserInfoPanel').style.display = 'block';
                this.userToShare = { id: user.id, username: user.username };
            } else {
                document.getElementById('shareUserInfoPanel').style.display = 'none';
                this.userToShare = null;
            }
        } catch {
            document.getElementById('shareUserInfoPanel').style.display = 'none';
            this.userToShare = null;
        }
    },

    async confirmShare() {
        if (!this.userToShare) {
            this.showToast('Please enter a valid username', 'error');
            return;
        }
        if (this.userToShare.id === API.getCurrentUserId()) {
            this.showToast('You cannot share an album with yourself', 'error');
            return;
        }
        try {
            const response = await API.post(`/albums/${this.state.currentAlbumId}/users`, {
                ownerId: API.getCurrentUserId(),
                userId: this.userToShare.id
            });
            if (response.ok) {
                this.showToast(`Album shared with ${this.userToShare.username}`, 'success');
                this.closeModal('shareAlbumModal');
                await this.loadAlbums();
            } else {
                const error = await response.text();
                this.showToast(`Failed to share: ${error}`, 'error');
            }
        } catch (error) {
            this.showToast('Failed to share album', 'error');
            console.error('Error sharing album:', error);
        }
    },

    async viewSharedUsers() {
        this.openModal('sharedUsersModal');
        const listContainer = document.getElementById('sharedUsersList');
        if (!listContainer) return;
        listContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: var(--muted);">Loading...</div>';

        try {
            // Simplified view – backend does not have a "get shared users" endpoint yet
            listContainer.innerHTML = `
                <div style="padding: 20px;">
                    <div style="margin-bottom: 20px;">
                        <div class="shared-user-item">
                            <div class="shared-user-info">
                                <div class="shared-user-avatar">👤</div>
                                <div class="shared-user-details">
                                    <div class="shared-user-name">${this.escapeHtml(localStorage.getItem('username'))}</div>
                                    <div class="shared-user-role">Owner</div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div style="text-align: center; padding: 20px; background: var(--accent-dim); border-radius: 8px;">
                        <p>Use the buttons below to add or remove users.</p>
                    </div>
                    <div style="display: flex; gap: 10px; margin-top: 20px;">
                        <button class="btn" id="addUserFromListBtn">Add User</button>
                        <button class="btn" id="removeUserFromListBtn">Remove User</button>
                    </div>
                </div>
            `;
            document.getElementById('addUserFromListBtn')?.addEventListener('click', () => {
                this.closeModal('sharedUsersModal');
                this.openShareModal();
            });
            document.getElementById('removeUserFromListBtn')?.addEventListener('click', () => {
                this.closeModal('sharedUsersModal');
                this.openRemoveUserModal();
            });
        } catch (error) {
            listContainer.innerHTML = '<div style="padding:20px;color:var(--danger);">Failed to load users</div>';
        }
    },

    openRemoveUserModal() {
        this.userToRemove = null;
        const input = document.getElementById('removeUsernameInput');
        const infoPanel = document.getElementById('removeUserInfoPanel');
        if (input) input.value = '';
        if (infoPanel) infoPanel.style.display = 'none';
        this.openModal('removeUserModal');

        const usernameInput = document.getElementById('removeUsernameInput');
        if (usernameInput) {
            let timeout;
            usernameInput.oninput = () => {
                clearTimeout(timeout);
                timeout = setTimeout(() => this.lookupUserForRemoval(usernameInput.value), 500);
            };
        }
    },

    async lookupUserForRemoval(username) {
        if (!username || username.length < 3) {
            document.getElementById('removeUserInfoPanel').style.display = 'none';
            this.userToRemove = null;
            return;
        }
        try {
            const response = await API.get(`/users/by-username/${encodeURIComponent(username)}`);
            if (response.ok) {
                const user = await response.json();
                document.getElementById('removeUsernameDisplay').textContent = user.username;
                document.getElementById('removeUserIdDisplay').textContent = user.id;
                document.getElementById('removeUserInfoPanel').style.display = 'block';
                this.userToRemove = { id: user.id, username: user.username };
            } else {
                document.getElementById('removeUserInfoPanel').style.display = 'none';
                this.userToRemove = null;
            }
        } catch {
            document.getElementById('removeUserInfoPanel').style.display = 'none';
            this.userToRemove = null;
        }
    },

    async confirmRemoveUser() {
        if (!this.userToRemove) {
            this.showToast('Please enter a valid username', 'error');
            return;
        }
        if (this.userToRemove.id === API.getCurrentUserId()) {
            this.showToast('You cannot remove yourself as owner', 'error');
            return;
        }
        try {
            const response = await API.delete(`/albums/${this.state.currentAlbumId}/users`, {
                ownerId: API.getCurrentUserId(),
                userId: this.userToRemove.id
            });
            if (response.ok) {
                this.showToast(`Removed ${this.userToRemove.username} from album`, 'success');
                this.closeModal('removeUserModal');
                await this.loadAlbums();
            } else {
                const error = await response.text();
                this.showToast(`Failed to remove: ${error}`, 'error');
            }
        } catch (error) {
            this.showToast('Failed to remove user', 'error');
            console.error('Error removing user:', error);
        }
    },

    async loadPhotos(albumId) {
        const content = document.getElementById('mainContent');
        if (!content) return;
        content.innerHTML = '<div style="padding:40px;color:var(--muted);text-align:center;">Loading photos…</div>';

        try {
            const response = await API.get(`/photos/album/${albumId}`);
            this.state.photos = await response.json();
            const mainSub = document.getElementById('mainSub');
            if (mainSub) {
                mainSub.textContent = this.state.photos.length
                    ? `${this.state.photos.length} photo${this.state.photos.length !== 1 ? 's' : ''}`
                    : '';
            }

            if (!this.state.photos.length) {
                content.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-icon">🖼️</div>
                        <h3>No photos yet</h3>
                        <p>${!this.state.currentAlbumIsShared ? 'Upload your first photo using the button above.' : 'This album has no photos yet.'}</p>
                    </div>`;
                return;
            }

            content.innerHTML = `<div class="photo-grid">${this.state.photos.map(p => this.photoCard(p)).join('')}</div>`;
            await this.loadPhotoThumbs();
        } catch (error) {
            content.innerHTML = '<div style="padding:40px;color:var(--muted);text-align:center;">Failed to load photos.</div>';
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
            if (card) {
                card.addEventListener('click', () => {
                    const name = card.dataset.photoName;
                    const size = card.dataset.photoSize;
                    this.openLightbox(photo.id, name, size);
                });
            }
            const thumbEl = document.getElementById(`thumb-${photo.id}`);
            if (!thumbEl) continue;
            try {
                const response = await API.get(`/photos/${photo.id}/data`);
                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                thumbEl.outerHTML = `<img class="photo-thumb" src="${url}" alt="" loading="lazy" />`;
            } catch {
                thumbEl.textContent = '🖼️';
            }
        }
    },

    async createAlbum() {
        const name = document.getElementById('newAlbumName')?.value.trim();
        if (!name) {
            this.showToast('Album name is required', 'error');
            return;
        }
        const description = document.getElementById('newAlbumDesc')?.value.trim() || '';
        const ownerId = parseInt(localStorage.getItem('userId'));

        try {
            const response = await API.post('/albums', { name, description, ownerId });
            if (!response.ok) throw new Error('Failed to create album');
            this.closeModal('newAlbumModal');
            this.showToast('Album created', 'success');
            await this.loadAlbums();
            const newAlbum = this.state.albums.find(a => a.name === name);
            if (newAlbum) this.selectAlbum(newAlbum.id, false);
        } catch {
            this.showToast('Could not create album', 'error');
        }
    },

    async saveAlbumEdit() {
        const name = document.getElementById('editAlbumName')?.value.trim();
        if (!name) {
            this.showToast('Name is required', 'error');
            return;
        }
        const description = document.getElementById('editAlbumDesc')?.value.trim() || '';
        try {
            const response = await API.put(`/albums/${this.state.currentAlbumId}`, { name, description });
            if (!response.ok) throw new Error();
            this.closeModal('editAlbumModal');
            this.showToast('Album updated', 'success');
            await this.loadAlbums();
            const mainTitle = document.getElementById('mainTitle');
            if (mainTitle) mainTitle.textContent = name;
        } catch {
            this.showToast('Could not update album', 'error');
        }
    },

    async deleteCurrentAlbum() {
        if (this.state.currentAlbumIsShared) {
            this.showToast('Cannot delete a shared album', 'error');
            return;
        }
        if (!confirm('Delete this album and all its photos? This cannot be undone.')) return;
        try {
            const response = await API.delete(`/albums/${this.state.currentAlbumId}`);
            if (!response.ok) throw new Error();
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
        } catch {
            this.showToast('Could not delete album', 'error');
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

    async uploadPhoto() {
        if (!this.selectedFile) {
            this.showToast('Please select a photo first', 'error');
            return;
        }
        if (!this.selectedFile.type.startsWith('image/')) {
            this.showToast('Please select an image file', 'error');
            return;
        }
        const maxSize = 10 * 1024 * 1024;
        if (this.selectedFile.size > maxSize) {
            this.showToast('File size must be less than 10MB', 'error');
            return;
        }

        const btn = document.getElementById('uploadBtn');
        if (btn) {
            btn.disabled = true;
            btn.textContent = 'Uploading…';
        }

        const formData = new FormData();
        formData.append('file', this.selectedFile);
        formData.append('albumId', this.state.currentAlbumId);
        formData.append('ownerId', localStorage.getItem('userId'));
        const description = document.getElementById('uploadDesc')?.value.trim() || '';
        if (description) formData.append('description', description);

        try {
            const response = await API.post('/photos', formData);
            if (!response.ok) throw new Error('Upload failed');
            this.closeModal('uploadModal');
            this.showToast('Photo uploaded!', 'success');
            await this.loadPhotos(this.state.currentAlbumId);
        } catch (error) {
            this.showToast(`Upload failed: ${error.message}`, 'error');
        } finally {
            if (btn) {
                btn.disabled = false;
                btn.textContent = 'Upload';
            }
        }
    },

    async openLightbox(photoId, name, meta) {
        this.state.currentPhotoId = photoId;
        document.getElementById('lightboxName').textContent = name;
        document.getElementById('lightboxMeta').textContent = meta;
        const img = document.getElementById('lightboxImg');
        img.src = '';
        document.getElementById('lightbox').classList.add('open');
        try {
            const response = await API.get(`/photos/${photoId}/data`);
            const blob = await response.blob();
            img.src = URL.createObjectURL(blob);
            img.onload = () => URL.revokeObjectURL(img.src);
        } catch (error) {
            console.error('Error loading image', error);
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
            if (!response.ok) throw new Error();
            this.closeLightbox();
            this.showToast('Photo deleted', 'success');
            await this.loadPhotos(this.state.currentAlbumId);
        } catch {
            this.showToast('Could not delete photo', 'error');
        }
    },

    logout() {
        localStorage.clear();
        window.location.href = 'index.html';
    },

    openModal(id) {
        document.getElementById(id)?.classList.add('open');
    },

    closeModal(id) {
        document.getElementById(id)?.classList.remove('open');
    },

    showToast(message, type = '') {
        const toast = document.getElementById('toast');
        if (!toast) return;
        toast.textContent = message;
        toast.className = `toast ${type} show`;
        setTimeout(() => toast.classList.remove('show'), 2800);
    },

    escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    },

    formatSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }
};

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => App.init());
} else {
    App.init();
}