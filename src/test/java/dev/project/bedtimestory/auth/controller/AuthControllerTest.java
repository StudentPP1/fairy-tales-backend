package dev.project.bedtimestory.auth.controller;

import dev.project.bedtimestory.config.PostgreSQLContainerInitializer;
import dev.project.bedtimestory.config.RedisContainerInitializer;
import dev.project.bedtimestory.request.UserLoginRequest;
import dev.project.bedtimestory.request.UserRegisterRequest;
import dev.project.bedtimestory.response.AuthenticationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class, RedisContainerInitializer.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterUser() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("user",
                "StrongPassword123!",
                "user@example.com");
        AuthenticationResponse response = new AuthenticationResponse("accessToken");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest(
                "user2",
                "StrongPassword123!",
                "user2@example.com"
        );

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(registerRequest)))
                .andExpect(status().isOk());

        UserLoginRequest loginRequest = new UserLoginRequest(
                "user2@example.com",
                "StrongPassword123!"
        );

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void shouldRefreshToken() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest(
                "user4",
                "StrongPassword123!",
                "user4@example.com"
        );

        var result = mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        var cookie = result.getResponse().getCookie("refreshToken");

        mockMvc.perform(post("/api/refresh-token")
                        .with(csrf())
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void shouldLogoutUser() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest(
                "user3",
                "StrongPassword123!",
                "user3@example.com"
        );

        var result = mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(registerRequest)))
                .andExpect(status().isOk()).andReturn();

        var cookie = result.getResponse().getCookie("refreshToken");

        mockMvc.perform(post("/api/logout").with(csrf()).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("logout successfully"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}