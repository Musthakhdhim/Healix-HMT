package com.hmt.healix.controller;

import com.hmt.healix.entity.*;
import com.hmt.healix.repository.AppointmentBookingRepository;
import com.hmt.healix.repository.DoctorSlotRepository;
import com.hmt.healix.service.RazorpayWebhookService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/patient")
@AllArgsConstructor
public class RazorpayWebhookController {

    private final RazorpayWebhookService razorpayWebhookService;


    @PostMapping("/payment/webhook")
    public ResponseEntity<?> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(razorpayWebhookService.handlePaymentWebhook(payload));
    }

}
