package com.vaultix.service;

import com.vaultix.entity.User;

public interface UserService {

    User registerUser(User user, String rawPassword);

    User findUserByEmail(String email);

    /** Called after a successful login to update lastLoginAt and reset failed attempts. */
    void recordSuccessfulLogin(String email);
}
