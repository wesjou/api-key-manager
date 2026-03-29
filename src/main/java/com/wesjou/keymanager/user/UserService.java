package com.wesjou.keymanager.user;

import java.util.List;

interface UserService {
    UserResponse createUser(CreateUserRequest request);
    List<User> getAllUser();
}
