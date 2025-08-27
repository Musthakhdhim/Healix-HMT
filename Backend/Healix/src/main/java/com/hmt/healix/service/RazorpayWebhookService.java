package com.hmt.healix.service;

import com.hmt.healix.entity.AppointmentBooking;
import com.hmt.healix.entity.BookingStatus;
import com.hmt.healix.entity.DoctorSlot;
import com.hmt.healix.entity.SlotStatus;
import com.hmt.healix.repository.AppointmentBookingRepository;
import com.hmt.healix.repository.DoctorSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RazorpayWebhookService {

    private final AppointmentBookingRepository bookingRepository;
    private final DoctorSlotRepository doctorSlotRepository;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

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
