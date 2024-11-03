package com.ilkeratik.todo.application.be.domain.todo.service;

import com.ilkeratik.todo.application.be.common.exception.UnauthorizedResourceException;
import com.ilkeratik.todo.application.be.domain.todo.data.entity.ToDo;
import com.ilkeratik.todo.application.be.domain.todo.data.enumeration.Status;
import com.ilkeratik.todo.application.be.domain.todo.data.repository.ToDoRepository;
import com.ilkeratik.todo.application.be.domain.todo.mapper.ToDoMapper;
import com.ilkeratik.todo.application.be.domain.user.service.UserService;
import com.ilkeratik.todo.application.be.web.model.request.CreateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.request.UpdateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.response.CreateToDoResponse;
import com.ilkeratik.todo.application.be.web.model.response.ToDoDTO;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToDoServiceImpl implements ToDoService {

  static final ToDoMapper TODO_MAPPER = ToDoMapper.getInstance();
  private final ToDoRepository toDoRepository;
  private final UserService userService;

  @Override
  public CreateToDoResponse create(CreateToDoRequest createToDoRequest) {
    ToDo toDo = toDoRepository.save(
        TODO_MAPPER.toEntity(createToDoRequest, userService.getCurrentUserId()));
    return new CreateToDoResponse(toDo.getId());
  }

  @Override
  public ToDoDTO get(Long id) {
    ToDo toDo = toDoRepository.findById(id).orElse(null);
    if (toDo == null) {
      return null;
    } else if (!userService.getCurrentUserId().equals(toDo.getUserId())) {
      throw new UnauthorizedResourceException("You are not authorized to access this resource");
    } else {
      return TODO_MAPPER.toDTO(toDo);
    }
  }

  @Override
  public List<ToDoDTO> getAll() {
    List<ToDo> toDos = toDoRepository.findAllByUserId(userService.getCurrentUserId()).orElse(null);
    if (toDos == null) {
      return Collections.emptyList();
    } else {
      return TODO_MAPPER.toDTOList(toDos);
    }
  }

  @Override
  public ToDoDTO update(Long id, UpdateToDoRequest updateToDoRequest) {
    ToDo toDo = toDoRepository.findById(id).orElse(null);
    if (toDo == null) {
      return null;
    } else if (!userService.getCurrentUserId().equals(toDo.getUserId())) {
      throw new UnauthorizedResourceException("You are not authorized to update this resource");
    } else {
      updatePartially(updateToDoRequest, toDo);
      toDoRepository.save(toDo);
      return TODO_MAPPER.toDTO(toDo);
    }
  }

  @Override
  public boolean delete(Long id) {
    ToDo toDo = toDoRepository.findById(id).orElse(null);
    if (toDo == null) {
      return false;
    } else if (!userService.getCurrentUserId().equals(toDo.getUserId())) {
      throw new UnauthorizedResourceException("You are not authorized to update this resource");
    } else {
      toDoRepository.deleteById(id);
      return true;
    }
  }

  private void updatePartially(UpdateToDoRequest updateToDoRequest, ToDo toDo) {
    if (updateToDoRequest.getTitle() != null) {
      toDo.setTitle(updateToDoRequest.getTitle());
    }
    if (updateToDoRequest.getDescription() != null) {
      toDo.setDescription(updateToDoRequest.getDescription());
    }
    if (updateToDoRequest.getStatus() != null) {
      toDo.setStatus(Status.valueOf(updateToDoRequest.getStatus()));
    }
    if (updateToDoRequest.getCategory() != null) {
      toDo.setCategory(updateToDoRequest.getCategory());
    }
    if (updateToDoRequest.getDeadline() != null) {
      toDo.setDeadline(updateToDoRequest.getDeadline());
    }
    if (updateToDoRequest.getPriority() != null) {
      toDo.setPriority(updateToDoRequest.getPriority());
    }
    if (updateToDoRequest.getImage() != null) {
      toDo.setImage(updateToDoRequest.getImage());
    }
  }
}
