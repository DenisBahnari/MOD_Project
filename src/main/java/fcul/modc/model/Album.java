package fcul.modc.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // --- Ownership ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // --- Photos in this album ---
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Photo> photos = new ArrayList<>();

    // --- Constructors ---
    protected Album() {}

    public Album(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    // --- Helper methods ---
    public void addPhoto(Photo photo) {
        photos.add(photo);
        photo.setAlbum(this);
    }

    public void removePhoto(Photo photo) {
        photos.remove(photo);
        photo.setAlbum(null);
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Photo> getPhotos() {
        return photos;
    }
}