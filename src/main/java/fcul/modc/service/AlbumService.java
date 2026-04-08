package fcul.modc.service;

import fcul.modc.model.Album;
import fcul.modc.model.User;
import fcul.modc.repository.AlbumRepository;
import fcul.modc.responses.albums.AlbumResponse;
import fcul.modc.requests.albums.CreateAlbumRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;

    public AlbumService(AlbumRepository albumRepository){
        this.albumRepository = albumRepository;
    }

    public AlbumResponse getAlbum(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + id));
        return AlbumResponse.from(album);
    }

    public List<AlbumResponse> getAlbumsByOwner(User owner) {
        return albumRepository.findByOwner(owner)
                .stream()
                .map(AlbumResponse::from)
                .toList();
    }

    public Album createAlbum(CreateAlbumRequest request, User owner) {
        Album album = new Album(request.getName(), owner);
        return albumRepository.save(album);
    }
}
