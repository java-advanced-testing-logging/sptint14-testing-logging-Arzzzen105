package com.softserve.itacademy.service;

import com.softserve.itacademy.config.exception.NullEntityReferenceException;
import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.dto.userDto.UserDtoConverter;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDtoConverter userDtoConverter;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("mail@mail.mail");
        user.setRole(UserRole.USER);
        user.setPassword("P@ssW0rd");
    }

    @Test
    void registerShouldSetRoleUserAndAddNoopToPassword() {
        CreateUserDto createDto = new CreateUserDto();
        createDto.setPassword("rawPassword");

        when(userDtoConverter.convertToUser(createDto)).thenReturn(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User registered = userService.register(createDto);

        assertEquals(UserRole.USER, createDto.getRole());
        assertEquals("{noop}P@ssW0rd", user.getPassword());
        assertEquals(user, registered);
    }

    @Test
    void createShouldReturnSavedWhenValid() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.create(user);

        assertEquals(user, saved);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenEmailExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.create(user));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createShouldThrowNullEntityReferenceExceptionWhenUserIsNull() {
        assertThrows(NullEntityReferenceException.class, () -> userService.create(null));
    }

    @Test
    void readByIdShouldReturnUserWhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.readById(1L);

        assertEquals(user, found);
    }

    @Test
    void readByIdShouldThrowEntityNotFoundExceptionWhenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.readById(1L));
    }


    @Test
    void updateShouldAllowAdminToChangeRole() {
        user.setRole(UserRole.ADMIN);
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setId(1L);
        updateDto.setRole(UserRole.USER);

        UserDto expectedDto = new UserDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userDtoConverter.toDto(user)).thenReturn(expectedDto);

        UserDto result = userService.update(updateDto);

        assertEquals(UserRole.USER, user.getRole());
        assertNull(updateDto.getRole());
        assertEquals(expectedDto, result);
        verify(userRepository).save(user);
    }

    @Test
    void updateShouldNotAllowNonAdminToChangeRole() {
        user.setRole(UserRole.USER);
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setId(1L);
        updateDto.setRole(UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userDtoConverter.toDto(user)).thenReturn(new UserDto());

        userService.update(updateDto);

        assertEquals(UserRole.USER, user.getRole());
        assertNull(updateDto.getRole());
    }

    @Test
    void deleteShouldCallRepositoryDeleteWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void getAllShouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = userService.getAll();

        assertThat(users).hasSize(1).contains(user);
    }

    @Test
    void findByUsernameShouldReturnOptionalUser() {
        when(userRepository.findByEmail("mail@mail.mail")).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByUsername("mail@mail.mail");

        assertTrue(found.isPresent());
        assertEquals(user, found.get());
    }

    @Test
    void findByIdThrowingShouldThrowWhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findByIdThrowing(1L));
    }
}