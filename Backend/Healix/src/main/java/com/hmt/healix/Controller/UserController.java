package com.hmt.healix.Controller;


import com.hmt.healix.Service.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
@AllArgsConstructor
public class UserController {

    @GetMapping
    public String Hello() {
        return "Hello";
    }

    @Autowired
    private EmailService emailService;

//    @GetMapping("/send-test-email")
//    public String sendTestEmail() {
//        emailService.sendEmail(
//                "pmusthakhdhim@gmail.com", // change to your email
//                "Test Email from Spring Boot",
//                "Hello! This is a test email from Spring Boot via Gmail SMTP."
//        );
//        return "Email sent!";
//    }
}
