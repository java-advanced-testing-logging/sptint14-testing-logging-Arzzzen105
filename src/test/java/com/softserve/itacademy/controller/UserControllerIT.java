package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.repository.UserRepository;
import com.softserve.itacademy.service.UserService;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserController userController;

    @Test
    void testUpdate() throws Exception {
        User user = new User();
        user.setFirstName("Arsen");
        user.setLastName("Smetaniak");
        user.setEmail("arsen@mail.com");
        user.setPassword("P@ssW0rd");
        user.setRole(UserRole.USER);

        user = userRepository.save(user);

        mockMvc.perform(post("/users/1/update")
                .param("firstName", "Arseniy")
                .param("lastName", "Smetaniak")
                .param("email", "arsen@gmail.com")
                .param("userId", "1")
                .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/all"));

        Optional<User> updated = userRepository.findById(1L);

        assertThat(updated)
                .isNotEmpty()
                .contains(user);
        assertEquals("Arseniy", updated.get().getFirstName());

    }
}
