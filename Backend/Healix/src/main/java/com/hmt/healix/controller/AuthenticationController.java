package com.hmt.healix.controller;


import com.hmt.healix.dtos.AuthenticationResponse;
import com.hmt.healix.dtos.LoginRequest;
import com.hmt.healix.dtos.RegisterRequest;
import com.hmt.healix.dtos.VerfiyUserDto;
import com.hmt.healix.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerfiyUserDto verfiyUserDto){
        try{
            authenticationService.verifyUser(verfiyUserDto);
            return ResponseEntity.ok("user verified successfully");
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resend(@RequestParam String email){
        try{
            authenticationService.resendVerificationCode(email);
            return ResponseEntity.ok("resend successfully");
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, String>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authenticationService.getCurrentUser(authHeader));
    }
}
