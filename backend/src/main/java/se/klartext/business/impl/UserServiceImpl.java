package se.klartext.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import se.klartext.business.api.UserService;
import se.klartext.domain.model.user.UserRepository;
import se.klartext.lib.exception.AccountAlreadyExistsException;
import se.klartext.domain.model.user.AuthToken;
import se.klartext.domain.model.user.User;
import se.klartext.security.api.AuthenticationService;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;

    private final AuthenticationService authService;


    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AuthenticationService authService) {
        this.userRepo = userRepository;
        this.authService = authService;
    }

    @Override
    public User register(User user) {
        userRepo.findByEmail(user.getEmail())
                .ifPresent(u -> { throw new AccountAlreadyExistsException("Email already exists"); });

        User newUser = User.builder()
                .email(user.getEmail())
                .password(encoder.encode(user.getPassword()))
                .name(user.getName())
                .build();

        return userRepo.save(newUser);
    }

    @Override
    public Optional<AuthToken> auth(User user) throws AuthenticationException {
        return authService.authenticate(user.getEmail(),user.getPassword());
    }

    @Override
    public Optional<User> profile(Long userId) {
        return userRepo.findOne(userId);
    }
}
