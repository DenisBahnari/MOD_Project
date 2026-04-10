package fcul.modc.controller;

import fcul.modc.requests.photos.CreatePhotoRequest;
import fcul.modc.requests.photos.UpdatePhotoRequest;
import fcul.modc.responses.photos.PhotoResponse;
import fcul.modc.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping
    public ResponseEntity<PhotoResponse> createPhoto(@ModelAttribute CreatePhotoRequest request) throws IOException {
        logger.info("=== CREATE PHOTO REQUEST RECEIVED ===");
        logger.info("Request parameters:");
        logger.info("  - albumId: {}", request.getAlbumId());
        logger.info("  - ownerId: {}", request.getOwnerId());
        logger.info("  - description: {}", request.getDescription());

        if (request.getFile() != null) {
            logger.info("  - file present: {}", request.getFile().getOriginalFilename());
            logger.info("  - file size: {} bytes", request.getFile().getSize());
            logger.info("  - file content type: {}", request.getFile().getContentType());
        } else {
            logger.error("  - file is NULL!");
            throw new IllegalArgumentException("File is required");
        }

        try {
            PhotoResponse photo = photoService.createPhoto(request);
            logger.info("Photo created successfully with ID: {}", photo.getId());
            logger.info("=== CREATE PHOTO REQUEST END (SUCCESS) ===");
            return ResponseEntity.status(201).body(photo);
        } catch (Exception e) {
            logger.error("Error creating photo: ", e);
            logger.error("=== CREATE PHOTO REQUEST END (ERROR) ===");
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotoResponse> updatePhoto(@PathVariable Long id, @RequestBody UpdatePhotoRequest request) {
        PhotoResponse updated = photoService.updatePhoto(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id, Authentication auth) {
        photoService.deletePhoto(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhoto(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(photoService.getPhotoById(id, auth.getName()));
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<byte[]> getPhotoData(@PathVariable Long id) {
        byte[] data = photoService.getPhotoData(id);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(data);
    }

    @GetMapping("/album/{albumId}")
    public ResponseEntity<List<PhotoResponse>> getPhotosByAlbum(@PathVariable Long albumId, Authentication auth) {
        return ResponseEntity.ok(photoService.getPhotosByAlbum(albumId, auth.getName()));
    }
}
