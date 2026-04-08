package fcul.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PhotoMetadata {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private long size; // bytes

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    @Column
    private LocalDateTime takenTime;

    @Column(length = 100)
    private String cameraModel;

    @Column(length = 50)
    private String location;

    @Column(length = 500)
    private String description;

    @ManyToOne
    private Album album;

    @OneToOne(mappedBy = "metadata", cascade = CascadeType.ALL)
    private Photo photo;


    public PhotoMetadata(Long id, String filename, long size, LocalDateTime uploadTime, LocalDateTime takenTime,
                         String cameraModel, String location, String description, Album album, Photo photo) {
        this.id = id;
        this.filename = filename;
        this.size = size;
        this.uploadTime = uploadTime;
        this.takenTime = takenTime;
        this.cameraModel = cameraModel;
        this.location = location;
        this.description = description;
        this.album = album;
        this.photo = photo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public LocalDateTime getTakenTime() {
        return takenTime;
    }

    public void setTakenTime(LocalDateTime takenTime) {
        this.takenTime = takenTime;
    }

    public String getCameraModel() {
        return cameraModel;
    }

    public void setCameraModel(String cameraModel) {
        this.cameraModel = cameraModel;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }
}
