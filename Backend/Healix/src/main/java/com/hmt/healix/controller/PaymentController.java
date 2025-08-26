package com.hmt.healix.controller;


import com.hmt.healix.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/patient")
public class PaymentController {

    private PaymentService paymentService;

    @PostMapping("/appointment/payment/{appointmentId}")
    public ResponseEntity<?> createOrder(@PathVariable Long appointmentId) {
        return paymentService.payForAppointment(appointmentId);
    }
}
