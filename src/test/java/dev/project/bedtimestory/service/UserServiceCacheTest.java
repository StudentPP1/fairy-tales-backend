package dev.project.bedtimestory.service;

import dev.project.bedtimestory.config.PostgreSQLContainerInitializer;
import dev.project.bedtimestory.config.RedisContainerInitializer;
import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.repository.StoryRepository;
import dev.project.bedtimestory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class, RedisContainerInitializer.class})
class UserServiceCacheTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private StoryRepository storyRepository;

    @MockitoBean
    private HelperService helperService;

    private Cache mostLikedCache;
    private Cache notReadStoriesCache;
    private Cache searchStoriesCache;

    private final String cacheKey = "key";
    private final Long userId = 1L;
    private final Long storyId = 10L;
    private final List<StoryDto> stories = List.of(new StoryDto(storyId, "Title", "Desc", "img", 5));

    @BeforeEach
    void setupCaches() {
        mostLikedCache = cacheManager.getCache("mostLikedStories");
        notReadStoriesCache = cacheManager.getCache("notReadStories");
        searchStoriesCache = cacheManager.getCache("searchStories");

        mostLikedCache.clear();
        notReadStoriesCache.clear();
        searchStoriesCache.clear();

        mostLikedCache.put(cacheKey, stories);
        notReadStoriesCache.put(userId, stories);
        searchStoriesCache.put(cacheKey, stories);
    }

    @Test
    void shouldEvictCacheAfterAddLikedStory() {
        User user = new User();
        user.setId(userId);
        Story story = new Story();
        story.setId(storyId);
        story.setLikedCount(0);

        when(helperService.getUserById(userId)).thenReturn(user);
        when(helperService.getStoryById(storyId)).thenReturn(story);

        userService.addLikedStory(userId, storyId);

        assertThat(mostLikedCache.get(cacheKey)).isNull();
        assertThat(notReadStoriesCache.get(userId)).isNull();
        assertThat(searchStoriesCache.get(cacheKey)).isNull();
    }

    @Test
    void shouldEvictCacheAfterRemoveLikedStory() {
        User user = new User();
        user.setId(userId);
        Story story = new Story();
        story.setId(storyId);
        story.setLikedCount(1);

        when(helperService.getUserById(userId)).thenReturn(user);
        when(helperService.getStoryById(storyId)).thenReturn(story);

        userService.removeLikedStory(userId, storyId);

        assertThat(mostLikedCache.get(cacheKey)).isNull();
        assertThat(notReadStoriesCache.get(userId)).isNull();
        assertThat(searchStoriesCache.get(cacheKey)).isNull();
    }

    @Test
    void shouldEvictNotReadStoriesCacheAfterRemoveReadStory() {
        Story story = new Story();
        story.setId(storyId);
        User user = new User();
        user.setId(userId);
        user.setReadStories(new ArrayList<>(List.of(story)));

        when(helperService.getUserById(userId)).thenReturn(user);
        when(helperService.getStoryById(storyId)).thenReturn(story);

        userService.removeReadStory(userId, storyId);

        assertThat(mostLikedCache.get(cacheKey)).isNotNull();
        assertThat(notReadStoriesCache.get(userId)).isNull();
        assertThat(searchStoriesCache.get(cacheKey)).isNotNull();
    }

    @Test
    void shouldEvictNotReadStoriesCacheAfterAddReadStory() {
        Story story = new Story();
        story.setId(storyId);
        User user = new User();
        user.setId(userId);

        when(helperService.getUserById(userId)).thenReturn(user);
        when(helperService.getStoryById(storyId)).thenReturn(story);

        userService.addReadStory(userId, storyId);

        assertThat(mostLikedCache.get(cacheKey)).isNotNull();
        assertThat(notReadStoriesCache.get(userId)).isNull();
        assertThat(searchStoriesCache.get(cacheKey)).isNotNull();
    }
}