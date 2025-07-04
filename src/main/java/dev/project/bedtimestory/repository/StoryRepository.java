package dev.project.bedtimestory.repository;

import dev.project.bedtimestory.dto.StoryDetailsDto;
import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface StoryRepository extends JpaRepository<Story, Long>, SearchStoryRepository {
    @Query("""
    SELECT new dev.project.bedtimestory.dto.StoryDto(
        s.id, s.title, s.description, s.imgUrl, s.likedCount
    )
    FROM Story s
    ORDER BY s.likedCount DESC
    """)
    Page<StoryDto> findMostLikedStories(Pageable pageable);

    @Query("""
    SELECT new dev.project.bedtimestory.dto.StoryDto(
        s.id, s.title, s.description, s.imgUrl, s.likedCount
    )
    FROM Story s
    WHERE s.id NOT IN (
        SELECT rs.id FROM User u JOIN u.readStories rs WHERE u.id = :userId
    )
    """)
    Page<StoryDto> findNotReadStories(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT new dev.project.bedtimestory.dto.StoryDto(
        s.id, s.title, s.description, s.imgUrl, s.likedCount
    )
    FROM Story s
    """)
    Page<StoryDto> findStories(Pageable pageable);

    @Query("""
    SELECT new dev.project.bedtimestory.dto.StoryDetailsDto(
        s.id,
        s.title,
        s.description,
        s.imgUrl,
        s.text,
        s.likedCount,
        EXISTS (
            SELECT 1 FROM User u JOIN u.likedStories ls WHERE u.id = :userId AND ls.id = s.id
        ),
        EXISTS (
            SELECT 1 FROM User u JOIN u.readStories rs WHERE u.id = :userId AND rs.id = s.id
        )
    )
    FROM Story s WHERE s.id = :storyId
    """)
    Optional<StoryDetailsDto> getStoryDetailsDto(Long storyId, Long userId);
}