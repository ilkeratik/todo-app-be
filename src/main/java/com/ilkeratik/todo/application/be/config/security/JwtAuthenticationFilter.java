package com.ilkeratik.todo.application.be.config.security;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    validateToken(request);
    filterChain.doFilter(request, response);
  }

  public void validateToken(HttpServletRequest request) {
    try {
      String token = request.getHeader("Authorization");
      if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);
        JWT jwt = JWTParser.parse(token);
        Map<String, Object> claims = jwt.getJWTClaimsSet().getClaims();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            claims.get("cognito:username"), null, Collections.emptyList());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    } catch (ParseException e) {
      throw new AccessDeniedException("Invalid token");
    } catch (Exception e) {
      log.error("Error on jwt token:", e);
    }
  }
}
