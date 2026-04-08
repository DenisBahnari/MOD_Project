package fcul.modc.service;

import fcul.modc.model.Album;
import fcul.modc.model.Photo;
import fcul.modc.model.PhotoMetadata;
import fcul.modc.repository.AlbumRepository;
import fcul.modc.repository.PhotoMetadataRepository;
import fcul.modc.repository.PhotoRepository;
import fcul.modc.responses.photos.PhotoResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final PhotoMetadataRepository photoMetadataRepository;

    public PhotoService(PhotoRepository photoRepository, AlbumRepository albumRepository, PhotoMetadataRepository photoMetadataRepository) {
        this.photoRepository = photoRepository;
        this.albumRepository = albumRepository;
        this.photoMetadataRepository = photoMetadataRepository;
    }

    public PhotoMetadata uploadPhoto(Long albumId, MultipartFile file) throws IOException {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        // Create metadata
        PhotoMetadata metadata = new PhotoMetadata();
        metadata.setFilename(file.getOriginalFilename());
        metadata.setSize(file.getSize());
        metadata.setUploadTime(LocalDateTime.now());

        // Create data
        Photo photo = new Photo();
        photo.setData(file.getBytes());
        photo.setMetadata(metadata);
        photo.setAlbum(album);

        metadata.setPhoto(photo);

        // Save
        return photoMetadataRepository.save(metadata);
    }

    public PhotoResponse getPhotoById(Long id) {
        Photo photo = photoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Photo not found with id: " + id));

        return PhotoResponse.from(photo);
    }

    public List<PhotoResponse> getPhotosByAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + albumId));

        return photoRepository.findByAlbum(album)
                .stream()
                .map(PhotoResponse::from)
                .toList();
    }

    // Returns raw bytes so the controller can stream it back
    public byte[] getPhotoData(Long id) {
        Photo photo = photoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Photo not found with id: " + id));

        return photo.getData();
    }
}
