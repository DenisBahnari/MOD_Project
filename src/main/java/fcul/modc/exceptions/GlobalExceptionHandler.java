package fcul.modc.exceptions;

import fcul.modc.exceptions.album.AlbumAlreadyExistsException;
import fcul.modc.exceptions.album.AlbumNotFoundException;
import fcul.modc.exceptions.album.AlbumOwnerMismatchException;
import fcul.modc.exceptions.photo.PhotoNotFoundException;
import fcul.modc.exceptions.photo.PhotoOwnerMismatchException;
import fcul.modc.exceptions.user.UserAlreadyExistsException;
import fcul.modc.exceptions.user.UserNotFoundException;
import fcul.modc.responses.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----------------- Generic Exception -----------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage(), 500));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJpaNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), 404));
    }

    // ----------------- User Exceptions -----------------
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), 409));
    }

    // ----------------- Album Exceptions -----------------
    @ExceptionHandler(AlbumNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAlbumNotFound(AlbumNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(AlbumAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlbumAlreadyExists(AlbumAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(AlbumOwnerMismatchException.class)
    public ResponseEntity<ErrorResponse> handleAlbumOwnerMismatch(AlbumOwnerMismatchException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), 403));
    }

    // ----------------- Photo Exceptions -----------------
    @ExceptionHandler(PhotoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePhotoNotFound(PhotoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(PhotoOwnerMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePhotoOwnerMismatch(PhotoOwnerMismatchException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), 403));
    }
}