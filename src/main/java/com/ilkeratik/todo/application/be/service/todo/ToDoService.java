package com.ilkeratik.todo.application.be.service.todo;

import com.ilkeratik.todo.application.be.web.model.request.CreateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.response.CreateToDoResponse;
import com.ilkeratik.todo.application.be.web.model.response.ToDoDTO;

public interface ToDoService {
  CreateToDoResponse create(CreateToDoRequest createToDoRequest);
  ToDoDTO get(String id);
}
