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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser() throws Exception {
        String json = """
        {
            "username": "denis",
            "password": "1234"
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
        User user = new User();
        user.setUsername("denis");
        user.setPassword(passwordEncoder.encode("1234"));
        userRepository.save(user);

        String json = """
        {
            "username": "denis",
            "password": "1234"
        }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError()); // ⚠️ explicação abaixo
    }

    @Test
    void testUpdateUser() throws Exception {
        User user = new User();
        user.setUsername("denis");
        user.setPassword(passwordEncoder.encode("1234"));
        user = userRepository.save(user);

        String json = """
        {
            "username": "alex",
            "password": "5678"  
        }
        """;

        mockMvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(httpBasic("denis", "1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alex"));
    }

    @Test
    void testDeleteUser() throws Exception {
        User user = new User();
        user.setUsername("denis");
        user.setPassword(passwordEncoder.encode("1234"));
        user = userRepository.save(user);

        mockMvc.perform(delete("/users/" + user.getId()).with(httpBasic("denis", "1234")))
                .andExpect(status().isNoContent());

    }
}