package fcul.modc.responses.photos;

import fcul.modc.model.Photo;
import fcul.modc.model.PhotoMetadata;

import java.time.LocalDateTime;

public class PhotoResponse {

    private Long id;
    private Long albumId;
    private String filename;
    private long sizeBytes;
    private String description;
    private String cameraModel;
    private String location;
    private LocalDateTime uploadTime;
    private LocalDateTime takenTime;

    public static PhotoResponse from(Photo photo) {
        PhotoResponse response = new PhotoResponse();
        response.id = photo.getId();
        response.albumId = photo.getAlbum().getId();

        PhotoMetadata meta = photo.getMetadata();
        if (meta != null) {
            response.filename = meta.getFilename();
            response.sizeBytes = meta.getSize();
            response.description = meta.getDescription();
            response.cameraModel = meta.getCameraModel();
            response.location = meta.getLocation();
            response.uploadTime = meta.getUploadTime();
            response.takenTime = meta.getTakenTime();
        }

        return response;
    }

    public Long getId() { return id; }
    public Long getAlbumId() { return albumId; }
    public String getFilename() { return filename; }
    public long getSizeBytes() { return sizeBytes; }
    public String getDescription() { return description; }
    public String getCameraModel() { return cameraModel; }
    public String getLocation() { return location; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public LocalDateTime getTakenTime() { return takenTime; }
}