package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.todoDto.CreateToDoDto;
import com.softserve.itacademy.dto.todoDto.ToDoDtoConverter;
import com.softserve.itacademy.dto.todoDto.UpdateToDoDto;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/todos")
@RequiredArgsConstructor
@Slf4j
public class ToDoController {

    private final ToDoService todoService;
    private final TaskService taskService;
    private final UserService userService;
    private final ToDoDtoConverter todoDtoConverter;

    @GetMapping("/create/users/{owner_id}")
    public String createToDoForm(@PathVariable("owner_id") Long ownerId, Model model) {
        log.debug("GET request for ToDo creating form from User with ID {}", ownerId);
        CreateToDoDto todoDto = new CreateToDoDto();
        todoDto.setOwnerId(ownerId);
        model.addAttribute("todo", todoDto);
        model.addAttribute("ownerId", ownerId);
        return "create-todo";
    }

    @PostMapping("/create/users/{owner_id}")
    public String createToDo(@PathVariable("owner_id") Long ownerId,
                            @Validated @ModelAttribute("todo") CreateToDoDto todoDto,
                            BindingResult result,
                            Model model) {
        log.info("POST request for ToDo creation from a User with ID {}", ownerId);
        if (result.hasErrors()) {
            log.warn("Validation failed for ToDo creation: {}", result.getAllErrors());
            model.addAttribute("ownerId", ownerId);
            return "create-todo";
        }
        User owner = userService.readById(ownerId);
        ToDo todo = todoDtoConverter.toEntity(todoDto, owner);
        try {
            todoService.create(todo);
            log.debug("ToDo with ID {} was successfully created", todo.getId());
        } catch (IllegalArgumentException e) {
            log.error("Error creating ToDo: ", e);
            result.rejectValue("title", "error.todo", e.getMessage());
            model.addAttribute("ownerId", ownerId);
            return "create-todo";
        }
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{todo_id}/update/users/{owner_id}")
    public String updateToDoForm(@PathVariable("todo_id") Long todoId,
                                 @PathVariable("owner_id") Long ownerId,
                                 Model model) {
        log.debug("GET request for update form a ToDo with ID {} of User with ID {}", todoId, ownerId);
        ToDo todo = todoService.readById(todoId);
        UpdateToDoDto todoDto = UpdateToDoDto.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .ownerId(todo.getOwner().getId())
                .build();
        model.addAttribute("todo", todoDto);
        return "update-todo";
    }

    @PostMapping("/{todo_id}/update/users/{owner_id}")
    public String updateToDo(@PathVariable("todo_id") Long todoId,
                            @PathVariable("owner_id") Long ownerId,
                            @Validated @ModelAttribute("todo") UpdateToDoDto todoDto,
                            BindingResult result,
                            Model model) {
        log.info("POST request from updating form for ToDo with ID {} from User with ID {}", todoId, ownerId);
        if (result.hasErrors()) {
            log.warn("Validation failed for ToDo updating: {}", result.getAllErrors());
            return "update-todo";
        }
        ToDo todo = todoService.readById(todoId);
        User owner = userService.readById(ownerId);
        todoDtoConverter.fillFields(todo, todoDto, owner);
        try {
            todoService.update(todo);
            log.debug("ToDo with ID {} updated successfully", todoId);
        } catch (IllegalArgumentException e) {
            log.error("Error updating ToDo: ", e);
            result.rejectValue("title", "error.todo", e.getMessage());
            return "update-todo";
        }
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{todo_id}/delete/users/{owner_id}")
    public String delete(@PathVariable("todo_id") Long todoId,
                        @PathVariable("owner_id") Long ownerId) {
        log.info("GET request for deleting a ToDo with ID {} from User with ID {}", todoId, ownerId);
        todoService.delete(todoId);
        log.debug("ToDo with ID {} deleted successfully.", todoId);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/all/users/{user_id}")
    public String getAll(@PathVariable("user_id") Long userId, Model model) {
        log.debug("GET request to get all ToDos of User with ID {}", userId);
        List<ToDo> todos = todoService.getByUserId(userId);
        model.addAttribute("todos", todos);
        model.addAttribute("user", userService.readById(userId));
        return "todos-user";
    }

    @GetMapping("/{id}/tasks")
    public String getTasks(@PathVariable("id") Long todoId, Model model) {
        log.debug("GET request to get all Tasks of ToDo with ID {}.", todoId);
        ToDo todo = todoService.readById(todoId);
        model.addAttribute("todo", todo);
        model.addAttribute("tasks", todo.getTasks());
        model.addAttribute("users", userService.getAll().stream()
                .filter(user -> !todo.getOwner().equals(user) && !todo.getCollaborators().contains(user))
                .collect(Collectors.toList()));
        return "todo-tasks";
    }

    @GetMapping("/{id}/add")
    public String addCollaborator(@PathVariable("id") Long todoId,
                                 @RequestParam("user_id") Long userId) {
        log.info("GET request to add User with ID {} as a collaborator of ToDo with ID {}", userId, todoId);
        todoService.addCollaborator(todoId, userId);
        log.debug("User with ID {} became a collaborator of ToDo with ID {}", userId, todoId);
        return "redirect:/todos/" + todoId + "/tasks";
    }

    @GetMapping("/{id}/remove")
    public String removeCollaborator(@PathVariable("id") Long todoId,
                                    @RequestParam("user_id") Long userId) {
        log.info("GET request to remove User with ID {} from collaborators of ToDo with ID {}", userId, todoId);
        todoService.removeCollaborator(todoId, userId);
        log.debug("User with ID {} stooped being a collaborator of ToDo with ID {}", userId, todoId);
        return "redirect:/todos/" + todoId + "/tasks";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("Entity not found: ", ex);
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }
}
