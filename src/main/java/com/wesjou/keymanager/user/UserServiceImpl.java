package com.wesjou.keymanager.user;

import com.wesjou.keymanager.exception.EmailAlreadyExistsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new EmailAlreadyExistsException();

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setRole(Role.USER);

        userRepository.save(user);
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Override
    public List<UserResponse> getAllUser() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail()
                )).toList();
    }

}
