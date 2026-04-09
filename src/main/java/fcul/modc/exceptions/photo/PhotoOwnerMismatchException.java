package fcul.modc.exceptions.photo;

public class PhotoOwnerMismatchException extends RuntimeException {
    public PhotoOwnerMismatchException(String message) {
        super(message);
    }
}
