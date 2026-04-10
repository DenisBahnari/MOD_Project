package fcul.modc;

import fcul.modc.model.User;
import fcul.modc.repository.AlbumRepository;
import fcul.modc.repository.UserRepository;
import fcul.modc.requests.albums.CreateAlbumRequest;
import fcul.modc.requests.albums.UpdateAlbumRequest;
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
class AlbumControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setup() {
        albumRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setUsername("denis");
        owner.setPassword(passwordEncoder.encode("1234"));
        owner = userRepository.save(owner);
    }

    @Test
    void testCreateAlbum() throws Exception {
        String json = """
        {
            "name": "Holiday",
            "description": "Summer vacation",
            "ownerId": %d
        }
        """.formatted(owner.getId());

        mockMvc.perform(post("/albums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(httpBasic("denis", "1234")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Holiday"))
                .andExpect(jsonPath("$.owner.id").value(owner.getId()));
    }

    @Test
    void testUpdateAlbum() throws Exception {
        var album = albumRepository.save(new fcul.modc.model.Album("Holiday", owner));

        String json = """
        {
            "name": "Holiday 2026",
            "description": "Updated desc",
            "ownerId": %d
        }
        """.formatted(owner.getId());

        mockMvc.perform(put("/albums/" + album.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(httpBasic("denis", "1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Holiday 2026"))
                .andExpect(jsonPath("$.description").value("Updated desc"));
    }

    @Test
    void testDeleteAlbum() throws Exception {
        var album = albumRepository.save(new fcul.modc.model.Album("Holiday", owner));

        mockMvc.perform(delete("/albums/" + album.getId()).with(httpBasic("denis", "1234")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/albums/" + album.getId()).with(httpBasic("denis", "1234")))
                .andExpect(status().isNotFound());
    }
}