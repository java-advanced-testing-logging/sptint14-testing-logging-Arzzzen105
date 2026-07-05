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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserDtoConverter userDtoConverter;

    @Transactional
    public User register(CreateUserDto createUserDto) {
        log.info("Registering a new User with first name: {} and email: {}", createUserDto.getFirstName(), createUserDto.getEmail());
        createUserDto.setRole(UserRole.USER);
        User user = userDtoConverter.convertToUser(createUserDto);
        user.setPassword("{noop}" + user.getPassword());
        return create(user);
    }

    @Transactional
    public User create(User user) {
        if (user != null) {
            log.info("Creating a new User with first name: {} and email: {}", user.getFirstName(), user.getEmail());
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                log.warn("Attempted to create an already existing user! ID: {}", user.getId());
                throw new IllegalArgumentException("User with email '" + user.getEmail() + "' already exists");
            }
            log.debug("User saved with ID {}", user.getId());
            return userRepository.save(user);
        }
        log.error("Attempted to create a null User!");
        throw new NullEntityReferenceException("User cannot be 'null'");
    }

    @Transactional(readOnly = true)
    public User readById(long id) {
        log.debug("Reading a User with ID {}", id);
        return userRepository.findById(id).orElseThrow(
                () ->  {
                    log.error("User with ID {} not found!", id);
                    return new EntityNotFoundException("User with id " + id + " not found");
                });
    }

    @Transactional
    public UserDto update(UpdateUserDto updateUserDto) {
        log.info("Updating User with ID {}", updateUserDto.getId());
        User user = userRepository.findById(updateUserDto.getId()).orElseThrow(
                () -> new EntityNotFoundException("User with id " + updateUserDto.getId() + " not found"));
        if (updateUserDto.getRole() != null && user.getRole() == UserRole.ADMIN) {
            log.debug("Update User role with ID {}", user.getId());
            user.setRole(updateUserDto.getRole());
            updateUserDto.setRole(null); // prevent double setting in converter if we want to be strict, 
                                         // but fillFields already has a null check now.
        } else {
            log.warn("User {} with user privileges tried to change role!", user.getId());
            updateUserDto.setRole(null); // don't allow non-admin to change role, or admin to change to null
        }
        userDtoConverter.fillFields(user, updateUserDto);
        userRepository.save(user);
        log.info("User {} updated successfully", user.getId());
        return userDtoConverter.toDto(user);
    }

    @Transactional
    public void delete(long id) {
        log.info("Deleting User with ID {}", id);
        User user = readById(id);
        userRepository.delete(user);
        log.debug("User {} deleted successfully", id);
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        log.debug("Fetching all Users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding a User with username {}", username);
        return userRepository.findByEmail(username);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findById(long id) {
        log.debug("Finding a User by ID {}", id);
        return userRepository.findById(id).map(userDtoConverter::toDto);
    }

    @Transactional(readOnly = true)
    public UserDto findByIdThrowing(long id) {
        log.debug("Finding a User by ID {}", id);
        return userRepository.findById(id).map(userDtoConverter::toDto).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        log.debug("Fetching all Users");
        return userRepository.findAll().stream().map(userDtoConverter::toDto).toList();
    }
}
