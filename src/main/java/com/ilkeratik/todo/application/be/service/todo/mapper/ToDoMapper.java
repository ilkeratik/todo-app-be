package com.ilkeratik.todo.application.be.service.todo.mapper;

import com.ilkeratik.todo.application.be.data.entity.ToDo;
import com.ilkeratik.todo.application.be.data.enumeration.Status;
import com.ilkeratik.todo.application.be.web.model.request.CreateToDoRequest;
import com.ilkeratik.todo.application.be.web.model.response.ToDoDTO;
import java.util.List;

public class ToDoMapper {

  private static final ToDoMapper INSTANCE = new ToDoMapper();

  private ToDoMapper() {
  }

  public static ToDoMapper getInstance() {
    return INSTANCE;
  }

  public ToDo toEntity(CreateToDoRequest createToDoRequest, String userId) {
    final ToDo toDo = new ToDo();
    toDo.setTitle(createToDoRequest.getTitle());
    toDo.setDescription(createToDoRequest.getDescription());
    toDo.setStatus(Status.fromValue(createToDoRequest.getStatus()));
    toDo.setCategory(createToDoRequest.getCategory());
    toDo.setDeadline(createToDoRequest.getDeadline());
    toDo.setPriority(createToDoRequest.getPriority());
    toDo.setImage(createToDoRequest.getImage());
    toDo.setUserId(userId);
    return toDo;
  }

  public ToDoDTO toDTO(ToDo toDoEntity) {
    ToDoDTO toDoDTO = new ToDoDTO();
    toDoDTO.setTitle(toDoEntity.getTitle());
    toDoDTO.setDescription(toDoEntity.getDescription());
    toDoDTO.setStatus(toDoEntity.getStatus().name());
    toDoDTO.setCategory(toDoEntity.getCategory());
    toDoDTO.setDeadline(toDoEntity.getDeadline());
    toDoDTO.setPriority(toDoEntity.getPriority());
    toDoDTO.setImage(toDoEntity.getImage());
    toDoDTO.setCreatedAt(toDoEntity.getCreatedAt());
    toDoDTO.setUpdatedAt(toDoEntity.getUpdatedAt());
    return toDoDTO;
  }

  public List<ToDoDTO> toDTOList(List<ToDo> toDoEntityList) {
    return toDoEntityList.stream().map(this::toDTO).toList();
  }
}
