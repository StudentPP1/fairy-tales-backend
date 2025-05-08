package dev.project.bedtimestory.repository;

import dev.project.bedtimestory.dto.StoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface SearchStoryRepository {
    Page<StoryDto> searchStories(String query, Pageable pageable);
}
