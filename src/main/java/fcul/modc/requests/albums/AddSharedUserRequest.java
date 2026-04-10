package fcul.modc.requests.albums;

import jakarta.validation.constraints.NotNull;

public class AddSharedUserRequest {

    @NotNull
    private Long ownerId;

    @NotNull
    private Long userId;

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
