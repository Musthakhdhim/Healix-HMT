package com.hmt.healix.controller;

import com.hmt.healix.entity.*;
import com.hmt.healix.repository.AppointmentBookingRepository;
import com.hmt.healix.repository.DoctorSlotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/patient")
public class RazorpayWebhookController {


    private final AppointmentBookingRepository bookingRepository;
    private final DoctorSlotRepository doctorSlotRepository;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public RazorpayWebhookController(AppointmentBookingRepository bookingRepository, DoctorSlotRepository doctorSlotRepository) {
        this.bookingRepository = bookingRepository;
        this.doctorSlotRepository = doctorSlotRepository;
    }

    @PostMapping("/payment/webhook")
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String event = (String) payload.get("event");

            if ("payment.captured".equals(event)) {
                Map<String, Object> payloadObj = (Map<String, Object>) payload.get("payload");
                Map<String, Object> paymentWrapper = (Map<String, Object>) payloadObj.get("payment");
                Map<String, Object> paymentEntity = (Map<String, Object>) paymentWrapper.get("entity");

                String orderId = (String) paymentEntity.get("order_id");
                String paymentId = (String) paymentEntity.get("id");

                AppointmentBooking booking = bookingRepository.findByRazorpayOrderId(orderId)
                        .orElseThrow(() -> new RuntimeException("Booking not found for order " + orderId));

                DoctorSlot slot=booking.getSlot();
                slot.setStatus(SlotStatus.BOOKED);
                doctorSlotRepository.save(slot);

                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setRazorpayPaymentId(paymentId);
                bookingRepository.save(booking);
            }

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
