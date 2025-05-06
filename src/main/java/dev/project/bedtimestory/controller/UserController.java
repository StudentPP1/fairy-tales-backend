package dev.project.bedtimestory.controller;

import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.dto.UserDto;
import dev.project.bedtimestory.request.UpdateUserRequest;
import dev.project.bedtimestory.service.UserService;
import dev.project.bedtimestory.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.rmi.ServerException;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping
    public ResponseEntity<UserDto> getUserDetails(
            @AuthenticationPrincipal UserDetails userDetails
    ) throws ServerException {
        return ResponseEntity.ok(userService.getUserDto(AuthUtils.getCurrentUserId(userDetails)));
    }

    @GetMapping("/readStories")
    public ResponseEntity<List<StoryDto>> getReadStories(
            @AuthenticationPrincipal UserDetails userDetails
    ) throws ServerException {
        return ResponseEntity.ok(userService.getReadStories(AuthUtils.getCurrentUserId(userDetails)));
    }

    @GetMapping("/likedStories")
    public ResponseEntity<List<StoryDto>> getLikedStories(
            @AuthenticationPrincipal UserDetails userDetails
    ) throws ServerException {
        return ResponseEntity.ok(userService.getLikedStories(AuthUtils.getCurrentUserId(userDetails)));
    }

    @PostMapping("/update")
    public void updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) throws ServerException {
        userService.updateUser(
                AuthUtils.getCurrentUserId(userDetails),
                request
        );
    }

    @PostMapping("/addLikedStory")
    public void addLikedStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("storyId") Long storyId
    ) throws ServerException {
        userService.addLikedStory(
                AuthUtils.getCurrentUserId(userDetails),
                storyId
        );
    }

    @PostMapping("/addReadStory")
    public void addReadStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("storyId") Long storyId
    ) throws ServerException {
        userService.addReadStory(
                AuthUtils.getCurrentUserId(userDetails),
                storyId
        );
    }
    @PostMapping("/removeLikedStory")
    public void removeLikedStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("storyId") Long storyId
    ) throws ServerException {
        userService.removeLikedStory(
                AuthUtils.getCurrentUserId(userDetails),
                storyId
        );
    }
    @PostMapping("/removeReadStory")
    public void removeReadStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("storyId") Long storyId
    ) throws ServerException {
        userService.removeReadStory(
                AuthUtils.getCurrentUserId(userDetails),
                storyId
        );
    }
    @PutMapping("/subscribe")
    public void subscribe(@AuthenticationPrincipal UserDetails userDetails) throws ServerException {
        userService.subscribe(AuthUtils.getCurrentUserId(userDetails));
    }
    @PutMapping("/unsubscribe")
    public void unsubscribe(@AuthenticationPrincipal UserDetails userDetails) throws ServerException {
        userService.unsubscribe(AuthUtils.getCurrentUserId(userDetails));
    }
}