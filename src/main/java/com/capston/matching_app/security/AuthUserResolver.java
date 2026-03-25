package com.capston.matching_app.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUserResolver {

    public Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud && cud.getUserId() != null) {
            return cud.getUserId();
        }
        throw new AccessDeniedException("Cannot resolve userId from authentication principal");
    }
}
