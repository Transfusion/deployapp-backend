package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.db.repositories.UserRepository;
import io.github.transfusion.deployapp.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(UUID id) {
        Optional<User> _user = userRepository.findById(id);
        if (_user.isEmpty()) throw new ResourceNotFoundException("User", "id", id);
        return _user.get();
    }
}
