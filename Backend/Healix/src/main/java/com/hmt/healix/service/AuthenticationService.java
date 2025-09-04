package com.hmt.healix.service;

import com.hmt.healix.dtos.AuthenticationResponse;
import com.hmt.healix.dtos.LoginRequest;
import com.hmt.healix.dtos.RegisterRequest;
import com.hmt.healix.dtos.VerfiyUserDto;
import com.hmt.healix.entity.Role;
import com.hmt.healix.entity.Users;
import com.hmt.healix.exception.*;
import com.hmt.healix.mapper.UserMapper;
import com.hmt.healix.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public ResponseEntity<?> register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())){
            log.error("Registration failed - Email already exists: {}", request.getEmail());
            throw new AlreadyExistsException("Email already exists");
        }

        if(userRepository.existsByUsername(request.getUsername())){
            log.error("Registration failed - Username already exists: {}", request.getUsername());
            throw new AlreadyExistsException("Username already exists");
        }

        var user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(request.getRole());
        user.setVerificationCode(generateVerficationCode());
        user.setVerificationCodeExpireAt(LocalDateTime.now().plusMinutes(1));
        user.setEnabled(false);
        user.setAdminAuthorised(false);

        log.debug("Generated verification code {} for user {}", user.getVerificationCode(), user.getEmail());

        sendVerificationEmail(user);
        userRepository.save(user);

        log.info("User {} registered successfully", request.getEmail());
        return ResponseEntity.ok("User registered successfully.");
    }

    public AuthenticationResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Login failed - user not found: {}", request.getEmail());
                    return new UsersNotFoundException("user not found");
                });

        if(!user.isAccountNonLocked()){
            log.error("Login failed - account locked for {}", request.getEmail());
            throw new AccountLockedException("your account has been locked by admin, please contact administrator");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            log.error("Login failed - invalid password for {}", request.getEmail());
            throw new BadCredentialsException("invalid password");
        }

        if (!user.isEnabled()) {
            log.error("Login failed - account not verified for {}", request.getEmail());
            throw new AccountNotVerifiedException("account is not yet verified, please verify your account");
        }

        if (user.getRole() == Role.DOCTOR && !user.isAdminAuthorised()) {
            log.error("Login failed - doctor {} not yet admin approved", request.getEmail());
            throw new AccountNotVerifiedException("Doctor account is pending admin approval");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var token = jwtService.generateToken(user);
        log.info("Login successful for {}", request.getEmail());
        return new AuthenticationResponse(token);
    }

    public void verifyUser(VerfiyUserDto dto) {
        log.info("Verifying user with email: {}", dto.getEmail());

        Optional<Users> optionalUsers = userRepository.findByEmail(dto.getEmail());
        if (optionalUsers.isPresent()) {
            Users user = optionalUsers.get();
            if (user.getVerificationCodeExpireAt().isBefore(LocalDateTime.now())) {
                log.error("Verification failed - code expired for {}", dto.getEmail());
                throw new VerficationCodeExpiredException("verification code has expired, try resending the code");
            }
            if (user.getVerificationCode().equals(dto.getVerificationCode())) {
                user.setEnabled(true);

                if (user.getRole() == Role.PATIENT) {
                    user.setAdminAuthorised(true);
                    log.info("Patient {} auto-approved after verification", dto.getEmail());
                }

                user.setVerificationCode(null);
                user.setVerificationCodeExpireAt(null);
                userRepository.save(user);
                log.info("User {} verified successfully", dto.getEmail());
            } else {
                log.error("Verification failed - wrong code for {}", dto.getEmail());
                throw new WrongVerificationCodeException("your verification code is incorrect, try again");
            }
        } else {
            log.error("Verification failed - user not found: {}", dto.getEmail());
            throw new UserNotFoundException("user not found");
        }
    }

    public void resendVerificationCode(String email){
        log.info("Resending verification code to {}", email);

        Optional<Users> optionalUsers=userRepository.findByEmail(email);
        if(optionalUsers.isPresent()){
            Users user=optionalUsers.get();

            if(user.isEnabled()){
                log.warn("Resend failed - user {} already verified", email);
                throw new AlreadyVerifiedException("user already verified");
            }

            user.setVerificationCode(generateVerficationCode());
            user.setVerificationCodeExpireAt(LocalDateTime.now().plusMinutes(15));
            log.debug("New verification code {} generated for {}", user.getVerificationCode(), email);

            sendVerificationEmail(user);
            userRepository.save(user);
            log.info("Verification code resent to {}", email);
        }
        else{
            log.error("Resend failed - user not found: {}", email);
            throw new UserNotFoundException("user not found");
        }
    }

    public void sendVerificationEmail(Users user){
        log.info("Sending verification email to {}", user.getEmail());

        String subject="Email Verification Code";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try{
            emailService.sendEmail(user.getEmail(),subject, htmlMessage);
            log.info("Verification email sent to {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}", user.getEmail(), e);
            throw new RuntimeException(e);
        }
    }

    private String generateVerficationCode(){
        Random random=new Random();
        int code=random.nextInt(900000)+100000;
        String generated = String.valueOf(code);
        log.debug("Generated verification code: {}", generated);
        return generated;
    }

    public Map<String, String> getCurrentUser(String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        log.debug("Extracted current user from token: email={}, role={}", email, role);

        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("role", role);
        return response;
    }
}
