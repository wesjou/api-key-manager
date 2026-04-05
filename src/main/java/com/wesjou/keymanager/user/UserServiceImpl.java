package com.wesjou.keymanager.user;

import com.wesjou.keymanager.exception.EmailAlreadyExistsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new EmailAlreadyExistsException();

        User user = new User();
        user.setEmail(request.email());

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
