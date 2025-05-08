package dev.project.bedtimestory.utils;

import dev.project.bedtimestory.security.AppUserDetails;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.rmi.ServerException;

@UtilityClass
public class AuthUtils {
    public Long getCurrentUserId(UserDetails userDetails) throws ServerException {
        if (userDetails instanceof AppUserDetails details) {
            return details.getId();
        }
        throw new ServerException("userDetails is not instance of AppUserDetails");
    }
    public AppUserDetails getAuthenticatedUser() throws ServerException {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AppUserDetails details) {
            return details;
        }
        throw new ServerException("principal in security context is not instance of AppUserDetails");
    }
}