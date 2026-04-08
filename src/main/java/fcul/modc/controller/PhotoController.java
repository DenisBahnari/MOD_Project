package fcul.modc.controller;

import fcul.modc.requests.photos.CreatePhotoRequest;
import fcul.modc.responses.photos.PhotoResponse;
import fcul.modc.service.PhotoService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponse> uploadPhoto(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") @Valid CreatePhotoRequest request
    ) throws IOException {
        PhotoResponse photo = photoService.uploadPhoto(file, request);
        return ResponseEntity
                .created(URI.create("/photos/" + photo.getId()))
                .body(photo);
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