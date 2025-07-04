package dev.project.bedtimestory.service;

import dev.project.bedtimestory.dto.StoryDetailsDto;
import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.repository.StoryRepository;
import dev.project.bedtimestory.repository.UserRepository;
import dev.project.bedtimestory.request.CreateStoryRequest;
import dev.project.bedtimestory.request.UpdateStoryRequest;
import dev.project.bedtimestory.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HelperService helperService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StoryService storyService;

    @Test
    void shouldReturnStoryByStoryId() {
        Long storyId = 1L;
        Story story = new Story();
        story.setId(storyId);
        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));

        Story result = storyService.getStoryById(storyId);
        assertThat(result.getId()).isEqualTo(storyId);
    }

    @Test
    void shouldReturnStoryDetailsByStoryIdAndUserId() {
        Long storyId = 1L;
        Long userId = 1L;
        StoryDetailsDto storyDetailsDto = new StoryDetailsDto(storyId, "title", "desc",
                "imgUrl", "text", 0, false, false);

        when(storyRepository.getStoryDetailsDto(storyId, userId)).thenReturn(Optional.of(storyDetailsDto));

        StoryDetailsDto result = storyService.getStoryDetailsById(storyId, userId);
        assertThat(result.getId()).isEqualTo(storyId);
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getDescription()).isEqualTo("desc");
    }

    @Test
    void shouldReturnMostLikedStories() {
        Pageable pageable = PageRequest.of(0, 10);
        StoryDto dto = new StoryDto(1L, "title", "desc", "img", 5);
        Page<StoryDto> page = new PageImpl<>(List.of(dto));

        when(storyRepository.findMostLikedStories(pageable)).thenReturn(page);

        Page<StoryDto> result = storyService.getMostLikedStories(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));

        verify(storyRepository).findMostLikedStories(pageable);
    }

    @Test
    void shouldReturnNotReadStoriesByUserId() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        StoryDto dto = new StoryDto(1L, "title", "desc", "img", 5);
        Page<StoryDto> page = new PageImpl<>(List.of(dto));

        when(storyRepository.findNotReadStories(userId, pageable)).thenReturn(page);

        Page<StoryDto> result = storyService.getNotReadStories(pageable, userId);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));

        verify(storyRepository).findNotReadStories(userId, pageable);
    }

    @Test
    void shouldReturnStories() {
        Pageable pageable = PageRequest.of(0, 10);
        StoryDto dto = new StoryDto(1L, "title", "desc", "img", 5);
        Page<StoryDto> page = new PageImpl<>(List.of(dto));

        when(storyRepository.findStories(pageable)).thenReturn(page);

        Page<StoryDto> result = storyService.getStories(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));

        verify(storyRepository).findStories(pageable);
    }

    @Test
    void shouldReturnSearchedStories() {
        String query = "title";
        Pageable pageable = PageRequest.of(0, 10);
        StoryDto dto = new StoryDto(1L, "title", "desc", "img", 5);
        Page<StoryDto> page = new PageImpl<>(List.of(dto));

        when(storyRepository.searchStories(query, pageable)).thenReturn(page);

        Page<StoryDto> result = storyService.searchStories(query, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().get(0));

        verify(storyRepository).searchStories(query, pageable);
    }

    @Test
    void shouldReturnUpdatedStory() {
        Long storyId = 1L;
        Story existingStory = new Story();
        existingStory.setId(storyId);
        existingStory.setImgUrl("img");
        existingStory.setDescription("desc");

        UpdateStoryRequest request = new UpdateStoryRequest();
        request.setId(storyId);
        request.setImgUrl("img");
        request.setDescription("new desc");

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(existingStory));
        when(storyRepository.save(any(Story.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoryDto dto = storyService.updateStory(request);
        assertEquals(storyId, dto.getId());
        assertEquals("new desc", dto.getDescription());
        assertEquals("img", dto.getImgUrl());
    }

    @Test
    void shouldReturnCreatedStory() {
        CreateStoryRequest request = new CreateStoryRequest();
        request.setTitle("Title");
        request.setDescription("Description");
        request.setImgUrl("img.png");
        request.setText("Story text");

        Story savedStory = new Story();
        savedStory.setId(1L);
        savedStory.setTitle(request.getTitle());
        savedStory.setDescription(request.getDescription());
        savedStory.setImgUrl(request.getImgUrl());
        savedStory.setText(request.getText());
        savedStory.setLikedCount(0);

        when(storyRepository.save(any(Story.class))).thenReturn(savedStory);

        StoryDto result = storyService.createStory(request);

        assertEquals(savedStory.getId(), result.getId());
        assertEquals(savedStory.getTitle(), result.getTitle());
        assertEquals(savedStory.getDescription(), result.getDescription());
        assertEquals(savedStory.getImgUrl(), result.getImgUrl());
        assertEquals(savedStory.getLikedCount(), result.getLikedCount());

        verify(storyRepository).save(any(Story.class));
        verify(notificationService).sendNotification(savedStory.getTitle(), savedStory.getDescription());
    }

    @Test
    void shouldReturnVoidAfterDeletedStory() {
        Long storyId = 1L;
        Long userId = 1L;

        Story story = new Story();
        story.setId(storyId);

        User user = new User();
        user.setId(userId);
        user.setReadStories(new ArrayList<>(List.of(story)));
        user.setLikedStories(new ArrayList<>(List.of(story)));

        when(storyRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(helperService.getUserById(userId)).thenReturn(user);

        storyService.deleteStory(storyId, userId);

        verify(storyRepository).delete(story);
        verify(userRepository).save(user);

        assertTrue(user.getReadStories().isEmpty());
        assertTrue(user.getLikedStories().isEmpty());
    }
}