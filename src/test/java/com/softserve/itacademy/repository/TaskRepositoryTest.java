package com.softserve.itacademy.repository;

import com.softserve.itacademy.model.State;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.TaskPriority;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void findByTodoId_ShouldReturnTasks() {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("test@mail.com");
        user.setPassword("password123");
        user.setRole(UserRole.USER);
        entityManager.persist(user);

        ToDo todo = new ToDo();
        todo.setTitle("ToDo1");
        todo.setOwner(user);
        todo.setCreatedAt(LocalDateTime.now());
        entityManager.persist(todo);

        State state = new State();
        state.setName("State1");
        entityManager.persist(state);

        Task task1 = new Task();
        task1.setName("Task1");
        task1.setPriority(TaskPriority.LOW);
        task1.setTodo(todo);
        task1.setState(state);
        entityManager.persist(task1);

        Task task2 = new Task();
        task2.setName("Task2");
        task2.setPriority(TaskPriority.HIGH);
        task2.setTodo(todo);
        task2.setState(state);
        entityManager.persist(task2);

        entityManager.flush();

        List<Task> tasks = taskRepository.findByTodoId(todo.getId());

        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Task1")));
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Task2")));
    }

    @Test
    void existsByNameAndTodoId_ShouldWork() {
        // Setup similar to above...
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("exists@mail.com");
        user.setPassword("password123");
        user.setRole(UserRole.USER);
        entityManager.persist(user);

        ToDo todo = new ToDo();
        todo.setTitle("ExistsToDo");
        todo.setOwner(user);
        todo.setCreatedAt(LocalDateTime.now());
        entityManager.persist(todo);

        State state = new State();
        state.setName("ExistsState");
        entityManager.persist(state);

        Task task = new Task();
        task.setName("ExistsTask");
        task.setPriority(TaskPriority.LOW);
        task.setTodo(todo);
        task.setState(state);
        entityManager.persist(task);

        entityManager.flush();

        assertTrue(taskRepository.existsByNameAndTodoId("ExistsTask", todo.getId()));
        assertFalse(taskRepository.existsByNameAndTodoId("NonExists", todo.getId()));
    }
}
