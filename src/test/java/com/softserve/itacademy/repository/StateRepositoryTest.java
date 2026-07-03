package com.softserve.itacademy.repository;

import com.softserve.itacademy.model.State;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StateRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StateRepository stateRepository;

    @Test
    void findByName_ShouldReturnState_WhenExists() {
        State state = new State();
        state.setName("TestState");
        entityManager.persist(state);
        entityManager.flush();

        Optional<State> found = stateRepository.findByName("TestState");

        assertTrue(found.isPresent());
        assertEquals("TestState", found.get().getName());
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenExists() {
        State state = new State();
        state.setName("Exists");
        entityManager.persist(state);
        entityManager.flush();

        assertTrue(stateRepository.existsByName("Exists"));
        assertFalse(stateRepository.existsByName("NotExists"));
    }

    @Test
    void existsByNameAndIdNot_ShouldWorkCorrectly() {
        State state1 = new State();
        state1.setName("Name1");
        entityManager.persist(state1);

        State state2 = new State();
        state2.setName("Name2");
        entityManager.persist(state2);
        entityManager.flush();

        // Check for Name1, but ignore state1's ID. Should be false.
        assertFalse(stateRepository.existsByNameAndIdNot("Name1", state1.getId()));
        
        // Check for Name2, but ignore state1's ID. Should be true because Name2 exists and ID is different.
        assertTrue(stateRepository.existsByNameAndIdNot("Name2", state1.getId()));
    }

    @Test
    void findAllByOrderByIdAsc_ShouldReturnSortedStates() {
        State state2 = new State();
        state2.setName("B");
        entityManager.persist(state2);

        State state1 = new State();
        state1.setName("A");
        entityManager.persist(state1);
        entityManager.flush();

        List<State> states = stateRepository.findAllByOrderByIdAsc();
        
        assertTrue(states.indexOf(state2) > states.indexOf(state1) || states.get(0).getId() < states.get(1).getId());
        // Since IDs are generated, we just check they are in list and order is by ID
        for (int i = 0; i < states.size() - 1; i++) {
            assertTrue(states.get(i).getId() < states.get(i+1).getId());
        }
    }
}
