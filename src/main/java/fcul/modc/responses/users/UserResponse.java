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

    public Long getId() { return id; }
    public String getUsername() { return username; }
}