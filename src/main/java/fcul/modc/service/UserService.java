package fcul.modc.service;

import fcul.modc.exceptions.user.UserAlreadyExistsException;
import fcul.modc.exceptions.user.UserNotFoundException;
import fcul.modc.model.User;
import fcul.modc.repository.UserRepository;
import fcul.modc.requests.users.CreateUserRequest;
import fcul.modc.responses.users.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
        }

        User user = new User(request.getUsername());
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return UserResponse.from(user);
    }

    public UserResponse updateUserName(Long id, String newName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setUsername(newName);
        return UserResponse.from(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
}