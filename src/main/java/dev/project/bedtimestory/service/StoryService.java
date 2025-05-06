package dev.project.bedtimestory.service;

import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.exception.ApiException;
import dev.project.bedtimestory.repository.StoryRepository;
import dev.project.bedtimestory.repository.UserRepository;
import dev.project.bedtimestory.request.CreateStoryRequest;
import dev.project.bedtimestory.request.UpdateStoryRequest;
import dev.project.bedtimestory.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final HelperService helperService;
    private final NotificationService notificationService;
    public Story getStoryById(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new ApiException("Story not found", HttpStatus.NOT_FOUND.value()));
    }
    @Cacheable(value = "mostLikedStories")
    public Page<StoryDto> getMostLikedStories(Pageable pageable) {
        return storyRepository.findMostLikedStories(pageable);
    }
    @Cacheable(value = "notReadStories", key = "#userId")
    public Page<StoryDto> getNotReadStories(Pageable pageable, Long userId) {
        return storyRepository.findNotReadStories(userId, pageable);
    }
    public Page<StoryDto> getStories(Pageable pageable) {
        return storyRepository.findStories(pageable);
    }
    @Cacheable(value = "searchStories", key = "#query")
    public Page<StoryDto> searchStories(String query, Pageable pageable) {
        return storyRepository.searchStories(query, pageable);
    }

    @Transactional
    public void updateStory(UpdateStoryRequest request) {
        Story story = getStoryById(request.getId());
        story.setTitle(request.getTitle());
        story.setDescription(request.getDescription());
        story.setImgUrl(request.getImgUrl());
        story.setText(request.getText());
    }
    public void createStory(CreateStoryRequest request) {
        Story story = new Story();
        story.setTitle(request.getTitle());
        story.setDescription(request.getDescription());
        story.setImgUrl(request.getImgUrl());
        story.setText(request.getText());
        story = storyRepository.save(story);
        notificationService.sendNotification(story.getTitle(), story.getDescription());
    }
    @CacheEvict(value = {"mostLikedStories", "notReadStories", "searchStories"}, allEntries = true)
    @Transactional
    public void deleteStory(Long storyId, Long userId) {
        Story story = getStoryById(storyId);
        User user = helperService.getUserById(userId);
        user.setReadStories(user.getReadStories().stream().filter(s -> !Objects.equals(s.getId(), storyId)).collect(Collectors.toList()));
        user.setLikedStories(user.getLikedStories().stream().filter(s -> !Objects.equals(s.getId(), storyId)).collect(Collectors.toList()));
        storyRepository.delete(story);
        userRepository.save(user);
    }
}