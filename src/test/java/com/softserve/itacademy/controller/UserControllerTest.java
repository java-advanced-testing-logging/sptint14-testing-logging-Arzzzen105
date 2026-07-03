package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private ToDoService todoService;
    @MockBean
    private UserDtoConverter userConverter;

    @Test
    void getCreateShouldReturnView() throws Exception {
        mockMvc.perform(get("/users/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void postCreateShouldRedirect() throws Exception {
        User saved = new User();
        saved.setId(1L);
        when(userService.register(any(CreateUserDto.class))).thenReturn(saved);

        mockMvc.perform(post("/users/create")
                .param("firstName", "First")
                .param("lastName", "Last")
                .param("email", "mail@mail.mail")
                .param("password", "P@ssW0rd"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/all/users/1"));
    }

    @Test
    void postCreateShouldReturnFormWhenInvalid() throws Exception {
        mockMvc.perform(post("/users/create")
                        .param("firstName", "First")
                        .param("lastName", "")
                        .param("email", "mail@mail.mail")
                        .param("password", "P@ssW0rd"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"))
                .andExpect(model().hasErrors());
    }

    @Test
    void getReadShouldReturnViewWhenValid() throws Exception {
        User user = new User();
        user.setId(1L);
        when(userService.readById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1/read")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect((model().attributeExists("user")))
                .andExpect(model().attribute("user", user))
                .andExpect(view().name("user-info"));
    }

    @Test
    void getUpdateShouldReturnView() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("mail@mail.mail");
        user.setRole(UserRole.USER);
        when(userService.readById((anyLong()))).thenReturn(user);
        UserDto dto = UserDto.builder()
                .id(1L)
                .firstName("First")
                .lastName("Last")
                .email("mail@mail.mail")
                .role(UserRole.USER)
                .build();
        when(userService.update(any(CreateUserDto.class))).thenReturn(dto);

        mockMvc.perform(get("/users/1/update")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(view().name("update-user"));
    }

    @Test
    void postUpdateShouldRedirect() throws Exception {
        UserDto mockUserDto = new UserDto();
        when(userService.update(any(UpdateUserDto.class))).thenReturn(mockUserDto);

        mockMvc.perform(post("/users/1/update")
                        .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("firstName", "Arsen")
                        .param("lastName", "Smetaniak")
                        .param("email", "arsen@mail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/all"));

        verify(userService, times(1)).update(any(UpdateUserDto.class));
    }

    @Test
    void deleteShouldRedirect() throws Exception {
        mockMvc.perform(get("/users/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/all"));
    }
}
