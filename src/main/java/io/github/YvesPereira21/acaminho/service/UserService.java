package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.exception.ObjectAlreadyExistsException;
import io.github.YvesPereira21.acaminho.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    protected void verifyUserAlreadyExists(String email) {
        boolean userAlreadyExists = userRepository.existsByEmail(email);
        if (userAlreadyExists) {
            throw new ObjectAlreadyExistsException("Email ou senha inválidos");
        }
    }
}
