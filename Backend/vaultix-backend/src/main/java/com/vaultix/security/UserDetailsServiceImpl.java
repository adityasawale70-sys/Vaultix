package com.vaultix.security;

import com.vaultix.entity.AccountStatus;
import com.vaultix.entity.User;
import com.vaultix.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        org.springframework.security.core.userdetails.User.UserBuilder builder =
                org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles("USER");

        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            builder.accountLocked(true);
        }

        if (user.getAccountStatus() == AccountStatus.DELETED || user.getAccountStatus() == AccountStatus.SUSPENDED) {
            builder.disabled(true);
        }

        return builder.build();
    }
}
