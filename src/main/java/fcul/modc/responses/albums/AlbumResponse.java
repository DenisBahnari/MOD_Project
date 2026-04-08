package fcul.modc.responses.albums;

import fcul.modc.model.Album;

public class AlbumResponse {

    private Long id;
    private String name;
    private String ownerUsername;

    public static AlbumResponse from(Album album) {
        AlbumResponse response = new AlbumResponse();
        response.id = album.getId();
        response.name = album.getName();
        response.ownerUsername = album.getOwner().getUsername();
        return response;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getOwnerUsername() { return ownerUsername; }
}
