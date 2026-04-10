package fcul.modc.exceptions.album;

public class AlbumAccessDeniedException extends RuntimeException {
    public AlbumAccessDeniedException(String message) {
        super(message);
    }
}