package com.ilkeratik.todo.application.be.service.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  public String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof String username) {
      return username;
    } else {
      throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }
  }
}
