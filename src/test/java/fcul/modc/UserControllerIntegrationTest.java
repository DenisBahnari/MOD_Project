package fcul.modc;

import fcul.modc.model.User;
import fcul.modc.repository.UserRepository;
import fcul.modc.requests.users.CreateUserRequest;
import fcul.modc.requests.users.UpdateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser() throws Exception {
        String json = """
        {
            "username": "denis"
        }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("denis"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCreateDuplicateUser() throws Exception {
        userRepository.save(new User("denis"));

        String json = """
        {
            "username": "denis"
        }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Username already taken")));
    }

    @Test
    void testUpdateUser() throws Exception {
        User user = userRepository.save(new User("denis"));

        String json = """
        {
            "username": "alex"
        }
        """;

        mockMvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alex"));
    }

    @Test
    void testDeleteUser() throws Exception {
        User user = userRepository.save(new User("denis"));

        mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isNotFound());
    }
}