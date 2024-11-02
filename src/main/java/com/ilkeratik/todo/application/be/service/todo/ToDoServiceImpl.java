package com.ilkeratik.todo.application.be.service.todo;

import com.ilkeratik.todo.application.be.data.entity.ToDo;
import com.ilkeratik.todo.application.be.data.repository.ToDoRepository;
import com.ilkeratik.todo.application.be.service.todo.mapper.ToDoMapper;
import com.ilkeratik.todo.application.be.web.model.request.CreateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.response.CreateToDoResponse;
import com.ilkeratik.todo.application.be.web.model.response.ToDoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToDoServiceImpl implements ToDoService {

  static final ToDoMapper TODO_MAPPER = ToDoMapper.getInstance();
  private final ToDoRepository toDoRepository;

  @Override
  public CreateToDoResponse create(CreateToDoRequest createToDoRequest) {
    ToDo toDo = toDoRepository.save(TODO_MAPPER.toEntity(createToDoRequest));
    return new CreateToDoResponse(toDo.getId());
  }

  @Override
  public ToDoDTO get(String id) {
    ToDo toDo = toDoRepository.findById(Long.valueOf(id)).orElse(null);
    if (toDo == null) {
      return null;
    } else {
      return TODO_MAPPER.toDTO(toDo);
    }
  }

}
