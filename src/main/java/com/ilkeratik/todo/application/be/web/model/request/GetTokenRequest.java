package com.ilkeratik.todo.application.be.web.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTokenRequest {

  private String code;
  private String redirectUri;

}
