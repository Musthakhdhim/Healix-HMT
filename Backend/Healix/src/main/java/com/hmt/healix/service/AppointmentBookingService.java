package com.hmt.healix.service;

import com.hmt.healix.dtos.AppointmentBookingRequest;
import com.hmt.healix.entity.*;
import com.hmt.healix.exception.UsersNotFoundException;
import com.hmt.healix.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AppointmentBookingService {

    private final JwtService jwtService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorSlotRepository doctorSlotRepository;
    private final AppointmentBookingRepository appointmentBookingRepository;
    private final WalletService walletService;
    private final WalletRepository walletRepository;

    public ResponseEntity<?> appointmentBooking(AppointmentBookingRequest appointmentBookingRequest,
                                                HttpServletRequest request) {
        String token = jwtService.getTokenFromAuthorization(request);
        String email = jwtService.extractEmail(token);

        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsersNotFoundException("Patient not found"));

        Doctor doctor = doctorRepository.findById(Math.toIntExact(appointmentBookingRequest.getDoctorId()))
                .orElseThrow(() -> new UsersNotFoundException("Doctor not found"));

        DoctorSlot slot = doctorSlotRepository.findById(Math.toIntExact(appointmentBookingRequest.getSlotId()))
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        BigDecimal consultationFee = BigDecimal.valueOf(doctor.getConsultingFee());

        Wallet wallet = walletRepository.findByPatient(patient)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(consultationFee) >= 0) {
            wallet.setBalance(wallet.getBalance().subtract(consultationFee));
            walletRepository.save(wallet);

            slot.setStatus(SlotStatus.BOOKED);
            doctorSlotRepository.save(slot);

            AppointmentBooking appointmentBooking = new AppointmentBooking();
            appointmentBooking.setPatient(patient);
            appointmentBooking.setDoctor(doctor);
            appointmentBooking.setSlot(slot);
            appointmentBooking.setStatus(BookingStatus.CONFIRMED);

            appointmentBookingRepository.save(appointmentBooking);

//            return ResponseEntity.ok("Appointment booked using wallet");
            return ResponseEntity.ok(Map.of(
                    "status", "WALLET_CONFIRMED",
                    "appointmentId", appointmentBooking.getAppointmentId(),
                    "message", "Appointment booked using wallet"
            ));
        } else {
            slot.setStatus(SlotStatus.PENDING);
            doctorSlotRepository.save(slot);

            AppointmentBooking appointmentBooking = new AppointmentBooking();
            appointmentBooking.setPatient(patient);
            appointmentBooking.setDoctor(doctor);
            appointmentBooking.setSlot(slot);
            appointmentBooking.setStatus(BookingStatus.PENDING);

            appointmentBookingRepository.save(appointmentBooking);

//            return ResponseEntity.ok("Proceed to Razorpay Payment");
            return ResponseEntity.ok(Map.of(
                    "status", "PAYMENT_REQUIRED",
                    "appointmentId", appointmentBooking.getAppointmentId(),
                    "message", "Proceed to Razorpay Payment"
            ));
        }
    }


    public ResponseEntity<List<AppointmentBooking>> getPatientAppointmentBooking(HttpServletRequest request) {
        String token=jwtService.getTokenFromAuthorization(request);
        String email= jwtService.extractEmail(token);

        Patient patient=patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new UsersNotFoundException("Patient not found"));

        return ResponseEntity.ok( appointmentBookingRepository.findByPatient_PatientId(patient.getPatientId()));

    }


    public ResponseEntity<?> cancelAppointment(Long appointmentId, HttpServletRequest request) {
        String token = jwtService.getTokenFromAuthorization(request);
        String email = jwtService.extractEmail(token);

        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsersNotFoundException("Patient not found"));

        AppointmentBooking booking = appointmentBookingRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!(booking.getPatient().getPatientId()==patient.getPatientId())) {
            throw new RuntimeException("Unauthorized to cancel this appointment");
        }

        DoctorSlot slot = booking.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        doctorSlotRepository.save(slot);

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            int fee = booking.getDoctor().getConsultingFee();
            walletService.credit(patient, new BigDecimal(fee),
                    "APPT_CANCEL_" + booking.getAppointmentId(),
                    "Refund for cancelled appointment #" + booking.getAppointmentId());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        appointmentBookingRepository.save(booking);

        return ResponseEntity.ok("Appointment cancelled successfully");
    }

    public ResponseEntity<?> getAppointmentById(Long appointmentId) {
        AppointmentBooking appointment=appointmentBookingRepository.findById(appointmentId)
                .orElseThrow(
                        ()->new UsersNotFoundException("appotinment not founr")
                );

        return ResponseEntity.ok(appointment);
    }
}






























//    public ResponseEntity<?> appointmentBooking(AppointmentBookingRequest appointmentBookingRequest,
//                                                HttpServletRequest request) {
//        String token=jwtService.getTokenFromAuthorization(request);
//        String email= jwtService.extractEmail(token);
//
//        Patient patient=patientRepository.findByUserEmail(email)
//                .orElseThrow(()-> new UsersNotFoundException("Patient not found"));
//
//        Doctor doctor=doctorRepository.findById(Math.toIntExact(appointmentBookingRequest.getDoctorId()))
//                .orElseThrow(()-> new UsersNotFoundException("Doctor not found"));
//
//        DoctorSlot slot=doctorSlotRepository.findById(Math.toIntExact(appointmentBookingRequest.getSlotId()))
//                .orElseThrow(()->new RuntimeException("slot not found"));
//
//        slot.setStatus(SlotStatus.PENDING);
//        doctorSlotRepository.save(slot);
//
//        AppointmentBooking appointmentBooking=new AppointmentBooking();
//        appointmentBooking.setPatient(patient);
//        appointmentBooking.setDoctor(doctor);
//        appointmentBooking.setSlot(slot);
//        appointmentBooking.setStatus(BookingStatus.PENDING);
////        appointmentBooking.setRazorpayOrderId(razorpayOrder.get("id"));
//        appointmentBookingRepository.save(appointmentBooking);
//
//        return ResponseEntity.ok(appointmentBooking);
//
//    }
//    public ResponseEntity<?> cancelAppointment(Long appointmentId, HttpServletRequest request) {
//        String token = jwtService.getTokenFromAuthorization(request);
//        String email = jwtService.extractEmail(token);
//
//        Patient patient = patientRepository.findByUserEmail(email)
//                .orElseThrow(() -> new UsersNotFoundException("Patient not found"));
//
//        AppointmentBooking booking = appointmentBookingRepository.findById(appointmentId)
//                .orElseThrow(() -> new RuntimeException("Appointment not found"));
//
//
//        if (!(booking.getPatient().getPatientId() ==patient.getPatientId())) {
//            throw new RuntimeException("Unauthorized to cancel this appointment");
//        }
//
//        DoctorSlot slot=booking.getSlot();
//        slot.setStatus(SlotStatus.AVAILABLE);
//        doctorSlotRepository.save(slot);
//
//        appointmentBookingRepository.delete(booking);
//
//        return ResponseEntity.ok("Appointment cancelled successfully");
//    }