package fcul.modc.controller;

import fcul.modc.responses.photos.PhotoResponse;
import fcul.modc.service.PhotoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhotoById(@PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoById(id));
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<byte[]> getPhotoData(@PathVariable Long id) {
        byte[] data = photoService.getPhotoData(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(data);
    }

    @GetMapping("/album/{albumId}")
    public ResponseEntity<List<PhotoResponse>> getPhotosByAlbum(@PathVariable Long albumId) {
        return ResponseEntity.ok(photoService.getPhotosByAlbum(albumId));
    }
}