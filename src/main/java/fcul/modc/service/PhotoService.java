package fcul.modc.service;

import fcul.modc.exceptions.album.AlbumAccessDeniedException;
import fcul.modc.exceptions.album.AlbumNotFoundException;
import fcul.modc.exceptions.photo.PhotoNotFoundException;
import fcul.modc.exceptions.photo.PhotoOwnerMismatchException;
import fcul.modc.model.Album;
import fcul.modc.model.Photo;
import fcul.modc.model.PhotoMetadata;
import fcul.modc.repository.AlbumRepository;
import fcul.modc.repository.PhotoMetadataRepository;
import fcul.modc.repository.PhotoRepository;
import fcul.modc.repository.UserRepository;
import fcul.modc.requests.photos.CreatePhotoRequest;
import fcul.modc.requests.photos.UpdatePhotoRequest;
import fcul.modc.responses.photos.PhotoResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final PhotoMetadataRepository photoMetadataRepository;
    private final UserRepository userRepository;

    public PhotoService(PhotoRepository photoRepository, AlbumRepository albumRepository,
                        PhotoMetadataRepository photoMetadataRepository, UserRepository userRepository) {
        this.photoRepository = photoRepository;
        this.albumRepository = albumRepository;
        this.photoMetadataRepository = photoMetadataRepository;
        this.userRepository = userRepository;
    }

    public PhotoResponse createPhoto(CreatePhotoRequest request) throws IOException {
        Album album = albumRepository.findById(request.getAlbumId())
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + request.getAlbumId()));

        if (!album.getOwner().getId().equals(request.getOwnerId())) {
            throw new PhotoOwnerMismatchException(
                    "User with id " + request.getOwnerId() + " is not the owner of album id " + album.getId()
            );
        }

        Photo photo = new Photo();
        photo.setData(request.getFile().getBytes());
        photo.setAlbum(album);

        PhotoMetadata metadata = new PhotoMetadata();
        metadata.setFilename(request.getFile().getOriginalFilename());
        metadata.setSize(request.getFile().getSize());
        metadata.setUploadTime(LocalDateTime.now());
        metadata.setDescription(request.getDescription());


        photo.setMetadata(metadata);
        metadata.setPhoto(photo);

        Photo savedPhoto = photoRepository.save(photo);

        return PhotoResponse.from(savedPhoto);
    }

    public PhotoResponse updatePhoto(Long photoId, UpdatePhotoRequest request) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException("Photo not found with id: " + photoId));

        if (!photo.getAlbum().getOwner().getId().equals(request.getOwnerId())) {
            throw new PhotoOwnerMismatchException(
                    "User with id " + request.getOwnerId() + " is not the owner of album id " + photo.getAlbum().getId()
            );
        }

        photo.getMetadata().setDescription(request.getDescription());
        photoMetadataRepository.save(photo.getMetadata());

        return PhotoResponse.from(photo);
    }

    public void deletePhoto(Long photoId, Long ownerId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException("Photo not found with id: " + photoId));

        if (!photo.getAlbum().getOwner().getId().equals(ownerId)) {
            throw new PhotoOwnerMismatchException(
                    "User with id " + ownerId + " is not the owner of album id " + photo.getAlbum().getId()
            );
        }

        photoRepository.delete(photo);
    }

    public PhotoResponse getPhotoById(Long id, Long requesterId) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new PhotoNotFoundException("Photo not found with id: " + id));
        validateAccess(photo.getAlbum(), requesterId);
        return PhotoResponse.from(photo);
    }

    public List<PhotoResponse> getPhotosByAlbum(Long albumId, Long requesterId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + albumId));
        validateAccess(album, requesterId);
        return photoRepository.findByAlbum(album)
                .stream()
                .map(PhotoResponse::from)
                .toList();
    }

    public byte[] getPhotoData(Long photoId, Long requesterId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException("Photo not found with id: " + photoId));
        validateAccess(photo.getAlbum(), requesterId);
        return photo.getData();
    }

    private void validateAccess(Album album, Long requesterId) {
        boolean isOwner = album.getOwner().getId().equals(requesterId);
        boolean isShared = album.getSharedUsers().stream()
                .anyMatch(u -> u.getId().equals(requesterId));
        if (!isOwner && !isShared) {
            throw new AlbumAccessDeniedException(
                    "User with id " + requesterId + " does not have access to album id " + album.getId()
            );
        }
    }
}
