package com.dmt.backend.security;

import com.dmt.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        log.debug("Loading user details username={}", username);

        return userRepository.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> {
                    log.warn("User details not found username={}", username);
                    return new UsernameNotFoundException("User not found");
                });
    }
}
