package com.ilkeratik.todo.application.be.web.controller;

import com.ilkeratik.todo.application.be.common.exception.ResourceNotFoundException;
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
@Tag(name = "To do", description = "Operations to manage To do's")
@RequiredArgsConstructor
public class ToDoController {

  public static final String TO_DO_ITEM_NOT_FOUND_WITH_ID = "To do item not found with id: ";
  private final ToDoService toDoService;

  @PostMapping()
  @Operation(summary = "Create a To do item", description = "Creates a new To do item.")
  public ResponseEntity<CreateToDoResponse> create(
      @RequestBody CreateToDoRequest createToDoRequest) {
    CreateToDoResponse response = toDoService.create(createToDoRequest);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a To do item by ID", description = "Retrieves a To do item by its ID.")
  public ResponseEntity<ToDoDTO> get(@PathVariable Long id) {
    ToDoDTO response = toDoService.get(id);
    if (response == null) {
      throw new ResourceNotFoundException(TO_DO_ITEM_NOT_FOUND_WITH_ID + id);
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping()
  @Operation(summary = "Get all To do's of a user", description = "Retrieves all To do's using the user ID.")
  public ResponseEntity<List<ToDoDTO>> getAll() {
    List<ToDoDTO> response = toDoService.getAll();
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update a To do item", description = "Updates a To do item.")
  public ResponseEntity<ToDoDTO> get(@PathVariable Long id,
      @RequestBody UpdateToDoRequest updateToDoRequest) {
    ToDoDTO response = toDoService.update(id, updateToDoRequest);
    if (response == null) {
      throw new ResourceNotFoundException(TO_DO_ITEM_NOT_FOUND_WITH_ID + id);
    }
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a To do", description = "Deletes a To do item.")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (toDoService.delete(id)) {
      throw new ResourceNotFoundException(TO_DO_ITEM_NOT_FOUND_WITH_ID + id);
    } else {
      return ResponseEntity.ok().build();
    }
  }

}
