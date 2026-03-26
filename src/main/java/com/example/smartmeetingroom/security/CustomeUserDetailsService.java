package com.example.smartmeetingroom.security;

import com.example.smartmeetingroom.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class CustomeUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email.trim().toLowerCase()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with email " + email + " doesn't exist")
        );
        return new CustomUserDetails(user);
    }
}
