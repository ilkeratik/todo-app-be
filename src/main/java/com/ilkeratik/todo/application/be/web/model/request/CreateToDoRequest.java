package com.ilkeratik.todo.application.be.web.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateToDoRequest {

  @NotNull
  @Length(min = 1, max = 255)
  private String title;
  @NotNull
  @Length(min = 1, max = 1000)
  private String description;
  private String image;
  @NotNull
  @Length(min = 1, max = 10)
  private String priority;
  @NotNull
  @Length(min = 1, max = 20)
  private String status;
  private String category;
  private LocalDateTime deadline;
}
