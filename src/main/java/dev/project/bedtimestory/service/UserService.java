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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final HelperService helperService;
    public User getUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND.value()));
    }
    public User save(User user) {
        return userRepository.save(user);
    }
    public UserDto getUserDto(Long userId) {
        return userRepository.getUserDtoById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND.value()));
    }
    public List<StoryDto> getReadStories(Long userId) {
        return userRepository.findReadStoryByUserId(userId);
    }
    public List<StoryDto> getLikedStories(Long userId) {
        return userRepository.findLikedStoryByUserId(userId);
    }
    @Transactional
    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        User user = helperService.getUserById(userId);
        user.setName(request.getName());
        user.setImg(request.getImg());
        user = userRepository.save(user);
        return UserDto.builder()
                .name(user.getName())
                .img(user.getImg())
                .role(user.getRole())
                .email(user.getEmail())
                .build();
    }
    @Transactional
    public void addLikedStory(Long userId, Long storyId) {
        User user = helperService.getUserById(userId);
        Story story = helperService.getStoryById(storyId);
        user.addLikedStory(story);
        story.setLikedCount(story.getLikedCount() + 1);
        userRepository.save(user);
        storyRepository.save(story);
    }
    @Transactional
    public void addReadStory(Long userId, Long storyId) {
        User user = helperService.getUserById(userId);
        user.addReadStory(helperService.getStoryById(storyId));
        userRepository.save(user);
    }
    @Transactional
    public void removeLikedStory(Long userId, Long storyId) {
        User user = helperService.getUserById(userId);
        Story story = helperService.getStoryById(storyId);
        user.removeLikedStory(story);
        story.setLikedCount(story.getLikedCount() - 1);
        userRepository.save(user);
        storyRepository.save(story);
    }
    @Transactional
    public void removeReadStory(Long userId, Long storyId) {
        User user = helperService.getUserById(userId);
        user.removeReadStory(helperService.getStoryById(storyId));
        userRepository.save(user);
    }
    @Transactional
    public void subscribe(Long userId) {
        User user = helperService.getUserById(userId);
        user.setSubscribed(true);
        userRepository.save(user);
    }
    @Transactional
    public void unsubscribe(Long userId) {
        User user = helperService.getUserById(userId);
        user.setSubscribed(false);
        userRepository.save(user);
    }
}