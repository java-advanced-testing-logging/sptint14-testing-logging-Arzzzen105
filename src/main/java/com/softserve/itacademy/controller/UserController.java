package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.service.UserService;
import jakarta.servlet.http.HttpSession;
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

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserDtoConverter userDtoConverter;

    @GetMapping("/create")
    public String create(Model model) {
        log.debug("GET request for a User creating form.");
        model.addAttribute("user", new CreateUserDto());
        return "create-user";
    }

    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") CreateUserDto userDto,
                        BindingResult result) {
        log.info("POST request to create a new User with ID {}", userDto.getId());
        if (result.hasErrors()) {
            log.warn("Validation failed for User creation: {}", result.getAllErrors());
            return "create-user";
        }
        try {
            User user = userService.register(userDto);
            log.info("A new User was registered with ID {}", user.getId());
            return "redirect:/todos/all/users/" + user.getId();
        } catch (IllegalArgumentException e) {
            log.error("Error creating a User: ", e);
            result.rejectValue("email", "error.user", e.getMessage());
            return "create-user";
        }
    }

    @GetMapping("/{id}/read")
    public String read(@PathVariable("id") Long id, Model model) {
        log.debug("GET request for User with ID {} info", id);
        User user = userService.readById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    @GetMapping("/{id}/update")
    public String update(@PathVariable("id") Long id, Model model) {
        log.debug("GET request for User updating form from with ID {}", id);
        User user = userService.readById(id);
        UpdateUserDto userDto = new UpdateUserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        
        model.addAttribute("user", userDto);
        model.addAttribute("roles", UserRole.values());
        return "update-user";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Long id,
                        @Validated @ModelAttribute("user") UpdateUserDto userDto,
                        BindingResult result,
                        Model model) {
        log.info("POST request to update a User with ID {}", userDto.getId());
        if (result.hasErrors()) {
            log.warn("Validation failed for User updating: {}", result.getAllErrors());
            model.addAttribute("roles", UserRole.values());
            return "update-user";
        }
        userDto.setId(id);
        userService.update(userDto);
        log.info("User with ID {} was successfully updated!", userDto.getId());
        return "redirect:/users/all";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        log.info("GET request to delete a User with ID {}", id);
        userService.delete(id);
        log.debug("User with ID {} was deleted.", id);
        return "redirect:/users/all";
    }

    @GetMapping("/all")
    public String getAll(Model model) {
        log.debug("Fetching all Users");
        model.addAttribute("users", userService.getAll());
        return "users-list";
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
