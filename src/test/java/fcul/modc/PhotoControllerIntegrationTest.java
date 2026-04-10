package fcul.modc;

import com.jayway.jsonpath.JsonPath;
import fcul.modc.model.Album;
import fcul.modc.model.User;
import fcul.modc.repository.AlbumRepository;
import fcul.modc.repository.PhotoRepository;
import fcul.modc.repository.UserRepository;
import fcul.modc.requests.photos.UpdatePhotoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PhotoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PhotoRepository photoRepository;

    private User user;
    private Album album;

    @BeforeEach
    void setup() {
        photoRepository.deleteAll();
        albumRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setUsername("denis");
        user.setPassword(passwordEncoder.encode("1234"));
        user = userRepository.save(user);

        album = albumRepository.save(new Album("My Album", user));
    }

    @Test
    void testUploadPhoto() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-image.png");
        InputStream is = resource.getInputStream();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                is
        );

        mockMvc.perform(multipart("/photos")
                        .file(file)
                        .param("ownerId", user.getId().toString())
                        .param("albumId", album.getId().toString())
                        .param("description", "Test photo")
                        .with(httpBasic("denis", "1234")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.filename").value("test-image.png"))
                .andExpect(jsonPath("$.description").value("Test photo"));
    }

    @Test
    void testGetPhotoById() throws Exception {
        // Primeiro criar a foto
        ClassPathResource resource = new ClassPathResource("test-image.png");
        InputStream is = resource.getInputStream();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                is
        );

        String response = mockMvc.perform(multipart("/photos")
                        .file(file)
                        .param("ownerId", user.getId().toString())
                        .param("albumId", album.getId().toString())
                        .param("description", "Test photo")
                        .with(httpBasic("denis", "1234")))
                .andReturn().getResponse().getContentAsString();

        Number photoIdNumber = JsonPath.read(response, "$.id");
        Long photoId = photoIdNumber.longValue();

        mockMvc.perform(get("/photos/" + photoId).with(httpBasic("denis", "1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(photoId))
                .andExpect(jsonPath("$.filename").value("test-image.png"));
    }

    @Test
    void testUpdatePhoto() throws Exception {
        ClassPathResource resource = new ClassPathResource("test-image.png");
        InputStream is = resource.getInputStream();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                is
        );

        String response = mockMvc.perform(multipart("/photos")
                        .file(file)
                        .param("ownerId", user.getId().toString())
                        .param("albumId", album.getId().toString())
                        .param("description", "Test photo")
                        .with(httpBasic("denis", "1234")))
                .andReturn().getResponse().getContentAsString();

        Number photoIdNumber = JsonPath.read(response, "$.id");
        long photoId = photoIdNumber.longValue();

        String updateJson = """
        {
            "description": "Updated description",
            "ownerId": %d
        }
        """.formatted(user.getId());

        mockMvc.perform(put("/photos/" + photoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(httpBasic("denis", "1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void testDeletePhoto() throws Exception {
        // --- Criar foto ---
        ClassPathResource resource = new ClassPathResource("test-image.png");
        InputStream is = resource.getInputStream();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                is
        );

        String response = mockMvc.perform(multipart("/photos")
                        .file(file)
                        .param("ownerId", user.getId().toString())
                        .param("albumId", album.getId().toString())
                        .param("description", "Test photo")
                        .with(httpBasic("denis", "1234")))
                .andReturn().getResponse().getContentAsString();

        Number photoIdNumber = JsonPath.read(response, "$.id");
        long photoId = photoIdNumber.longValue();

        // --- Cenário 1: dono apaga a foto com sucesso ---
        mockMvc.perform(delete("/photos/" + photoId)
                        .param("ownerId", user.getId().toString())
                        .with(httpBasic("denis", "1234")))
                .andExpect(status().isNoContent());

        // Verificar que a foto não existe mais
        mockMvc.perform(get("/photos/" + photoId).with(httpBasic("denis", "1234")))
                .andExpect(status().isNotFound());

        // --- Cenário 2: outro usuário tenta apagar a foto ---
        User otherUser = new User();
        otherUser.setUsername("otherUser");
        otherUser.setPassword(passwordEncoder.encode("1234"));
        otherUser = userRepository.save(otherUser);

        // Recriar a foto para este teste
        InputStream is2 = new ClassPathResource("test-image.png").getInputStream();
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                is2
        );

        String response2 = mockMvc.perform(multipart("/photos")
                        .file(file2)
                        .param("ownerId", user.getId().toString())
                        .param("albumId", album.getId().toString())
                        .param("description", "Test photo 2")
                        .with(httpBasic("denis", "1234")))
                .andReturn().getResponse().getContentAsString();

        long photoId2 = ((Number) JsonPath.read(response2, "$.id")).longValue();

        // Tentar deletar com outro usuário
        mockMvc.perform(delete("/photos/" + photoId2)
                        .param("ownerId", otherUser.getId().toString())
                        .with(httpBasic("otherUser", "1234")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("not the owner of album")));
    }
}