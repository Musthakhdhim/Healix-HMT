package com.hmt.healix.Service;

import com.hmt.healix.Dtos.AuthenticationResponse;
import com.hmt.healix.Dtos.LoginRequest;
import com.hmt.healix.Dtos.RegisterRequest;
import com.hmt.healix.Entity.Role;
import com.hmt.healix.Entity.Users;
import com.hmt.healix.Mapper.UserMapper;
import com.hmt.healix.Repository.UserRepository;
import io.jsonwebtoken.Jwt;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UsernameNotFoundException("Email already exists");
        }

        if(userRepository.existsByUsername(request.getUsername())){
            throw new UsernameNotFoundException("Username already exists");
        }
        var user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.PATIENT);
        

        userRepository.save(user);
        var token=jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse login(LoginRequest request) {
        System.out.println("from login");
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        System.out.println("from logni");
        var user=userRepository.findByEmail(request.getEmail()).orElseThrow(()->{
            throw new UsernameNotFoundException("user not found");
        });
        var token=jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }
}
