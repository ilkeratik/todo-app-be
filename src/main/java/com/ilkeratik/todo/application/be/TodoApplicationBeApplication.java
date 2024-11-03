package com.ilkeratik.todo.application.be;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "To do Application API", version = "1.0", description = "API Docs for To do application"))
@SpringBootApplication
@EnableEncryptableProperties
public class TodoApplicationBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(TodoApplicationBeApplication.class, args);
  }

}
