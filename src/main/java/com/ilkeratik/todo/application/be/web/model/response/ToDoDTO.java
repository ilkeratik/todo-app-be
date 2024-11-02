package com.ilkeratik.todo.application.be.web.model.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToDoDTO {
  private String title;
  private String description;
  private String image;
  private String priority;
  private String status;
  private String category;
  private LocalDateTime deadline;

}
