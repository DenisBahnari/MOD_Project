package fcul.modc.requests.photos;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class CreatePhotoRequest {

    @NotNull
    private Long ownerId;

    @NotNull
    private Long albumId;

    @NotNull
    private MultipartFile file;

    private String description;

    // Getters e Setters
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}