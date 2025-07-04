package dev.project.bedtimestory.repository;

import dev.project.bedtimestory.config.PostgreSQLContainerInitializer;
import dev.project.bedtimestory.dto.StoryDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
// @Import(SearchStoryRepositoryImpl.class) -> if we load just search repo without @Repository annotation
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {PostgreSQLContainerInitializer.class})
class SearchStoryRepositoryTest {

    @Autowired
    private StoryRepository storyRepository;

    @Test
    void shouldFindStoryByTitleFromMigrationData() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        Page<StoryDto> result = storyRepository.searchStories("Ivasyk", pageable);
        log.info("find content: '{}'", result.getContent());
        assertEquals(1, result.getTotalElements());
    }
}