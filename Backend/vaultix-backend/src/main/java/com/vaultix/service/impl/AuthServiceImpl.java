
package com.vaultix.service.impl;


import com.vaultix.dto.LoginRequest;
import com.vaultix.dto.LoginResponse;
import com.vaultix.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.vaultix.dto.RegisterRequest;
import com.vaultix.dto.RegisterResponse;
import com.vaultix.entity.User;
import com.vaultix.service.AuthService;
import com.vaultix.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserService userService,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        User savedUser = userService.registerUser(user, request.getPassword());

        return RegisterResponse.fromEntity(savedUser);
    }
    @Override
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateAccessToken(request.getEmail());

        return new LoginResponse(token);
    }
}