package fcul.modc.controller;

import fcul.modc.model.Album;
import fcul.modc.requests.albums.AddSharedUserRequest;
import fcul.modc.requests.albums.RemoveSharedUserRequest;
import fcul.modc.requests.albums.UpdateAlbumRequest;
import fcul.modc.responses.albums.AlbumResponse;
import fcul.modc.requests.albums.CreateAlbumRequest;
import fcul.modc.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbum(@PathVariable Long id, @RequestParam Long requesterId) {
        return ResponseEntity.ok(albumService.getAlbum(id, requesterId));
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

    @PostMapping("/{albumId}/users")
    public ResponseEntity<AlbumResponse> addSharedUser(
            @PathVariable Long albumId,
            @RequestBody AddSharedUserRequest request) {
        return ResponseEntity.ok(albumService.addSharedUser(albumId, request));
    }

    @DeleteMapping("/{albumId}/users")
    public ResponseEntity<AlbumResponse> removeSharedUser(
            @PathVariable Long albumId,
            @RequestBody RemoveSharedUserRequest request) {
        return ResponseEntity.ok(albumService.removeSharedUser(albumId, request));
    }

    @GetMapping("/shared")
    public ResponseEntity<List<AlbumResponse>> getSharedAlbums(@RequestParam Long userId) {
        return ResponseEntity.ok(albumService.getSharedAlbums(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchAlbums(@RequestParam String name) {
        return ResponseEntity.ok(albumService.searchAlbumsByName(name));
    }
}