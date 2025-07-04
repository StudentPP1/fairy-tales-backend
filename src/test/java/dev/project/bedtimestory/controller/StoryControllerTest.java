package dev.project.bedtimestory.controller;

import dev.project.bedtimestory.config.PostgreSQLContainerInitializer;
import dev.project.bedtimestory.config.RedisContainerInitializer;
import dev.project.bedtimestory.dto.StoryDetailsDto;
import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Role;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.request.CreateStoryRequest;
import dev.project.bedtimestory.request.UpdateStoryRequest;
import dev.project.bedtimestory.response.PageWrapper;
import dev.project.bedtimestory.security.AppUserDetails;
import dev.project.bedtimestory.service.StoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class, RedisContainerInitializer.class})
class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoryService storyService;

    @Test
    void shouldReturnStoryDetailsDtoByStoryId() throws Exception {
        Long storyId = 1L;
        Long userId = 1L;

        StoryDetailsDto storyDetailsDto = new StoryDetailsDto(
                storyId, "Title", "Description",
                "imgUrl", "text", 10, true, false
        );

        User user = new User();
        user.setRole(Role.USER);
        user.setId(userId);
        user.setEmail("user@example.com");
        AppUserDetails appUserDetails = new AppUserDetails(user);

        when(storyService.getStoryDetailsById(storyId, userId)).thenReturn(storyDetailsDto);

        mockMvc.perform(get("/api/story").param("storyId", storyId.toString())
                        .with(user(appUserDetails))
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(storyId))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.description").value("Description"));

        verify(storyService).getStoryDetailsById(eq(storyId), eq(userId));
    }

    @Test
    void shouldReturnMostLikedStories() throws Exception {
        StoryDto dto = new StoryDto(1L, "Title", "Desc", "img", 5);
        PageWrapper<StoryDto> wrapper = PageWrapper.of(new PageImpl<>(List.of(dto)));

        when(storyService.getMostLikedStories(any(Pageable.class))).thenReturn(wrapper);

        mockMvc.perform(get("/api/story/topStories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Title"))
                .andExpect(jsonPath("$.content[0].description").value("Desc"))
                .andExpect(jsonPath("$.content[0].imgUrl").value("img"))
                .andExpect(jsonPath("$.content[0].likedCount").value(5));

        verify(storyService).getMostLikedStories(any(Pageable.class));
    }

    @Test
    void shouldReturnNotReadStories() throws Exception {
        StoryDto storyDto = new StoryDto(1L, "Title", "Description", "imgUrl", 5);
        PageWrapper<StoryDto> pageWrapper = PageWrapper.of(new PageImpl<>(List.of(storyDto)));

        Long userId = 1L;

        User user = new User();
        user.setRole(Role.USER);
        user.setId(userId);
        user.setEmail("user@example.com");
        AppUserDetails appUserDetails = new AppUserDetails(user);

        // ! getNotReadStories(any Pageable, only mockUserId) => return pageWrapper
        when(storyService.getNotReadStories(any(Pageable.class), eq(userId))).thenReturn(pageWrapper);

        mockMvc.perform(get("/api/story/notReadStories")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user(appUserDetails))
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Title"))
                .andExpect(jsonPath("$.content[0].description").value("Description"))
                .andExpect(jsonPath("$.content[0].imgUrl").value("imgUrl"))
                .andExpect(jsonPath("$.content[0].likedCount").value(5));

        verify(storyService).getNotReadStories(any(Pageable.class), eq(userId));
    }

    @Test
    void shouldReturnPagedStories() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        StoryDto dto = new StoryDto(1L, "title", "desc", "img", 5);
        Page<StoryDto> page = new PageImpl<>(List.of(dto));
        when(storyService.getStories(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/story/getStories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("title"));

        verify(storyService).getStories(any(Pageable.class));
    }

    @Test
    void shouldReturnSearchResults() throws Exception {
        String query = "test";
        Pageable pageable = PageRequest.of(0, 10);
        StoryDto dto = new StoryDto(1L, "title", "desc", "img", 5);
        PageWrapper<StoryDto> pageWrapper = PageWrapper.of(new PageImpl<>(List.of(dto)));
        when(storyService.searchStories(eq(query), any(Pageable.class))).thenReturn(pageWrapper);

        mockMvc.perform(get("/api/story/search")
                        .param("query", query)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("title"));

        verify(storyService).searchStories(eq(query), any(Pageable.class));
    }

    @Test
    void shouldReturnCreatedStory() throws Exception {
        StoryDto dto = new StoryDto(1L, "title", "desc", "img", 0);

        User user = new User();
        user.setRole(Role.ADMIN);
        user.setId(1L);
        user.setEmail("user@example.com");
        AppUserDetails appUserDetails = new AppUserDetails(user);

        when(storyService.createStory(any(CreateStoryRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/story/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                              "title": "title",
                              "description": "desc",
                              "imgUrl": "img",
                              "text": "text"
                            }
                            """)
                        .with(user(appUserDetails))
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.description").value("desc"));

        verify(storyService).createStory(any(CreateStoryRequest.class));
    }

    @Test
    @WithMockUser(username = "username", roles = {"ADMIN"})
    void shouldUpdateStory() throws Exception {
        UpdateStoryRequest request = new UpdateStoryRequest();
        request.setId(1L);
        request.setTitle("Updated Title");
        request.setDescription("Updated Desc");
        request.setImgUrl("img.png");
        request.setText("Updated Text");

        StoryDto responseDto = new StoryDto(1L, "Updated Title", "Updated Desc", "img.png", 0);
        when(storyService.updateStory(any(UpdateStoryRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/story/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": 1,
                              "title": "Updated Title",
                              "description": "Updated Desc",
                              "imgUrl": "img.png",
                              "text": "Updated Text"
                            }
                            """).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(storyService).updateStory(any(UpdateStoryRequest.class));
    }

    @Test
    void shouldDeleteStory() throws Exception {
        Long storyId = 1L;

        User user = new User();
        user.setRole(Role.ADMIN);
        user.setId(1L);
        user.setEmail("user@example.com");
        AppUserDetails appUserDetails = new AppUserDetails(user);

        doNothing().when(storyService).deleteStory(eq(storyId), anyLong());

        mockMvc.perform(delete("/api/story/delete")
                        .param("storyId", storyId.toString())
                        .with(user(appUserDetails))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("story deleted successfully"));

        verify(storyService).deleteStory(eq(storyId), anyLong());
    }
}