package com.ilkeratik.todo.application.be.web.controller;

import com.ilkeratik.todo.application.be.service.todo.ToDoService;
import com.ilkeratik.todo.application.be.web.model.request.CreateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.request.UpdateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.response.CreateToDoResponse;
import com.ilkeratik.todo.application.be.web.model.response.ToDoDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
  public ResponseEntity<ToDoDTO> get(@PathVariable Long id) {
    ToDoDTO response = toDoService.get(id);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping()
  public ResponseEntity<List<ToDoDTO>> getAll() {
    List<ToDoDTO> response = toDoService.getAll();
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ToDoDTO> get(@PathVariable Long id,
      @RequestBody UpdateToDoRequest updateToDoRequest) {
    ToDoDTO response = toDoService.update(id, updateToDoRequest);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (toDoService.delete(id)) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}
