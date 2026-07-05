package com.softserve.itacademy.repository;

import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void findByEmailSuccess() {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("mail@mail.mail");
        user.setRole(UserRole.USER);
        user.setPassword("P@ssW0rd!");

        em.persist(user);

        em.flush(); em.clear();

        Optional<User> found = userRepository.findByEmail("mail@mail.mail");

        assertThat(found).isNotEmpty();
        assertEquals(user, found.get());
    }

    @Test
    void findByEmailEmpty() {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("mail@mail.mail");
        user.setRole(UserRole.USER);
        user.setPassword("P@ssW0rd!");

        em.persist(user);

        em.flush(); em.clear();

        Optional<User> found = userRepository.findByEmail("hello@world.net");

        assertThat(found).isEmpty();
    }
}
