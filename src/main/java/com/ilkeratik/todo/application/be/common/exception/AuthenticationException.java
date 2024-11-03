package com.ilkeratik.todo.application.be.common.exception;

public class AuthenticationException extends RuntimeException {

  public AuthenticationException(String message) {
    super(message);
  }
}
