package com.vaultix.service;

import com.vaultix.dto.*;

public interface AuthService {

    RegisterResponse       register(RegisterRequest request);

    LoginResponse          login(LoginRequest request);

    RefreshTokenResponse   refreshToken(RefreshTokenRequest request);

    void                   logout(LogoutRequest request);

    MeResponse             getCurrentUser(String email);
}