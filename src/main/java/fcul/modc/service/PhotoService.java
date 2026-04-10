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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PhotoService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final PhotoMetadataRepository photoMetadataRepository;

    public PhotoService(PhotoRepository photoRepository, AlbumRepository albumRepository,
                        PhotoMetadataRepository photoMetadataRepository) {
        this.photoRepository = photoRepository;
        this.albumRepository = albumRepository;
        this.photoMetadataRepository = photoMetadataRepository;
    }

    @Transactional
    public PhotoResponse createPhoto(CreatePhotoRequest request) throws IOException {
        logger.info("=== PHOTO SERVICE: createPhoto START ===");
        logger.info("Request: albumId={}, ownerId={}", request.getAlbumId(), request.getOwnerId());

        // Validate album exists
        logger.info("Looking for album with ID: {}", request.getAlbumId());
        Album album = albumRepository.findById(request.getAlbumId())
                .orElseThrow(() -> {
                    logger.error("Album not found with id: {}", request.getAlbumId());
                    return new AlbumNotFoundException("Album not found with id: " + request.getAlbumId());
                });
        logger.info("Album found: ID={}, Name={}, Owner ID={}", album.getId(), album.getName(), album.getOwner().getId());

        // Check ownership
        logger.info("Verifying ownership - Album owner ID: {}, Request owner ID: {}",
                album.getOwner().getId(), request.getOwnerId());
        if (!album.getOwner().getId().equals(request.getOwnerId())) {
            logger.error("Owner mismatch! Album owner: {}, Request owner: {}",
                    album.getOwner().getId(), request.getOwnerId());
            throw new PhotoOwnerMismatchException(
                    "User with id " + request.getOwnerId() + " is not the owner of album id " + album.getId()
            );
        }
        logger.info("Ownership verified successfully");

        // Create photo entity
        Photo photo = new Photo();
        logger.info("Reading file bytes...");
        byte[] fileBytes = request.getFile().getBytes();
        logger.info("File bytes read: {} bytes", fileBytes.length);
        photo.setData(fileBytes);
        photo.setAlbum(album);

        // Create metadata
        PhotoMetadata metadata = new PhotoMetadata();
        metadata.setFilename(request.getFile().getOriginalFilename());
        metadata.setSize(request.getFile().getSize());
        metadata.setUploadTime(LocalDateTime.now());
        metadata.setDescription(request.getDescription() != null ? request.getDescription() : "");

        logger.info("Metadata created: filename={}, size={}, description={}",
                metadata.getFilename(), metadata.getSize(), metadata.getDescription());

        // Set bidirectional relationship
        photo.setMetadata(metadata);
        metadata.setPhoto(photo);

        // Save photo (cascade will save metadata)
        logger.info("Saving photo to database...");
        Photo savedPhoto = photoRepository.save(photo);
        logger.info("Photo saved with ID: {}", savedPhoto.getId());

        if (savedPhoto.getMetadata() != null) {
            logger.info("Metadata saved with ID: {}", savedPhoto.getMetadata().getId());
        }

        logger.info("=== PHOTO SERVICE: createPhoto END (SUCCESS) ===");
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

    // Access not being validated on purpose.
    public byte[] getPhotoData(Long photoId, Long requesterId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException("Photo not found with id: " + photoId));

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
