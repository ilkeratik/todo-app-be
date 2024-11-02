package com.ilkeratik.todo.application.be.data.entity;

import com.ilkeratik.todo.application.be.data.enumeration.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Getter
@Setter
@Entity
@Table(name = "todo")
@EntityListeners(AuditingEntityListener.class)
public class ToDo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;
  
  private String title;

  private String description;

  private String image;

  private String priority;

  @Enumerated(EnumType.STRING)
  private Status status;

  private String category;

  private LocalDateTime deadline;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;
}