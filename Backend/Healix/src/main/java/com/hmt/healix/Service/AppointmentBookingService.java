package com.hmt.healix.Service;

import com.hmt.healix.Dtos.AppointmentBookingRequest;
import com.hmt.healix.Entity.*;
import com.hmt.healix.Exception.UsersNotFoundException;
import com.hmt.healix.Repository.AppointmentBookingRepository;
import com.hmt.healix.Repository.DoctorRepository;
import com.hmt.healix.Repository.DoctorSlotRepository;
import com.hmt.healix.Repository.PatientRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.print.Doc;
import java.util.List;

@Service
@AllArgsConstructor
public class AppointmentBookingService {

    private final JwtService jwtService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorSlotRepository doctorSlotRepository;
    private final AppointmentBookingRepository appointmentBookingRepository;

    public ResponseEntity<?> appointmentBooking(AppointmentBookingRequest appointmentBookingRequest,
    HttpServletRequest request) {
        String token=jwtService.getTokenFromAuthorization(request);
        String email= jwtService.extractEmail(token);

        Patient patient=patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new UsersNotFoundException("Patient not found"));

        Doctor doctor=doctorRepository.findById(Math.toIntExact(appointmentBookingRequest.getDoctorId()))
                .orElseThrow(()-> new UsersNotFoundException("Doctor not found"));

        DoctorSlot slot=doctorSlotRepository.findById(Math.toIntExact(appointmentBookingRequest.getSlotId()))
                .orElseThrow(()->new RuntimeException("slot not found"));

        if (appointmentBookingRepository.existsBySlot(slot)) {
            throw new RuntimeException("This slot is already booked!");
        }

        AppointmentBooking appointmentBooking=new AppointmentBooking();
        appointmentBooking.setPatient(patient);
        appointmentBooking.setDoctor(doctor);
        appointmentBooking.setSlot(slot);
        appointmentBooking.setStatus(BookingStatus.PENDING);
        appointmentBookingRepository.save(appointmentBooking);

        return ResponseEntity.ok(appointmentBooking);

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

        if (!(booking.getPatient().getPatientId() ==patient.getPatientId())) {
            throw new RuntimeException("Unauthorized to cancel this appointment");
        }

        appointmentBookingRepository.delete(booking);

        return ResponseEntity.ok("Appointment cancelled successfully");
    }

}
