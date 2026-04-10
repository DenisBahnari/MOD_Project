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

    @Column(length = 500)
    private String description;

    // --- Ownership ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // --- Photos in this album ---
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Photo> photos = new ArrayList<>();

    // --- Shared users ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "album_shared_users",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> sharedUsers = new ArrayList<>();

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

    public void addSharedUser(User user) {
        sharedUsers.add(user);
    }

    public void removeSharedUser(User user) {
        sharedUsers.remove(user);
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {return description; }

    public void setDescription(String description) {this.description = description; }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public List<User> getSharedUsers() {
        return sharedUsers;
    }
}