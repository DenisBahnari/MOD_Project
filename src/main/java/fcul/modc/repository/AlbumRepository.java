package fcul.modc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fcul.modc.model.Album;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
}