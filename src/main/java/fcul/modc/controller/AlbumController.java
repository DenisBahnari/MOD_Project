package fcul.modc.controller;

import fcul.modc.model.Album;
import fcul.modc.requests.albums.UpdateAlbumRequest;
import fcul.modc.responses.albums.AlbumResponse;
import fcul.modc.requests.albums.CreateAlbumRequest;
import fcul.modc.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbum(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.getAlbum(id));
    }

    @GetMapping
    public ResponseEntity<List<AlbumResponse>> getAlbumsByOwner(@RequestParam Long ownerId) {
        return ResponseEntity.ok(albumService.getAlbumsByOwner(ownerId));
    }

    @PostMapping
    public ResponseEntity<Album> createAlbum(@RequestBody CreateAlbumRequest request) {
        Album album = albumService.createAlbum(request);
        return ResponseEntity.created(URI.create("/albums/" + album.getId())).body(album);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlbumResponse> updateAlbum(@PathVariable Long id, @RequestBody UpdateAlbumRequest request) {
        Album updated = albumService.updateAlbum(id, request);
        return ResponseEntity.ok(AlbumResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }
}