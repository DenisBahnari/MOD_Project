package fcul.modc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fcul.modc.model.PhotoMetadata;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoMetadataRepository extends JpaRepository<PhotoMetadata, Long> {

}