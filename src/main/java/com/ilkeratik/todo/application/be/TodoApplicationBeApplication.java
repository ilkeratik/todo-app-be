package com.ilkeratik.todo.application.be;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@OpenAPIDefinition(info = @Info(title = "ToDo Application API", version = "1.0", description = "API Docs for ToDo Application"))
@SpringBootApplication
public class TodoApplicationBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(TodoApplicationBeApplication.class, args);
  }

}
