package fcul.modc.requests.users;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRequest {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}