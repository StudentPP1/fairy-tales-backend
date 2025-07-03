package dev.project.bedtimestory.repository;

import dev.project.bedtimestory.dto.StoryDetailsDto;
import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Role;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataJpaTest // ! enable JPA-components in context (repositories, entityManager)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // ! don't connect in-memory db
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true"
})
class StoryRepositoryTest {
    private static final int MIGRATION_ADDED_STORY_COUNT = 4;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("username")
            .withPassword("password");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnMostLikedStoriesSortedByLikes() {
        storyRepository.save(new Story("T1", "D1", "img", "text", 10));
        storyRepository.save(new Story("T2", "D2", "img", "text", 20));

        Page<StoryDto> result = storyRepository.findMostLikedStories(PageRequest.of(0, 10));
        assertEquals(MIGRATION_ADDED_STORY_COUNT + 2, result.getTotalElements());
        assertEquals("T2", result.getContent().get(0).getTitle());
    }

    @Test
    void shouldReturnOnlyNotReadStories() {
        Story s1 = storyRepository.save(new Story("S1", "Desc", "img", "text", 0));
        storyRepository.save(new Story("S2", "Desc", "img", "text", 0));
        User user = userRepository.save(new User("User", "user@mail.com", null, "pass", Role.USER, false,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        user.getReadStories().add(s1);
        userRepository.save(user);

        Page<StoryDto> result = storyRepository.findNotReadStories(user.getId(), PageRequest.of(0, 10));
        assertEquals(MIGRATION_ADDED_STORY_COUNT + 1, result.getTotalElements());
        assertEquals("S2", result.getContent().get(MIGRATION_ADDED_STORY_COUNT).getTitle());
    }

    @Test
    void shouldReturnAllStoriesWithExpectedTitlesAndCount() {
        storyRepository.save(new Story("Story 1", "Desc 1", "img1", "text1", 5));
        storyRepository.save(new Story("Story 2", "Desc 2", "img2", "text2", 10));
        storyRepository.save(new Story("Story 3", "Desc 3", "img3", "text3", 15));

        Page<StoryDto> result = storyRepository.findStories(PageRequest.of(0, 10));

        assertEquals(MIGRATION_ADDED_STORY_COUNT + 3, result.getTotalElements());
        List<String> titles = result.getContent().stream().map(StoryDto::getTitle).toList();
        assertTrue(titles.containsAll(List.of("Story 1", "Story 2", "Story 3")));
    }

    @Test
    void shouldReturnStoryDetailsDtoWithCorrectTitleAndLikedAndReadFlags() {
        Story story = storyRepository.save(new Story("Story", "Desc", "img", "txt", 5));
        ArrayList<Story> stories = new ArrayList<>();
        stories.add(story);
        User user = userRepository.save(
                new User("User", "user@mail.com", null, "pass", Role.USER, false, new ArrayList<>(), stories, stories)
        );
        userRepository.save(user);

        Optional<StoryDetailsDto> result = storyRepository.getStoryDetailsDto(story.getId(), user.getId());
        assertTrue(result.isPresent());
        StoryDetailsDto dto = result.get();
        assertEquals("Story", dto.getTitle());
        assertTrue(dto.isLiked());
        assertTrue(dto.isRead());
    }
}