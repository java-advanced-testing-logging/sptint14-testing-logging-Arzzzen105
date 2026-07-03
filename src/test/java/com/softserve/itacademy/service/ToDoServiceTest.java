package com.softserve.itacademy.service;

import com.softserve.itacademy.config.exception.NullEntityReferenceException;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.ToDoRepository;
import com.softserve.itacademy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToDoServiceTest {

    @Mock
    private ToDoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ToDoService todoService;

    private ToDo todo;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(2L);
        user.setFirstName("Tania");
        user.setEmail("tania@mail.com");

        todo = new ToDo();
        todo.setId(1L);
        todo.setTitle("Weekend Plans");
        todo.setCollaborators(new HashSet<>());
    }

    @Test
    void createShouldReturnSavedWhenValid() {
        when(todoRepository.existsByTitle(todo.getTitle())).thenReturn(false);
        when(todoRepository.save(todo)).thenReturn(todo);

        ToDo saved = todoService.create(todo);

        assertNotNull(saved);
        assertEquals(todo.getTitle(), saved.getTitle());
        verify(todoRepository, times(1)).save(todo);
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenTitleExists() {
        when(todoRepository.existsByTitle(todo.getTitle())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> todoService.create(todo));
        verify(todoRepository, never()).save(any());
    }

    @Test
    void createShouldThrowNullEntityReferenceExceptionWhenToDoIsNull() {
        assertThrows(NullEntityReferenceException.class, () -> todoService.create(null));
    }

    @Test
    void readByIdShouldReturnToDoWhenExists() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        ToDo found = todoService.readById(1L);

        assertEquals(todo, found);
    }

    @Test
    void readByIdShouldThrowEntityNotFoundExceptionWhenNotFound() {
        when(todoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> todoService.readById(1L));
    }

    @Test
    void updateShouldReturnSavedWhenNoTitleCollision() {
        when(todoRepository.existsByTitleAndIdNot(todo.getTitle(), todo.getId())).thenReturn(false);
        when(todoRepository.findById(todo.getId())).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        ToDo updated = todoService.update(todo);

        assertEquals(todo, updated);
        verify(todoRepository).save(todo);
    }

    @Test
    void updateShouldThrowIllegalArgumentExceptionWhenTitleCollisionExists() {
        when(todoRepository.existsByTitleAndIdNot(todo.getTitle(), todo.getId())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> todoService.update(todo));
        verify(todoRepository, never()).save(any());
    }

    @Test
    void updateShouldThrowNullEntityReferenceExceptionWhenToDoIsNull() {
        assertThrows(NullEntityReferenceException.class, () -> todoService.update(null));
    }

    @Test
    void deleteShouldCallRepositoryDeleteWhenToDoExists() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));

        todoService.delete(1L);

        verify(todoRepository, times(1)).delete(todo);
    }

    @Test
    void addCollaboratorShouldAddUserAndCallUpdate() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.existsByTitleAndIdNot(todo.getTitle(), todo.getId())).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(todoRepository.save(todo)).thenReturn(todo);

        todoService.addCollaborator(1L, 2L);

        assertThat(todo.getCollaborators()).contains(user);
        verify(todoRepository).save(todo);
    }

    @Test
    void addCollaboratorShouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> todoService.addCollaborator(1L, 2L));
        verify(todoRepository, never()).save(any());
    }

    @Test
    void removeCollaboratorShouldRemoveUserAndCallUpdate() {
        todo.getCollaborators().add(user); // спочатку додаємо

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.existsByTitleAndIdNot(todo.getTitle(), todo.getId())).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(todoRepository.save(todo)).thenReturn(todo);

        todoService.removeCollaborator(1L, 2L);

        assertThat(todo.getCollaborators()).doesNotContain(user);
        verify(todoRepository).save(todo);
    }

    @Test
    void getAllShouldReturnListOfToDos() {
        when(todoRepository.findAll()).thenReturn(List.of(todo));

        List<ToDo> result = todoService.getAll();

        assertThat(result).hasSize(1).contains(todo);
    }

    @Test
    void getByUserIdShouldReturnUserSpecificToDos() {
        when(todoRepository.getByUserId(2L)).thenReturn(List.of(todo));

        List<ToDo> result = todoService.getByUserId(2L);

        assertThat(result).hasSize(1).contains(todo);
    }
}