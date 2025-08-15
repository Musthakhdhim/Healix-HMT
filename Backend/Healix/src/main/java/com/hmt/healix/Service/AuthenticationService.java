package com.hmt.healix.Service;

import com.hmt.healix.Dtos.AuthenticationResponse;
import com.hmt.healix.Dtos.LoginRequest;
import com.hmt.healix.Dtos.RegisterRequest;
import com.hmt.healix.Dtos.VerfiyUserDto;
import com.hmt.healix.Entity.Role;
import com.hmt.healix.Entity.Users;
import com.hmt.healix.Exception.*;
import com.hmt.healix.Mapper.UserMapper;
import com.hmt.healix.Repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public ResponseEntity<?> register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AlreadyExistsException("Email already exists");
//            return ResponseEntity.badRequest().body(
//                    Map.of("email","email already registered")
//            );
        }

        if(userRepository.existsByUsername(request.getUsername())){
            throw new AlreadyExistsException("Username already exists");
        }
        var user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(request.getRole());
        user.setVerificationCode(generateVerficationCode());
        user.setVerificationCodeExpireAt(LocalDateTime.now().plusMinutes(1));
        user.setEnabled(false);
        user.setAdminAuthorised(false);
        sendVerificationEmail(user);

        userRepository.save(user);
//        var token=jwtService.generateToken(user);
        return ResponseEntity.ok(user);
    }

    public AuthenticationResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsersNotFoundException("user not found"));

        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("account is not yet verified, please verify your account");
        }

        if (user.getRole() == Role.DOCTOR && !user.isAdminAuthorised()) {
            throw new AccountNotVerifiedException("Doctor account is pending admin approval");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }

    public void verifyUser(VerfiyUserDto dto) {
        Optional<Users> optionalUsers = userRepository.findByEmail(dto.getEmail());
        if (optionalUsers.isPresent()) {
            Users user = optionalUsers.get();
            if (user.getVerificationCodeExpireAt().isBefore(LocalDateTime.now())) {
                throw new VerficationCodeExpiredException("verification code has expired, try resending the code");
            }
            if (user.getVerificationCode().equals(dto.getVerificationCode())) {
                user.setEnabled(true);

                // For patients → allow login directly
                // For doctors → still require admin approval
                if (user.getRole() == Role.PATIENT) {
                    user.setAdminAuthorised(true);
                }

                user.setVerificationCode(null);
                user.setVerificationCodeExpireAt(null);
                userRepository.save(user);
            } else {
                throw new WrongVerificationCodeException("your verification code is incorrect, try again");
            }
        } else {
            throw new UserNotFoundException("user not found");
        }
    }

    public void resendVerificationCode(String email){
        Optional<Users> optionalUsers=userRepository.findByEmail(email);
        System.out.println("resending verification code");
        if(optionalUsers.isPresent()){
            Users user=optionalUsers.get();
            System.out.println(user.toString());
            if(user.isEnabled()){
                System.out.println("user already verified");
                throw new AlreadyVerifiedException("user already verified");
            }
            user.setVerificationCode(generateVerficationCode());
            user.setVerificationCodeExpireAt(LocalDateTime.now().plusMinutes(15));
            sendVerificationEmail(user);
            System.out.println("user saved");
            userRepository.save(user);
        }
        else{
            System.out.println("user not found");
            throw new UserNotFoundException("user not found");
        }
    }

    public void sendVerificationEmail(Users user){
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
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    private String generateVerficationCode(){
        Random random=new Random();
        int code=random.nextInt(900000)+100000;
        return String.valueOf(code);
    }
}
