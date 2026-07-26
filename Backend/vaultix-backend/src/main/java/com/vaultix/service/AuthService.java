package com.vaultix.service;

import com.vaultix.dto.LoginRequest;
import com.vaultix.dto.LoginResponse;
import com.vaultix.dto.RegisterRequest;
import com.vaultix.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

}