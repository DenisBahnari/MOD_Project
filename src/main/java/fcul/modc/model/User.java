package fcul.modc.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Album> albums = new ArrayList<>();

    // --- Constructors ---
    public User() {}

    public User(String username) {
        this.username = username;
    }

    // --- Helper methods ---
    public void addAlbum(Album album) {
        albums.add(album);
        album.setOwner(this);
    }

    public void removeAlbum(Album album) {
        albums.remove(album);
        album.setOwner(null);
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
