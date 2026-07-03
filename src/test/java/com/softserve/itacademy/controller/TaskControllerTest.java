package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.TaskDto;
import com.softserve.itacademy.dto.TaskTransformer;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.service.StateService;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;
    @MockBean
    private ToDoService todoService;
    @MockBean
    private StateService stateService;
    @MockBean
    private TaskTransformer taskTransformer;

    @Test
    void createGet_ShouldReturnCreateTaskView() throws Exception {
        ToDo todo = new ToDo();
        todo.setId(1L);
        when(todoService.readById(1L)).thenReturn(todo);

        mockMvc.perform(get("/tasks/create/todos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-task"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attribute("todo", todo))
                .andExpect(model().attributeExists("priorities"));
    }

    @Test
    void createPost_ShouldRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/tasks/create/todos/1")
                .param("name", "New Task")
                .param("priority", "LOW")
                .param("todoId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/1/tasks"));

        verify(taskService).create(any(TaskDto.class));
    }

    @Test
    void createPost_ShouldReturnForm_WhenInvalid() throws Exception {
        ToDo todo = new ToDo();
        todo.setId(1L);
        when(todoService.readById(1L)).thenReturn(todo);

        mockMvc.perform(post("/tasks/create/todos/1")
                .param("name", "") // Empty name - invalid
                .param("priority", "LOW"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-task"))
                .andExpect(model().attributeExists("todo"));
    }

    @Test
    void updateGet_ShouldReturnUpdateTaskView() throws Exception {
        Task task = new Task();
        task.setId(1L);
        TaskDto taskDto = new TaskDto();
        taskDto.setId(1L);
        ToDo todo = new ToDo();
        todo.setId(1L);

        when(taskService.readById(1L)).thenReturn(task);
        when(taskTransformer.convertToDto(task)).thenReturn(taskDto);
        when(todoService.readById(1L)).thenReturn(todo);
        when(stateService.getAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/tasks/1/update/todos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-task"))
                .andExpect(model().attribute("task", taskDto))
                .andExpect(model().attribute("todo", todo));
    }

    @Test
    void delete_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/tasks/1/delete/todos/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/1/tasks"));

        verify(taskService).delete(1L);
    }
}
