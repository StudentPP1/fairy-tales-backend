package dev.project.bedtimestory.controller;

import dev.project.bedtimestory.dto.StoryDetailsDto;
import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.request.CreateStoryRequest;
import dev.project.bedtimestory.request.UpdateStoryRequest;
import dev.project.bedtimestory.service.StoryService;
import dev.project.bedtimestory.utils.AuthUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.rmi.ServerException;

@RestController
@RequestMapping("/api/story")
@RequiredArgsConstructor
public class StoryController {
    private final StoryService storyService;

    @GetMapping
    public ResponseEntity<StoryDetailsDto> getStory(
            @RequestParam("storyId") Long storyId,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws ServerException {
        return ResponseEntity.ok(storyService.getStoryDetailsById(
                storyId,
                AuthUtils.getCurrentUserId(userDetails)));
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
    public ResponseEntity<StoryDto> createStory(@Valid @RequestBody CreateStoryRequest request) {
        return ResponseEntity.ok(storyService.createStory(request));
    }
    @PostMapping("/update")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StoryDto> updateStory(@Valid @RequestBody UpdateStoryRequest request) {
        return ResponseEntity.ok(storyService.updateStory(request));
    }
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteStory(
            @NotNull HttpServletResponse response,
            @RequestParam("storyId") Long storyId,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        storyService.deleteStory(storyId, AuthUtils.getCurrentUserId(userDetails), response);
    }
}