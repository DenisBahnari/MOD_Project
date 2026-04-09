package fcul.modc.exceptions.photo;

public class PhotoNotFoundException extends RuntimeException {
    public PhotoNotFoundException(String message) {
        super(message);
    }
}
