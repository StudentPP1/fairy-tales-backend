package dev.project.bedtimestory.service;

import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.dto.UserDto;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.exception.ApiException;
import dev.project.bedtimestory.repository.StoryRepository;
import dev.project.bedtimestory.repository.UserRepository;
import dev.project.bedtimestory.request.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final HelperService helperService;
    public User getUserByEmail(String email) {
        log.info("UserService: get user by email");
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND.value()));
    }
    public User save(User user) {
        return userRepository.save(user);
    }
    public UserDto getUserDto(Long userId) {
        log.info("UserService: get user dto");
        return userRepository.getUserDtoById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND.value()));
    }
    public List<StoryDto> getReadStories(Long userId) {
        log.info("UserService: getReadStories");
        return userRepository.findReadStoryByUserId(userId);
    }
    public List<StoryDto> getLikedStories(Long userId) {
        log.info("UserService: getLikedStories");
        return userRepository.findLikedStoryByUserId(userId);
    }
    @Transactional
    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        log.info("UserService: updateUser with {}", request);
        User user = helperService.getUserById(userId);
        user.setName(request.getName());
        user.setImg(request.getImg());
        user.setSubscribed(request.getIsSubscribed());
        user = userRepository.save(user);
        return UserDto.builder()
                .name(user.getName())
                .img(user.getImg())
                .role(user.getRole())
                .email(user.getEmail())
                .isSubscribed(user.isSubscribed())
                .build();
    }
    @CacheEvict(value = {"mostLikedStories", "notReadStories", "searchStories"}, allEntries = true)
    @Transactional
    public void addLikedStory(Long userId, Long storyId) {
        log.info("UserService: addLikedStory");
        User user = helperService.getUserById(userId);
        Story story = helperService.getStoryById(storyId);
        user.addLikedStory(story);
        story.setLikedCount(story.getLikedCount() + 1);
        userRepository.save(user);
        storyRepository.save(story);
    }
    @CacheEvict(value = {"mostLikedStories", "notReadStories", "searchStories"}, allEntries = true)
    @Transactional
    public void removeLikedStory(Long userId, Long storyId) {
        log.info("UserService: removeLikedStory");
        User user = helperService.getUserById(userId);
        Story story = helperService.getStoryById(storyId);
        user.removeLikedStory(story);
        story.setLikedCount(story.getLikedCount() - 1);
        userRepository.save(user);
        storyRepository.save(story);
    }
    @CacheEvict(value = {"mostLikedStories", "notReadStories", "searchStories"}, allEntries = true)
    @Transactional
    public void addReadStory(Long userId, Long storyId) {
        log.info("UserService: addReadStory");
        User user = helperService.getUserById(userId);
        user.addReadStory(helperService.getStoryById(storyId));
        userRepository.save(user);
    }
    @CacheEvict(value = {"mostLikedStories", "notReadStories", "searchStories"}, allEntries = true)
    @Transactional
    public void removeReadStory(Long userId, Long storyId) {
        log.info("UserService: removeReadStory");
        User user = helperService.getUserById(userId);
        user.removeReadStory(helperService.getStoryById(storyId));
        userRepository.save(user);
    }
}