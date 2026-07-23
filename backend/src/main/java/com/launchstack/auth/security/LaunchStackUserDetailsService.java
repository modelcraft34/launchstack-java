package com.launchstack.auth.security;

import com.launchstack.role.Role;
import com.launchstack.user.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LaunchStackUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public LaunchStackUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .disabled(!user.isEnabled())
                        .accountLocked(!user.isAccountNonLocked())
                        .authorities(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
