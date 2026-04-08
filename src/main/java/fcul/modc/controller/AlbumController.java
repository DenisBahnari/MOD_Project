package fcul.modc.controller;

import fcul.modc.model.Album;
import fcul.modc.model.PhotoMetadata;
import fcul.modc.model.User;
import fcul.modc.responses.albums.AlbumResponse;
import fcul.modc.requests.albums.CreateAlbumRequest;
import fcul.modc.service.AlbumService;
import fcul.modc.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/albums")
public class AlbumController {

    private final AlbumService albumService;
    private final PhotoService photoService;

    public AlbumController(AlbumService albumService, PhotoService photoService) {
        this.albumService = albumService;
        this.photoService = photoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbum(@PathVariable Long id) {
        AlbumResponse album = albumService.getAlbum(id);
        return ResponseEntity.ok(album);
    }

    @GetMapping
    public ResponseEntity<List<AlbumResponse>> getAlbumsByOwner(User currentUser) {
        List<AlbumResponse> albums = albumService.getAlbumsByOwner(currentUser);
        return ResponseEntity.ok(albums);
    }

    @PostMapping
    public ResponseEntity<Album> createAlbum(@RequestBody CreateAlbumRequest request) {
        var testUser = new User();

        Album album = albumService.createAlbum(request, testUser);

        return ResponseEntity
                .created(URI.create("/albums/" + album.getId()))
                .body(album);
    }

    @PostMapping("/{albumId}/photos")
    public ResponseEntity<String> uploadPhoto(@PathVariable Long albumId, @RequestParam("file") MultipartFile file) {
        try {
            PhotoMetadata saved = photoService.uploadPhoto(albumId, file);
            return ResponseEntity.ok("Uploaded: " + saved.getFilename());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}