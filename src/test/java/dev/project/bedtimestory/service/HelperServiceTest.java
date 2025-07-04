package dev.project.bedtimestory.service;

import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.repository.StoryRepository;
import dev.project.bedtimestory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelperServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private HelperService helperService;

    @Test
    void getUserById() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = helperService.getUserById(userId);
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void getStoryById() {
        Long storyId = 1L;
        Story story = new Story();
        story.setId(storyId);

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        Story result = helperService.getStoryById(storyId);
        assertThat(result.getId()).isEqualTo(storyId);
    }
}