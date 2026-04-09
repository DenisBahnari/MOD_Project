package fcul.modc.responses.users;

import fcul.modc.model.User;

public class UserResponse {

    private Long id;
    private String username;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.id = user.getId();
        response.username = user.getUsername();
        return response;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}