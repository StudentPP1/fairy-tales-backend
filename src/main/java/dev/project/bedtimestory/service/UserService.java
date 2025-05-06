package dev.project.bedtimestory.service;

import dev.project.bedtimestory.entity.User;
import dev.project.bedtimestory.exception.ApiException;
import dev.project.bedtimestory.repository.UserRepository;
import dev.project.bedtimestory.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public AppUserDetails getUserDetailsById(Long id) {
        return new AppUserDetails(
                userRepository.findById(id)
                        .orElseThrow(() -> ApiException.builder().status(404).message("User not found").build())
        );
    }
    public User getUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> ApiException.builder().status(404).message("User not found").build());
    }

    public User save(User user) {
        return userRepository.save(user);
    }
    public AppUserDetails getUserFromContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AppUserDetails user) {
            return user;
        }
        else {
            throw ApiException.builder()
                    .message("User not found")
                    .status(HttpStatus.NOT_FOUND.value())
                    .build();
        }
    }
}
