package fcul.modc.repository;

import fcul.modc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import fcul.modc.model.Album;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByOwner(User owner);
    List<Album> findBySharedUsersContaining(User user);
    List<Album> findByNameIgnoreCase(String name);
}