package com.hmt.healix.service;

import com.hmt.healix.entity.AppointmentBooking;
import com.hmt.healix.exception.UsersNotFoundException;
import com.hmt.healix.repository.AppointmentBookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key.id}") private String razorpayKeyId;
    @Value("${razorpay.key.secret}") private String razorpayKeySecret;

    private final AppointmentBookingRepository bookingRepository;

    public ResponseEntity<?> payForAppointment(Long appointmentId)  {
        try {
            AppointmentBooking booking = bookingRepository.findById(appointmentId)
                    .orElseThrow(() -> new UsersNotFoundException("Appointment not found"));

            int amount = booking.getDoctor().getConsultingFee();

            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount * 100);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "appt_" + appointmentId);

            Map<String, String> notes = new HashMap<>();
            notes.put("purpose", "APPOINTMENT_BOOKING");
            notes.put("appointmentId", String.valueOf(appointmentId));
            notes.put("patientId", String.valueOf(booking.getPatient().getPatientId()));
            orderRequest.put("notes", notes);

            Order order = razorpay.orders.create(orderRequest);

            booking.setRazorpayOrderId(order.get("id"));
            bookingRepository.save(booking);

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", order.get("id"));
            resp.put("amount", order.get("amount"));
            resp.put("currency", order.get("currency"));
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error creating order: " + ex.getMessage());
        }
    }
}


















































//package com.hmt.healix.service;
//
//import com.hmt.healix.entity.AppointmentBooking;
//import com.hmt.healix.exception.UsersNotFoundException;
//import com.hmt.healix.repository.AppointmentBookingRepository;
//import com.razorpay.Order;
//import lombok.RequiredArgsConstructor;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import com.razorpay.RazorpayClient;
//
//@Service
//@RequiredArgsConstructor
//public class PaymentService {
//
//    @Value("${razorpay.key.id}")
//    private String razorpayKeyId;
//
//    @Value("${razorpay.key.secret}")
//    private String razorpayKeySecret;
//
//    private final AppointmentBookingRepository bookingRepository;
//
//    public ResponseEntity<?> payForAppointment(
//            Long appointmentId
//    )  {
//        try{
//            AppointmentBooking booking = bookingRepository.findById(appointmentId)
//                    .orElseThrow(() -> new UsersNotFoundException("Appointment not found"));
//
//            int amount=booking.getDoctor().getConsultingFee();
//
//            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
//
//            JSONObject orderRequest = new JSONObject();
//            orderRequest.put("amount", amount*100);
//            orderRequest.put("currency", "INR");
//            orderRequest.put("receipt","appt_"+ appointmentId);
//            orderRequest.put("payment_capture",1);
//
//            Order order=razorpay.orders.create(orderRequest);
//
//            booking.setRazorpayOrderId(order.get("id"));
//            bookingRepository.save(booking);
//            return ResponseEntity.ok(order.toString());
//        }
//        catch(Exception ex){
//            return ResponseEntity.badRequest().body("Error creating order: " + ex.getMessage());        }
//    }
//
//}
