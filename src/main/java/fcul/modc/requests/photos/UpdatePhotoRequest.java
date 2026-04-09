package fcul.modc.requests.photos;

import jakarta.validation.constraints.NotNull;

public class UpdatePhotoRequest {

    @NotNull
    private Long ownerId;

    private String description;

    // Getters e Setters
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}