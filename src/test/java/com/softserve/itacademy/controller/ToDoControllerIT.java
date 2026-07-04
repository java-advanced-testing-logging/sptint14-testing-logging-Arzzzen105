package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.repository.ToDoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ToDoControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ToDoRepository toDoRepository;

    @Test
    void testPostUpdate() throws Exception {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("mail@mail.mail");
        user.setRole(UserRole.USER);
        user.setPassword("P@ssW0rd");

        ToDo todo = new ToDo();
        todo.setTitle("Personal");
        todo.setOwner(user);

        toDoRepository.save(todo);

        mockMvc.perform(post("/todos/1/update/users/1")
                .param("todoId", "1")
                .param("ownerId", "1")
                .param("title", "Weekend Plans")
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));

        Optional<ToDo> updated = toDoRepository.findById(1L);

        assertThat(updated)
                .isNotEmpty()
                .contains(todo);

    }
}
