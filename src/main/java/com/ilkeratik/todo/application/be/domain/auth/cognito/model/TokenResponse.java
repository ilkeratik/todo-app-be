package com.ilkeratik.todo.application.be.domain.auth.cognito.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {

  private String accessToken;
  private String tokenType;
  private int expiresIn;
  private String refreshToken;
}
