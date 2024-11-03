package com.ilkeratik.todo.application.be.web.controller;

import com.ilkeratik.todo.application.be.domain.todo.service.ToDoService;
import com.ilkeratik.todo.application.be.web.model.request.CreateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.request.UpdateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.response.CreateToDoResponse;
import com.ilkeratik.todo.application.be.web.model.response.ToDoDTO;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "ToDo", description = "Operations to manage ToDos")
@RequiredArgsConstructor
public class ToDoController {

  private final ToDoService toDoService;

  @PostMapping()
  @Operation(summary = "Create a ToDo", description = "Creates a new ToDo item.")
  public ResponseEntity<CreateToDoResponse> create(
      @RequestBody CreateToDoRequest createToDoRequest) {
    CreateToDoResponse response = toDoService.create(createToDoRequest);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a ToDo by ID", description = "Retrieves a ToDo item by its ID.")
  public ResponseEntity<ToDoDTO> get(@PathVariable Long id) {
    ToDoDTO response = toDoService.get(id);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping()
  @Operation(summary = "Get all ToDos of a user", description = "Retrieves all ToDos using the user ID.")
  public ResponseEntity<List<ToDoDTO>> getAll() {
    List<ToDoDTO> response = toDoService.getAll();
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update a ToDo", description = "Updates a ToDo item.")
  public ResponseEntity<ToDoDTO> get(@PathVariable Long id,
      @RequestBody UpdateToDoRequest updateToDoRequest) {
    ToDoDTO response = toDoService.update(id, updateToDoRequest);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a ToDo", description = "Deletes a ToDo item.")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (toDoService.delete(id)) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}
