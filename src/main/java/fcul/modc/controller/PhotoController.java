package fcul.modc.controller;

import fcul.modc.requests.photos.CreatePhotoRequest;
import fcul.modc.requests.photos.UpdatePhotoRequest;
import fcul.modc.responses.photos.PhotoResponse;
import fcul.modc.service.PhotoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping
    public ResponseEntity<PhotoResponse> createPhoto(@ModelAttribute CreatePhotoRequest request) throws IOException {
        PhotoResponse photo = photoService.createPhoto(request);
        return ResponseEntity.status(201).body(photo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotoResponse> updatePhoto(@PathVariable Long id, @RequestBody UpdatePhotoRequest request) {
        PhotoResponse updated = photoService.updatePhoto(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id, @RequestParam Long ownerId) {
        photoService.deletePhoto(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhoto(@PathVariable Long id, @RequestParam Long requesterId) {
        return ResponseEntity.ok(photoService.getPhotoById(id, requesterId));
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<byte[]> getPhotoData(@PathVariable Long id, @RequestParam Long requesterId) {
        byte[] data = photoService.getPhotoData(id, requesterId);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(data);
    }

    @GetMapping("/album/{albumId}")
    public ResponseEntity<List<PhotoResponse>> getPhotosByAlbum(@PathVariable Long albumId, @RequestParam Long requesterId) {
        return ResponseEntity.ok(photoService.getPhotosByAlbum(albumId, requesterId));
    }
}