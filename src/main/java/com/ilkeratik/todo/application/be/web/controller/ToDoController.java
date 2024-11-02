package com.ilkeratik.todo.application.be.web.controller;

import com.ilkeratik.todo.application.be.service.todo.ToDoService;
import com.ilkeratik.todo.application.be.web.model.request.CreateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.response.CreateToDoResponse;
import com.ilkeratik.todo.application.be.web.model.response.ToDoDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/todo")
@Tag(name = "ToDo", description = "Operations to manage ToDo/s")
@RequiredArgsConstructor
public class ToDoController {

  private final ToDoService toDoService;

  @PostMapping()
  public ResponseEntity<CreateToDoResponse> create(
      @RequestBody CreateToDoRequest createToDoRequest) {
    CreateToDoResponse response = toDoService.create(createToDoRequest);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ToDoDTO> get(@PathVariable String id) {
    ToDoDTO response = toDoService.get(id);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

}
