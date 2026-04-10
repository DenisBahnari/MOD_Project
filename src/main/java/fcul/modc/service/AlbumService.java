package fcul.modc.service;

import fcul.modc.exceptions.album.AlbumAccessDeniedException;
import fcul.modc.exceptions.album.AlbumAlreadyExistsException;
import fcul.modc.exceptions.album.AlbumNotFoundException;
import fcul.modc.exceptions.album.AlbumOwnerMismatchException;
import fcul.modc.exceptions.album.AlbumUserAlreadySharedException;
import fcul.modc.exceptions.album.AlbumUserNotSharedException;
import fcul.modc.exceptions.user.UserNotFoundException;
import fcul.modc.model.Album;
import fcul.modc.model.User;
import fcul.modc.repository.AlbumRepository;
import fcul.modc.repository.UserRepository;
import fcul.modc.requests.albums.AddSharedUserRequest;
import fcul.modc.requests.albums.RemoveSharedUserRequest;
import fcul.modc.requests.albums.UpdateAlbumRequest;
import fcul.modc.responses.albums.AlbumResponse;
import fcul.modc.requests.albums.CreateAlbumRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AlbumService(AlbumRepository albumRepository, UserRepository userRepository) {
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
    }

    public AlbumResponse getAlbum(Long id, String username) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + id));
        validateAccess(album, resolveUser(username));
        return AlbumResponse.from(album);
    }

    public List<AlbumResponse> getAlbumsByOwner(String username) {
        User owner = resolveUser(username);
        return albumRepository.findByOwner(owner)
                .stream()
                .map(AlbumResponse::from)
                .toList();
    }

    public Album createAlbum(CreateAlbumRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getOwnerId()));

        boolean exists = albumRepository.findByOwner(owner).stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(request.getName()));
        if (exists) {
            throw new AlbumAlreadyExistsException(
                    "Album already exists with name: '" + request.getName() + "' for user id: " + owner.getId()
            );
        }

        Album album = new Album(request.getName(), owner);
        album.setDescription(request.getDescription());
        return albumRepository.save(album);
    }

    public Album updateAlbum(Long albumId, UpdateAlbumRequest request) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + albumId));

        if (!album.getOwner().getId().equals(request.getOwnerId())) {
            throw new AlbumOwnerMismatchException(
                    "User with id " + request.getOwnerId() + " is not the owner of album id " + albumId
            );
        }

        boolean exists = albumRepository.findByOwner(album.getOwner()).stream()
                .anyMatch(a -> !a.getId().equals(albumId) && a.getName().equalsIgnoreCase(request.getName()));
        if (exists) {
            throw new AlbumAlreadyExistsException(
                    "Another album already exists with name: '" + request.getName() + "' for user id: " + album.getOwner().getId()
            );
        }

        album.setName(request.getName());
        album.setDescription(request.getDescription());
        return albumRepository.save(album);
    }

    public void deleteAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + albumId));
        albumRepository.delete(album);
    }

    public AlbumResponse addSharedUser(Long albumId, AddSharedUserRequest request) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + albumId));

        if (!album.getOwner().getId().equals(request.getOwnerId())) {
            throw new AlbumOwnerMismatchException(
                    "User with id " + request.getOwnerId() + " is not the owner of album id " + albumId
            );
        }

        User target = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));

        boolean alreadyShared = album.getSharedUsers().stream()
                .anyMatch(u -> u.getId().equals(target.getId()));
        if (alreadyShared) {
            throw new AlbumUserAlreadySharedException(
                    "User with id " + target.getId() + " already has access to album id " + albumId
            );
        }

        album.addSharedUser(target);
        return AlbumResponse.from(albumRepository.save(album));
    }

    public AlbumResponse removeSharedUser(Long albumId, RemoveSharedUserRequest request) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + albumId));

        if (!album.getOwner().getId().equals(request.getOwnerId())) {
            throw new AlbumOwnerMismatchException(
                    "User with id " + request.getOwnerId() + " is not the owner of album id " + albumId
            );
        }

        User target = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));

        boolean isShared = album.getSharedUsers().stream()
                .anyMatch(u -> u.getId().equals(target.getId()));
        if (!isShared) {
            throw new AlbumUserNotSharedException(
                    "User with id " + target.getId() + " does not have access to album id " + albumId
            );
        }

        album.removeSharedUser(target);
        return AlbumResponse.from(albumRepository.save(album));
    }

    public List<AlbumResponse> getSharedAlbums(String username) {
        User user = resolveUser(username);
        return albumRepository.findBySharedUsersContaining(user)
                .stream()
                .map(AlbumResponse::from)
                .toList();
    }

    // name=' UNION SELECT id, id, username, username FROM app_user --
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchAlbumsByName(String name) {
        String sql = "SELECT * FROM album WHERE name = '" + name + "'";
        Query query = entityManager.createNativeQuery(sql); // no entity class mapping
        query.unwrap(org.hibernate.query.NativeQuery.class)
                .setResultTransformer(org.hibernate.transform.AliasToEntityMapResultTransformer.INSTANCE);
        return query.getResultList();
    }

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private void validateAccess(Album album, User requester) {
        boolean isOwner = album.getOwner().getId().equals(requester.getId());
        boolean isShared = album.getSharedUsers().stream()
                .anyMatch(u -> u.getId().equals(requester.getId()));
        if (!isOwner && !isShared) {
            throw new AlbumAccessDeniedException(
                    "User '" + requester.getUsername() + "' does not have access to album id " + album.getId()
            );
        }
    }
}
