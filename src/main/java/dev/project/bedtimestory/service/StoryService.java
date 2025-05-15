package dev.project.bedtimestory.service;

import dev.project.bedtimestory.dto.StoryDetailsDto;
import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.exception.ApiException;
import dev.project.bedtimestory.repository.SearchStoryRepository;
import dev.project.bedtimestory.repository.StoryRepository;
import dev.project.bedtimestory.repository.UserRepository;
import dev.project.bedtimestory.request.CreateStoryRequest;
import dev.project.bedtimestory.request.UpdateStoryRequest;
import dev.project.bedtimestory.service.notification.NotificationService;
import dev.project.bedtimestory.utils.ApplicationProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoryService {
    private final StoryRepository storyRepository;
    private final SearchStoryRepository searchStoryRepository;
    private final UserRepository userRepository;
    private final HelperService helperService;
    private final NotificationService notificationService;
    private final ApplicationProperties applicationProperties;

    public Story getStoryById(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new ApiException("Story not found", HttpStatus.NOT_FOUND.value()));
    }
    public StoryDetailsDto getStoryDetailsById(Long storyId, Long userId) {
        return storyRepository.getStoryDetailsDto(storyId, userId)
                .orElseThrow(() -> new ApiException("Story not found", HttpStatus.NOT_FOUND.value()));
    }
    @Cacheable(value = "mostLikedStories")
    public Page<StoryDto> getMostLikedStories(Pageable pageable) {
        Page<StoryDto> stories = storyRepository.findMostLikedStories(pageable);
        log.info("mostLikedStories: {}", Arrays.toString(stories.getContent().toArray()));
        return stories;
    }
    @Cacheable(value = "notReadStories", key = "#userId")
    public Page<StoryDto> getNotReadStories(Pageable pageable, Long userId) {
        Page<StoryDto> stories = storyRepository.findNotReadStories(userId, pageable);
        log.info("notReadStories: {}", Arrays.toString(stories.getContent().toArray()));
        return stories;
    }
    public Page<StoryDto> getStories(Pageable pageable) {
        Page<StoryDto> stories = storyRepository.findStories(pageable);
        log.info("getStories: {}", Arrays.toString(stories.getContent().toArray()));
        return stories;
    }
    @Cacheable(value = "searchStories", key = "#query.toLowerCase()")
    public Page<StoryDto> searchStories(String query, Pageable pageable) {
        return searchStoryRepository.searchStories(query.toLowerCase(), pageable);
    }

    @CacheEvict(value = {"mostLikedStories", "notReadStories", "searchStories"}, allEntries = true)
    @Transactional
    public StoryDto updateStory(UpdateStoryRequest request) {
        log.info("StoryService: updateStory -> {}", request);
        Story story = getStoryById(request.getId());
        story.setTitle(request.getTitle());
        story.setDescription(request.getDescription());
        story.setImgUrl(request.getImgUrl());
        story.setText(request.getText());
        story = storyRepository.save(story);
        return new StoryDto(
                story.getId(),
                story.getTitle(),
                story.getDescription(),
                story.getImgUrl(),
                story.getLikedCount()
        );
    }
    @CacheEvict(value = {"mostLikedStories", "notReadStories", "searchStories"}, allEntries = true)
    public StoryDto createStory(CreateStoryRequest request) {
        log.info("StoryService: createStory -> {}", request);
        Story story = new Story();
        story.setTitle(request.getTitle());
        story.setDescription(request.getDescription());
        story.setImgUrl(request.getImgUrl());
        story.setText(request.getText());
        story = storyRepository.save(story);
        notificationService.sendNotification(story.getTitle(), story.getDescription());
        return new StoryDto(
                story.getId(),
                story.getTitle(),
                story.getDescription(),
                story.getImgUrl(),
                story.getLikedCount()
        );
    }
    @CacheEvict(value = {"mostLikedStories", "notReadStories", "searchStories"}, allEntries = true)
    @Transactional
    public void deleteStory(Long storyId, Long userId, HttpServletResponse response) throws IOException {
        log.info("StoryService: deleteStory -> {}", storyId);
        Story story = getStoryById(storyId);
        User user = helperService.getUserById(userId);
        user.setReadStories(user.getReadStories().stream().filter(s -> !Objects.equals(s.getId(), storyId)).collect(Collectors.toList()));
        user.setLikedStories(user.getLikedStories().stream().filter(s -> !Objects.equals(s.getId(), storyId)).collect(Collectors.toList()));
        storyRepository.delete(story);
        userRepository.save(user);
        response.sendRedirect(applicationProperties.getFrontUrl());
    }
}