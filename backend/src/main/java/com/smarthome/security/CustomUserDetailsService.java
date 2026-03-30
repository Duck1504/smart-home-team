package com.smarthome.security;

import com.smarthome.entity.User;
import com.smarthome.entity.UserRole;
import com.smarthome.repository.UserRepository;
import java.util.Arrays;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    GrantedAuthority authority = new SimpleGrantedAuthority(
      user.getRole() == UserRole.ADMIN ? "ROLE_ADMIN" : "ROLE_USER"
    );

    return new org.springframework.security.core.userdetails.User(
      user.getUsername(),
      user.getPasswordHash(),
      Arrays.asList(authority)
    );
  }
}

