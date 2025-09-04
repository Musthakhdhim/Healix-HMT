package com.hmt.healix.service;

import com.hmt.healix.dtos.AppointmentBookingRequest;
import com.hmt.healix.entity.*;
import com.hmt.healix.exception.UsersNotFoundException;
import com.hmt.healix.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
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
        log.info("Starting appointment booking: doctorId={}, slotId={}",
                appointmentBookingRequest.getDoctorId(), appointmentBookingRequest.getSlotId());

        String token = jwtService.getTokenFromAuthorization(request);
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email from token: {}", email);

        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> {
                    log.error("Patient not found for email: {}", email);
                    return new UsersNotFoundException("Patient not found");
                });
        log.info("Patient found: {}", patient.getFullName());

        Doctor doctor = doctorRepository.findById(Math.toIntExact(appointmentBookingRequest.getDoctorId()))
                .orElseThrow(() -> {
                    log.error("Doctor not found for ID: {}", appointmentBookingRequest.getDoctorId());
                    return new UsersNotFoundException("Doctor not found");
                });
        log.info("Doctor found: {}", doctor.getDoctorName());

        DoctorSlot slot = doctorSlotRepository.findById(Math.toIntExact(appointmentBookingRequest.getSlotId()))
                .orElseThrow(() -> {
                    log.error("Slot not found for ID: {}", appointmentBookingRequest.getSlotId());
                    return new RuntimeException("Slot not found");
                });
        log.info("Slot retrieved: {} on {}", slot.getSlotId(), slot.getDate());

        BigDecimal consultationFee = BigDecimal.valueOf(doctor.getConsultingFee());
        log.debug("Consultation fee: {}", consultationFee);

        Wallet wallet = walletRepository.findByPatient(patient)
                .orElseGet(() -> {
                    log.warn("No wallet found for patient {}. Creating new wallet with balance=0.", patient.getFullName());
                    return walletRepository.save(Wallet.builder().patient(patient).balance(BigDecimal.ZERO).build());
                });
        log.debug("Wallet balance for patient {}: {}", patient.getFullName(), wallet.getBalance());

        if (wallet.getBalance().compareTo(consultationFee) >= 0) {
            log.info("Sufficient balance. Booking appointment using wallet.");

            wallet.setBalance(wallet.getBalance().subtract(consultationFee));
            walletRepository.save(wallet);
            log.debug("Wallet debited. New balance: {}", wallet.getBalance());

            walletService.debit(patient, consultationFee,
                    "APPT_BOOK_" + slot.getSlotId(),
                    "Consultation fee for appointment #" + slot.getSlotId());

            slot.setStatus(SlotStatus.BOOKED);
            doctorSlotRepository.save(slot);
            log.info("Slot {} marked as BOOKED", slot.getSlotId());

            AppointmentBooking appointmentBooking = new AppointmentBooking();
            appointmentBooking.setPatient(patient);
            appointmentBooking.setDoctor(doctor);
            appointmentBooking.setSlot(slot);
            appointmentBooking.setStatus(BookingStatus.CONFIRMED);
            appointmentBookingRepository.save(appointmentBooking);
            log.info("Appointment CONFIRMED with ID {}", appointmentBooking.getAppointmentId());

            return ResponseEntity.ok(Map.of(
                    "status", "WALLET_CONFIRMED",
                    "appointmentId", appointmentBooking.getAppointmentId(),
                    "message", "Appointment booked using wallet"
            ));
        } else {
            log.warn("Insufficient balance. Marking slot {} as PENDING", slot.getSlotId());

            slot.setStatus(SlotStatus.PENDING);
            doctorSlotRepository.save(slot);

            AppointmentBooking appointmentBooking = new AppointmentBooking();
            appointmentBooking.setPatient(patient);
            appointmentBooking.setDoctor(doctor);
            appointmentBooking.setSlot(slot);
            appointmentBooking.setStatus(BookingStatus.PENDING);
            appointmentBookingRepository.save(appointmentBooking);
            log.info("Appointment PENDING with ID {}", appointmentBooking.getAppointmentId());

            return ResponseEntity.ok(Map.of(
                    "status", "PAYMENT_REQUIRED",
                    "appointmentId", appointmentBooking.getAppointmentId(),
                    "message", "Proceed to Razorpay Payment"
            ));
        }
    }

    public ResponseEntity<List<AppointmentBooking>> getPatientAppointmentBooking(HttpServletRequest request) {
        log.info("Fetching patient appointment bookings.");
        String token = jwtService.getTokenFromAuthorization(request);
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email: {}", email);

        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> {
                    log.error("Patient not found for email: {}", email);
                    return new UsersNotFoundException("Patient not found");
                });

        log.info("Returning bookings for patient {}", patient.getFullName());
        return ResponseEntity.ok(appointmentBookingRepository.findByPatient_PatientId(patient.getPatientId()));
    }

    public ResponseEntity<?> cancelAppointment(Long appointmentId, HttpServletRequest request) {
        log.info("Attempting to cancel appointment {}", appointmentId);

        String token = jwtService.getTokenFromAuthorization(request);
        String email = jwtService.extractEmail(token);

        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> {
                    log.error("Patient not found for email: {}", email);
                    return new UsersNotFoundException("Patient not found");
                });

        AppointmentBooking booking = appointmentBookingRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment not found for ID: {}", appointmentId);
                    return new RuntimeException("Appointment not found");
                });

        if (!(booking.getPatient().getPatientId() == patient.getPatientId())) {
            log.error("Unauthorized cancellation attempt by patient {} for appointment {}", patient.getFullName(), appointmentId);
            throw new RuntimeException("Unauthorized to cancel this appointment");
        }

        DoctorSlot slot = booking.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        doctorSlotRepository.save(slot);
        log.info("Slot {} reset to AVAILABLE", slot.getSlotId());

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            int fee = booking.getDoctor().getConsultingFee();
            walletService.credit(patient, new BigDecimal(fee),
                    "APPT_CANCEL_" + booking.getAppointmentId(),
                    "Refund for cancelled appointment #" + booking.getAppointmentId());
            log.info("Refund issued to wallet for cancelled appointment {}", appointmentId);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        appointmentBookingRepository.save(booking);
        log.info("Appointment {} marked as CANCELLED", appointmentId);

        return ResponseEntity.ok("Appointment cancelled successfully");
    }

    public ResponseEntity<?> getAppointmentById(Long appointmentId) {
        log.info("Fetching appointment by ID {}", appointmentId);

        AppointmentBooking appointment = appointmentBookingRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment not found for ID: {}", appointmentId);
                    return new UsersNotFoundException("appointment not found");
                });

        log.debug("Appointment details retrieved for ID {}", appointmentId);
        return ResponseEntity.ok(appointment);
    }
}
