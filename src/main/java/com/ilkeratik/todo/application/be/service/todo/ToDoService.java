package com.ilkeratik.todo.application.be.service.todo;

import com.ilkeratik.todo.application.be.web.model.request.CreateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.request.UpdateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.response.CreateToDoResponse;
import com.ilkeratik.todo.application.be.web.model.response.ToDoDTO;
import java.util.List;

public interface ToDoService {

  CreateToDoResponse create(CreateToDoRequest createToDoRequest);

  ToDoDTO get(Long id);

  List<ToDoDTO> getAll();

  ToDoDTO update(Long id, UpdateToDoRequest updateToDoRequest);

  boolean delete(Long id);

}
