package com.ilkeratik.todo.application.be.domain.user.service;

import com.ilkeratik.todo.application.be.common.exception.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  public String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AuthenticationException("This operation requires an authenticated user");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof Jwt jwt) {
      return jwt.getSubject();
    } else {
      throw new AuthenticationException("Unexpected principal type: " + principal.getClass());
    }
  }
}
