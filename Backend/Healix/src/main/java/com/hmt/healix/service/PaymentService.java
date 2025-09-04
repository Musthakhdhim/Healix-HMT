package com.hmt.healix.service;

import com.hmt.healix.entity.AppointmentBooking;
import com.hmt.healix.exception.UsersNotFoundException;
import com.hmt.healix.repository.AppointmentBookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final AppointmentBookingRepository bookingRepository;

    public ResponseEntity<?> payForAppointment(Long appointmentId) {
        log.info("Initiating payment for appointmentId: {}", appointmentId);
        try {
            AppointmentBooking booking = bookingRepository.findById(appointmentId)
                    .orElseThrow(() -> {
                        log.error("Appointment not found for id: {}", appointmentId);
                        return new UsersNotFoundException("Appointment not found");
                    });

            int amount = booking.getDoctor().getConsultingFee();
            log.debug("Consulting fee for doctor (appointmentId: {}): {}", appointmentId, amount);

            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            log.info("Razorpay client initialized successfully");

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount * 100);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "appt_" + appointmentId);

            Map<String, String> notes = new HashMap<>();
            notes.put("purpose", "APPOINTMENT_BOOKING");
            notes.put("appointmentId", String.valueOf(appointmentId));
            notes.put("patientId", String.valueOf(booking.getPatient().getPatientId()));
            orderRequest.put("notes", notes);

            log.debug("Creating Razorpay order with request: {}", orderRequest);

            Order order = razorpay.orders.create(orderRequest);
            log.info("Razorpay order created successfully with id: {}", Optional.ofNullable(order.get("id")));

            booking.setRazorpayOrderId(order.get("id"));
            bookingRepository.save(booking);
            log.info("Saved Razorpay orderId in booking record for appointmentId: {}", appointmentId);

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", order.get("id"));
            resp.put("amount", order.get("amount"));
            resp.put("currency", order.get("currency"));

            log.info("Returning payment response for appointmentId: {}", appointmentId);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            log.error("Error while creating Razorpay order for appointmentId: {} - {}", appointmentId, ex.getMessage(), ex);
            return ResponseEntity.badRequest().body("Error creating order: " + ex.getMessage());
        }
    }
}
