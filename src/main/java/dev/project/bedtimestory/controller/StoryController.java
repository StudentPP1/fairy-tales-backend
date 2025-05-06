package dev.project.bedtimestory.controller;

import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.request.CreateStoryRequest;
import dev.project.bedtimestory.request.UpdateStoryRequest;
import dev.project.bedtimestory.service.StoryService;
import dev.project.bedtimestory.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.rmi.ServerException;

@RestController
@RequestMapping("/api/story")
@RequiredArgsConstructor
public class StoryController {
    private final StoryService storyService;

    @GetMapping
    public ResponseEntity<Story> getStory(@RequestParam("storyId") Long storyId) {
        return ResponseEntity.ok(storyService.getStoryById(storyId));
    }

    @GetMapping("/topStories")
    public ResponseEntity<Page<StoryDto>> getMostLikedStories(Pageable pageable) {
        return ResponseEntity.ok(storyService.getMostLikedStories(pageable));
    }
    @GetMapping("/notReadStories")
    public ResponseEntity<Page<StoryDto>> getNotReadStories(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws ServerException {
        return ResponseEntity.ok(storyService.getNotReadStories(pageable, AuthUtils.getCurrentUserId(userDetails)));
    }
    @GetMapping("/getStories")
    public ResponseEntity<Page<StoryDto>> getStories(Pageable pageable) {
        return ResponseEntity.ok(storyService.getStories(pageable));
    }
    @GetMapping("/search")
    public ResponseEntity<Page<StoryDto>> searchStories(@RequestParam("query") String query, Pageable pageable) {
        return ResponseEntity.ok(storyService.searchStories(query, pageable));
    }
    
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void createStory(@Valid @RequestBody CreateStoryRequest request) {
        storyService.createStory(request);
    }
    @PostMapping("/update")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateStory(@Valid @RequestBody UpdateStoryRequest request) {
        storyService.updateStory(request);
    }
    @DeleteMapping("/update")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteStory(
            @RequestParam("storyId") Long storyId,
            @AuthenticationPrincipal UserDetails userDetails) throws ServerException {
        storyService.deleteStory(storyId, AuthUtils.getCurrentUserId(userDetails));
    }
}