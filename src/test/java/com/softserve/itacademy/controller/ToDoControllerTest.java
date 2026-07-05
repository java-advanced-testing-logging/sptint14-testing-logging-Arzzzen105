package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.todoDto.CreateToDoDto;
import com.softserve.itacademy.dto.todoDto.ToDoDtoConverter;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ToDoController.class)
class ToDoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToDoService todoService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    @MockBean
    private ToDoDtoConverter todoDtoConverter;

    private User owner;
    private ToDo todo;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setFirstName("Arsen");

        todo = new ToDo();
        todo.setId(10L);
        todo.setTitle("Weekend Plans");
        todo.setOwner(owner);
        todo.setTasks(new HashSet<>());
        todo.setCollaborators(new HashSet<>());
    }

    @Test
    void createToDoFormShouldReturnView() throws Exception {
        mockMvc.perform(get("/todos/create/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-todo"))
                .andExpect(model().attributeExists("todo"))
                .andExpect(model().attribute("ownerId", 1L));
    }

    @Test
    void createToDoShouldRedirectWhenValid() throws Exception {
        when(userService.readById(1L)).thenReturn(owner);
        when(todoDtoConverter.toEntity(any(CreateToDoDto.class), any(User.class))).thenReturn(todo);
        when(todoService.create(any(ToDo.class))).thenReturn(todo);

        mockMvc.perform(post("/todos/create/users/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Valid Title")
                        .param("ownerId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));

        verify(todoService, times(1)).create(any(ToDo.class));
    }

    @Test
    void createToDoShouldReturnFormWhenValidationFails() throws Exception {
        mockMvc.perform(post("/todos/create/users/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("create-todo"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("ownerId", 1L));

        verify(todoService, never()).create(any());
    }

    @Test
    void createToDoShouldReturnFormWhenTitleAlreadyExists() throws Exception {
        when(userService.readById(1L)).thenReturn(owner);
        when(todoDtoConverter.toEntity(any(CreateToDoDto.class), any(User.class))).thenReturn(todo);

        when(todoService.create(any(ToDo.class)))
                .thenThrow(new IllegalArgumentException("ToDo with title 'Weekend Plans' already exists"));

        mockMvc.perform(post("/todos/create/users/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Weekend Plans"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-todo"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("ownerId", 1L));
    }

    @Test
    void updateToDoFormShouldReturnView() throws Exception {
        when(todoService.readById(10L)).thenReturn(todo);

        mockMvc.perform(get("/todos/10/update/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-todo"))
                .andExpect(model().attributeExists("todo"));

        verify(todoService, times(1)).readById(10L);
    }

    @Test
    void updateToDoShouldRedirectWhenValid() throws Exception {
        when(todoService.readById(10L)).thenReturn(todo);
        when(userService.readById(1L)).thenReturn(owner);
        when(todoService.update(any(ToDo.class))).thenReturn(todo);

        mockMvc.perform(post("/todos/10/update/users/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "10")
                        .param("title", "Updated Title")
                        .param("ownerId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));

        verify(todoService, times(1)).update(any(ToDo.class));
    }

    @Test
    void deleteShouldRedirectToUserToDos() throws Exception {
        mockMvc.perform(get("/todos/10/delete/users/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));

        verify(todoService, times(1)).delete(10L);
    }
}