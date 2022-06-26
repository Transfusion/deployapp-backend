package io.github.transfusion.deployapp.services;

import io.github.transfusion.deployapp.db.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;
}
