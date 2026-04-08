package fcul.modc.requests.photos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreatePhotoRequest {

    @NotNull
    private Long albumId;

    @Size(max = 500)
    private String description;

    @Size(max = 100)
    private String cameraModel;

    @Size(max = 50)
    private String location;

    private LocalDateTime takenTime;

    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCameraModel() { return cameraModel; }
    public void setCameraModel(String cameraModel) { this.cameraModel = cameraModel; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getTakenTime() { return takenTime; }
    public void setTakenTime(LocalDateTime takenTime) { this.takenTime = takenTime; }
}