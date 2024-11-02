package com.ilkeratik.todo.application.be;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class TodoApplicationBeApplicationTests {
  @Autowired
  ApplicationContext context;
  @Test
  void contextLoads() {
    assertNotNull(context);
  }

}
