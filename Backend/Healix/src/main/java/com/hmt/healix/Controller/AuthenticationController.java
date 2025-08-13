package com.hmt.healix.Controller;


import com.hmt.healix.Dtos.AuthenticationResponse;
import com.hmt.healix.Dtos.LoginRequest;
import com.hmt.healix.Dtos.RegisterRequest;
import com.hmt.healix.Dtos.VerfiyUserDto;
import com.hmt.healix.Service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request){
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
}
