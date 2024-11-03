package com.ilkeratik.todo.application.be.domain.auth.cognito;

import com.ilkeratik.todo.application.be.domain.auth.cognito.model.TokenResponse;

public interface CognitoService {

  TokenResponse getToken(String grantType, String code, String redirectUri);
}
