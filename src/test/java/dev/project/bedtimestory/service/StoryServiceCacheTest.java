package dev.project.bedtimestory.service;

import dev.project.bedtimestory.config.PostgreSQLContainerInitializer;
import dev.project.bedtimestory.config.RedisContainerInitializer;
import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.repository.StoryRepository;
import dev.project.bedtimestory.repository.UserRepository;
import dev.project.bedtimestory.request.CreateStoryRequest;
import dev.project.bedtimestory.request.UpdateStoryRequest;
import dev.project.bedtimestory.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class, RedisContainerInitializer.class})
@TestPropertySource(properties = {"spring.data.web.pageable.serialization-mode=via_dto"})
class StoryServiceCacheTest {

    @Autowired
    private StoryService storyService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private StoryRepository storyRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private HelperService helperService;

    @MockitoBean
    private NotificationService notificationService;

    private Long userId = 1L;
    private Pageable pageable;
    private StoryDto dto;
    private Page<StoryDto> page;

    @BeforeEach
    void initData() {
        pageable = PageRequest.of(0, 10, Sort.by("title"));
        dto = new StoryDto(1L, "title", "desc", "img", 5);
        page = new PageImpl<>(List.of(dto));
    }

    @Test
    void shouldCacheMostLikedStoriesAndEvictOnUpdate() {
        when(storyRepository.findMostLikedStories(pageable)).thenReturn(page);

        storyService.getMostLikedStories(pageable); // ! first call repo
        verify(storyRepository, times(1)).findMostLikedStories(pageable);

        storyService.getMostLikedStories(pageable); // ! service will take data from cache -> won't call repo
        verify(storyRepository, times(1)).findMostLikedStories(pageable);

        UpdateStoryRequest request = new UpdateStoryRequest();
        request.setId(1L);
        request.setTitle("New Title");
        request.setDescription("New Desc");
        request.setImgUrl("img");
        request.setText("text");
        Story mockStory = new Story();
        mockStory.setId(1L);

        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));
        when(storyRepository.save(any(Story.class))).thenReturn(mockStory);

        storyService.updateStory(request);
        storyService.getMostLikedStories(pageable);
        verify(storyRepository, times(2)).findMostLikedStories(pageable);
    }

    @Test
    void shouldCacheNotReadStoriesAndEvictOnCreate() {
        when(storyRepository.findNotReadStories(userId, pageable)).thenReturn(new PageImpl<>(List.of(dto)));

        storyService.getNotReadStories(pageable, userId);
        verify(storyRepository, times(1)).findNotReadStories(userId, pageable);

        storyService.getNotReadStories(pageable, userId);
        verify(storyRepository, times(1)).findNotReadStories(userId, pageable);

        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("Title");
        request.setDescription("Desc");
        request.setImgUrl("img");
        request.setText("Text");

        Story story = new Story();
        story.setId(1L);
        when(storyRepository.save(any(Story.class))).thenReturn(story);

        storyService.createStory(request);

        storyService.getNotReadStories(pageable, userId);
        verify(storyRepository, times(2)).findNotReadStories(userId, pageable);
    }

    @Test
    void shouldCacheSearchStoriesAndEvictOnDelete() {
        String query = "story";

        when(storyRepository.searchStories(query, pageable)).thenReturn(new PageImpl<>(List.of(
                new StoryDto(1L, "title", "desc", "img", 5)
        )));

        storyService.searchStories(query, pageable);
        verify(storyRepository, times(1)).searchStories(query, pageable);

        storyService.searchStories(query, pageable);
        verify(storyRepository, times(1)).searchStories(query, pageable);

        Story story = new Story();
        story.setId(1L);
        User user = new User();
        user.setId(1L);
        user.setReadStories(new ArrayList<>(List.of(story)));
        user.setLikedStories(new ArrayList<>(List.of(story)));

        when(storyRepository.findById(1L)).thenReturn(Optional.of(story));
        when(helperService.getUserById(1L)).thenReturn(user);

        storyService.deleteStory(1L, 1L);

        storyService.searchStories(query, pageable);
        verify(storyRepository, times(2)).searchStories(query, pageable);
    }
}