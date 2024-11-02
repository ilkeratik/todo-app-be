package com.ilkeratik.todo.application.be.data.repository;

import com.ilkeratik.todo.application.be.data.entity.ToDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToDoRepository extends JpaRepository<ToDo, Long> {

}
