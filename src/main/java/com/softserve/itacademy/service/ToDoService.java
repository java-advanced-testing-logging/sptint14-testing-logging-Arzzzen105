package com.softserve.itacademy.service;

import com.softserve.itacademy.config.exception.NullEntityReferenceException;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.ToDoRepository;
import com.softserve.itacademy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToDoService {
    private final ToDoRepository todoRepository;
    private final UserRepository userRepository;

    @Transactional
    public ToDo create(ToDo todo) {
        if (todo != null) {
            log.info("Creating a new ToDo with ID {}", todo.getId());
            if (todoRepository.existsByTitle(todo.getTitle())) {
                log.warn("ToDo with title {} already exists.", todo.getTitle());
                throw new IllegalArgumentException("ToDo with title '" + todo.getTitle() + "' already exists");
            }
            return todoRepository.save(todo);
        }
        log.error("Attempted to create a null ToDo!");
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Transactional(readOnly = true)
    public ToDo readById(long id) {
        log.debug("Reading a ToDo with ID {}", id);
        return todoRepository.findById(id).orElseThrow(
                () -> {
                    log.error("ToDo with ID {} not found!", id);
                    return new EntityNotFoundException("ToDo with id " + id + " not found");
                });
    }

    @Transactional
    public ToDo update(ToDo todo) {
        if (todo != null) {
            log.info("Updating a ToDo with ID {}.", todo.getId());
            if (todoRepository.existsByTitleAndIdNot(todo.getTitle(), todo.getId())) {
                log.warn("Tried to create already existing ToDo.");
                throw new IllegalArgumentException("ToDo with title '" + todo.getTitle() + "' already exists");
            }
            readById(todo.getId());
            log.info("ToDo with ID {} successfully saved.", todo.getId());
            return todoRepository.save(todo);
        }
        log.error("Attempted to update a null ToDo!");
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Transactional
    public void delete(long id) {
        log.info("Deleting a ToDo with ID {}", id);
        ToDo todo = readById(id);
        todoRepository.delete(todo);
        log.debug("ToDo with ID {} was successfully deleted.", id);
    }

    @Transactional(readOnly = true)
    public List<ToDo> getAll() {
        log.debug("Fetching all ToDos");
        return todoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ToDo> getByUserId(long userId) {
        log.debug("Fetching a ToDo by owner/collaborator ID {}", userId);
        return todoRepository.getByUserId(userId);
    }

    @Transactional
    public void addCollaborator(long todoId, long userId) {
        log.info("Adding a User with ID {} as a collaborator for ToDo with ID {}", userId, todoId);
        ToDo todo = readById(todoId);
        User collaborator = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Collaborator with ID {} not found!", userId);
                    return new EntityNotFoundException("User with id " + userId + " not found");
                });
        todo.getCollaborators().add(collaborator);
        update(todo);
        log.debug("User with ID {} was added to ToDo with ID {} as a collaborator", userId, todoId);
    }

    @Transactional
    public void removeCollaborator(long todoId, long userId) {
        log.info("Removing User with ID {} from collaborators of ToDo with ID {}", userId, todoId);
        ToDo todo = readById(todoId);
        User collaborator = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Collaborator with ID {} not found!", userId);
                    return new EntityNotFoundException("User with id " + userId + " not found");
                });
        todo.getCollaborators().remove(collaborator);
        update(todo);
        log.debug("User with ID {} was removed from ToDo's collaborators with ID {}", userId, todoId);
    }
}
