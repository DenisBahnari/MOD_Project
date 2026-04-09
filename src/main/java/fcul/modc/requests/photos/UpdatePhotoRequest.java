package fcul.modc.requests.photos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdatePhotoRequest {

    @NotNull
    private Long ownerId;

    @Size(max = 500)
    private String description;

    // Getters & Setters
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}