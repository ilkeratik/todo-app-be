package com.ilkeratik.todo.application.be.web.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateToDoRequest {

  private String title;
  private String description;
  private String image;
  private String priority;
  private String status;
  private String category;
  private LocalDateTime deadline;
}
