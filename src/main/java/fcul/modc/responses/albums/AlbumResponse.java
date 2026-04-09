package fcul.modc.responses.albums;

import fcul.modc.model.Album;
import fcul.modc.responses.users.UserResponse;

public class AlbumResponse {

    private Long id;
    private String name;
    private String description;
    private UserResponse owner;

    public AlbumResponse() {}

    public AlbumResponse(Long id, String name, String description, UserResponse owner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    public static AlbumResponse from(Album album) {
        return new AlbumResponse(
                album.getId(),
                album.getName(),
                album.getDescription(),
                UserResponse.from(album.getOwner())
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserResponse getOwner() {
        return owner;
    }

    public void setOwner(UserResponse owner) {
        this.owner = owner;
    }
}