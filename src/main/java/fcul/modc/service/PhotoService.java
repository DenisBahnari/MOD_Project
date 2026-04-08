package fcul.modc.service;

import fcul.modc.model.Album;
import fcul.modc.model.Photo;
import fcul.modc.model.PhotoMetadata;
import fcul.modc.repository.AlbumRepository;
import fcul.modc.repository.PhotoMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PhotoService {

    private final AlbumRepository albumRepository;
    private final PhotoMetadataRepository photoMetadataRepository;

    public PhotoService(AlbumRepository albumRepository, PhotoMetadataRepository photoMetadataRepository) {
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

}
