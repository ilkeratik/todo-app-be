package com.ilkeratik.todo.application.be.web.controller;

import com.ilkeratik.todo.application.be.domain.auth.cognito.CognitoService;
import com.ilkeratik.todo.application.be.domain.auth.cognito.model.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Operations to get tokens")
@RequiredArgsConstructor
public class AuthController {

  private final CognitoService cognitoService;

  @PostMapping("/token")
  @Operation(summary = "Get token by authorization code and redirectionUri", description = "Retrieves token by code and redirectionUri.")
  public ResponseEntity<TokenResponse> getToken(@RequestParam String code,
      @RequestParam String redirectUri) {
    TokenResponse tokenResponse = cognitoService.getToken("authorization_code", code, redirectUri);
    return ResponseEntity.ok(tokenResponse);
  }
}