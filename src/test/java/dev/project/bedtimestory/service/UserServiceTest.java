package dev.project.bedtimestory.service;

import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.dto.UserDto;
import dev.project.bedtimestory.entity.Role;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.exception.ApiException;
import dev.project.bedtimestory.repository.StoryRepository;
import dev.project.bedtimestory.repository.UserRepository;
import dev.project.bedtimestory.request.UpdateUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // ! activate annotations for MockMvc
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private HelperService helperService;

    @InjectMocks // ! inject mocked beans to service
    private UserService userService;

    @Test
    void shouldReturnUserByEmail() {
        final String email = "test@gmail.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserByEmail(email);
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void shouldThrowWhenNotFoundUserByEmail() {
        when(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.getUserByEmail("nope@example.com"));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatus());
    }

    @Test
    void shouldReturnSavedUser() {
        final String email = "test@gmail.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        when(userRepository.save(mockUser)).thenReturn(mockUser);

        User result = userService.save(mockUser);
        assertEquals(mockUser, result);
    }

    @Test
    void shouldReturnUserDtoByUserId() {
        final String email = "test@gmail.com";
        final Long id = 1L;

        User mockUser = new User();
        UserDto mockUserDto = new UserDto();
        mockUser.setId(id);
        mockUser.setEmail(email);
        mockUserDto.setEmail(email);

        when(userRepository.getUserDtoById(id)).thenReturn(Optional.of(mockUserDto));

        UserDto result = userService.getUserDto(id);
        assertEquals(email, result.getEmail());
    }

    @Test
    void shouldReturnReadStoriesByUserId() {
        final Long id = 1L;
        StoryDto storyDto =  new StoryDto(1L, "Story", "Desc", "img", 5);

        when(userRepository.findReadStoryByUserId(id)).thenReturn(List.of(storyDto));
        List<StoryDto> stories = userService.getReadStories(id);
        assertEquals(List.of(storyDto), stories);
    }

    @Test
    void shouldReturnLikedStoriesByUserId() {
        final Long id = 1L;
        StoryDto storyDto = new StoryDto(1L, "Story", "Desc", "img", 5);

        when(userRepository.findLikedStoryByUserId(id)).thenReturn(List.of(storyDto));
        List<StoryDto> stories = userService.getLikedStories(id);
        assertEquals(List.of(storyDto), stories);
    }

    @Test
    void shouldReturnUpdatedUser() {
        final Long id = 1L;

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setName("Old Name");
        existingUser.setImg("old-img.png");
        existingUser.setSubscribed(false);
        existingUser.setEmail("test@example.com");
        existingUser.setRole(Role.USER);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");
        request.setImg("new-img.png");
        request.setIsSubscribed(true);

        when(helperService.getUserById(id)).thenReturn(existingUser);
        // ! arguments: (any User.class) => return first argument in method (User.class)
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(id, request);

        assertEquals("New Name", result.getName());
        assertEquals("new-img.png", result.getImg());
        assertTrue(result.isSubscribed());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());

        verify(helperService).getUserById(id);
        verify(userRepository).save(existingUser);
    }

    @Test
    void shouldAddLikedStory() {
        Long userId = 1L;
        Long storyId = 10L;

        User user = new User();
        user.setId(userId);

        Story story = new Story();
        story.setId(storyId);
        story.setLikedCount(0);

        when(helperService.getUserById(userId)).thenReturn(user);
        when(helperService.getStoryById(storyId)).thenReturn(story);

        userService.addLikedStory(userId, storyId);

        assertTrue(user.getLikedStories().contains(story));
        assertEquals(1, story.getLikedCount());

        verify(userRepository).save(user);
        verify(storyRepository).save(story);
    }

    @Test
    void shouldRemoveLikedStory() {
        Long userId = 1L;
        Long storyId = 10L;

        User user = new User();
        user.setId(userId);

        Story story = new Story();
        story.setId(storyId);
        story.setLikedCount(1);

        when(helperService.getUserById(userId)).thenReturn(user);
        when(helperService.getStoryById(storyId)).thenReturn(story);

        userService.removeLikedStory(userId, storyId);

        Assertions.assertFalse(user.getLikedStories().contains(story));
        assertEquals(0, story.getLikedCount());

        verify(userRepository).save(user);
        verify(storyRepository).save(story);
    }

    @Test
    void shouldAddReadStory() {
        Long userId = 1L;
        Long storyId = 10L;

        User user = new User();
        user.setId(userId);

        Story story = new Story();
        story.setId(storyId);

        when(helperService.getUserById(userId)).thenReturn(user);
        when(helperService.getStoryById(storyId)).thenReturn(story);

        userService.addReadStory(userId, storyId);

        assertTrue(user.getReadStories().contains(story));

        verify(userRepository).save(user);
    }

    @Test
    void shouldRemoveReadStory() {
        Long userId = 1L;
        Long storyId = 10L;

        Story story = new Story();
        story.setId(storyId);

        User user = new User();
        user.setId(userId);
        user.setReadStories(new ArrayList<>(List.of(story)));

        when(helperService.getUserById(userId)).thenReturn(user);
        when(helperService.getStoryById(storyId)).thenReturn(story);

        userService.removeReadStory(userId, storyId);

        Assertions.assertFalse(user.getReadStories().contains(story));

        verify(userRepository).save(user);
    }
}