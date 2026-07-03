package com.softserve.itacademy.repository;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.TargetSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ToDoRepositoryTest {
    @Autowired
    ToDoRepository todoRepository;

    @Autowired
    TestEntityManager em;

    @Test
    void testGetByUserId() {
        // На жаль Instancio не дуже гарно працює з @Pattern :(
//        User arsen = Instancio.of(User.class)
//                .ignore(Select.field("id"))
//                .ignore((Select.field("myTodos")))
//                .ignore((Select.field("otherTodos")))
//                .set(Select.field("firstName"), "Arsen")
//                .create();
//        User tania = Instancio.of(User.class)
//                .ignore((Select.field("id")))
//                .ignore((Select.field("myTodos")))
//                .ignore((Select.field("otherTodos")))
//                .set(Select.field("firstName"), "Tania")
//                .create();

        User user1 = new User();
        user1.setFirstName("First");
        user1.setLastName("Last");
        user1.setEmail("test@mail.com");
        user1.setPassword("password123");
        user1.setRole(UserRole.USER);
        em.persist(user1);

        User user2 = new User();
        user2.setFirstName("First");
        user2.setLastName("Last");
        user2.setEmail("test2@mail.com");
        user2.setPassword("password1234");
        user2.setRole(UserRole.USER);
        em.persist(user2);

        ToDo todo1 = new ToDo();
        todo1.setTitle("Personal");
        todo1.setOwner(user1);
        todo1.getCollaborators().add(user2);
        em.persist(todo1);

        ToDo todo2 = Instancio.of(ToDo.class)
                .ignore((Select.field("id")))
                .ignore((Select.field("collaborators")))
                .ignore((Select.field("tasks")))
                .set(Select.field("owner"), user2)
                .create();

        em.persist(todo2);

        em.flush(); em.clear();

        List<ToDo> actual = todoRepository.getByUserId(user2.getId());

        assertEquals(2, actual.size());
        assertThat(actual).
                contains(todo1, todo2);
    }
}
