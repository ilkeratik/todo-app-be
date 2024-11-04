package com.ilkeratik.todo.application.be.domain.auth.cognito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilkeratik.todo.application.be.common.exception.AuthenticationException;
import com.ilkeratik.todo.application.be.domain.auth.cognito.model.TokenResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CognitoServiceImpl implements CognitoService {

  private final OkHttpClient httpClient = new OkHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Value("${cognito.client-id}")
  private String clientId;
  @Value("${cognito.client-secret}")
  private String clientSecret;
  @Value("${cognito.domain}")
  private String domain;

  public TokenResponse getToken(String grantType, String code, String redirectUri) {
    String secretHash = generateSecretHash(clientId, clientSecret);
    String tokenEndpoint = "https://" + domain + "/oauth2/token";

    RequestBody formBody = new FormBody.Builder()
        .add("grant_type", grantType)
        .add("client_id", clientId)
        .add("code", code)
        .add("redirect_uri", redirectUri)
        .build();

    Request request = new Request.Builder()
        .url(tokenEndpoint)
        .post(formBody)
        .addHeader("Authorization", "Basic " + secretHash)
        .addHeader("Content-Type", "application/x-www-form-urlencoded")
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected response code " + response.code() + ": " + responseBody);
      }
      return objectMapper.readValue(responseBody, TokenResponse.class);
    } catch (IOException e) {
      throw new AuthenticationException("Error while getting token");
    }
  }

  private String generateSecretHash(String clientId, String clientSecret) {
    String message = clientId + ":" + clientSecret;
    return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
  }

}
