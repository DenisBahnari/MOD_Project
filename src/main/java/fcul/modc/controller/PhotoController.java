package fcul.modc.controller;

import fcul.modc.model.PhotoMetadata;
import fcul.modc.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/albums")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }


    @PostMapping("/{albumId}/photos")
    public ResponseEntity<String> uploadPhoto(@PathVariable Long albumId, @RequestParam("file") MultipartFile file) {
        try {
            PhotoMetadata saved = photoService.uploadPhoto(albumId, file);
            return ResponseEntity.ok("Uploaded: " + saved.getFilename());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

}