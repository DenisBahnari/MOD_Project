package fcul.modc.responses.albums;

import fcul.modc.model.Album;
import fcul.modc.responses.users.UserResponse;

import java.util.List;

public class AlbumResponse {

    private Long id;
    private String name;
    private String description;
    private UserResponse owner;
    private List<UserResponse> sharedUsers;

    public AlbumResponse() {}

    public AlbumResponse(Long id, String name, String description, UserResponse owner, List<UserResponse> sharedUsers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.sharedUsers = sharedUsers;
    }

    public static AlbumResponse from(Album album) {
        List<UserResponse> sharedUsers = album.getSharedUsers().stream()
                .map(UserResponse::from)
                .toList();
        return new AlbumResponse(
                album.getId(),
                album.getName(),
                album.getDescription(),
                UserResponse.from(album.getOwner()),
                sharedUsers
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

    public List<UserResponse> getSharedUsers() {
        return sharedUsers;
    }

    public void setSharedUsers(List<UserResponse> sharedUsers) {
        this.sharedUsers = sharedUsers;
    }
}