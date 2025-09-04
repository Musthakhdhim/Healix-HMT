package com.hmt.healix.service;

import com.hmt.healix.dtos.RegisterDoctorDto;
import com.hmt.healix.dtos.UpdateDoctorDto;
import com.hmt.healix.entity.AppointmentBooking;
import com.hmt.healix.entity.BookingStatus;
import com.hmt.healix.entity.Doctor;
import com.hmt.healix.entity.Users;
import com.hmt.healix.exception.AlreadyExistsException;
import com.hmt.healix.exception.UsersNotFoundException;
import com.hmt.healix.mapper.DoctorMapper;
import com.hmt.healix.repository.AppointmentBookingRepository;
import com.hmt.healix.repository.DoctorRepository;
import com.hmt.healix.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final DoctorMapper doctorMapper;
    private final AppointmentBookingRepository appointmentBookingRepository;

    public ResponseEntity<?> saveDoctor(String token, RegisterDoctorDto registerDoctorDto) {
        log.debug("Attempting to save doctor profile with token: {}", token);

        String email = jwtService.extractEmail(token);
        log.debug("Extracted email from token: {}", email);

        Users user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            throw new UsersNotFoundException("user not found");
        });

        if (doctorRepository.findByUser_UserId(user.getUserId()).isPresent()) {
            log.warn("Doctor profile already exists for userId: {}", user.getUserId());
            throw new AlreadyExistsException("doctor profile already added");
        }

        Doctor doctor = doctorMapper.toDoctor(registerDoctorDto);
        doctor.setUser(user);
        doctorRepository.save(doctor);

        log.info("Doctor profile saved successfully for userId: {}", user.getUserId());
        return ResponseEntity.ok().build();
    }

    public Doctor getDoctorDetails(String token) {
        log.debug("Fetching doctor details with token: {}", token);

        String email = jwtService.extractEmail(token);
        log.debug("Extracted email: {}", email);

        Users user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            throw new UsersNotFoundException("user not found");
        });

        Doctor doctor = doctorRepository.findByUser_UserId(user.getUserId()).orElseThrow(() -> {
            log.error("Doctor profile not found for userId: {}", user.getUserId());
            throw new UsersNotFoundException("doctor not found");
        });

        log.info("Doctor details retrieved successfully for userId: {}", user.getUserId());
        return doctor;
    }

    public void updateDoctor(String token, UpdateDoctorDto updateDoctorDto) {
        log.debug("Updating doctor profile with token: {}", token);

        String email = jwtService.extractEmail(token);
        log.debug("Extracted email: {}", email);

        Users user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            throw new UsersNotFoundException("user not found");
        });

        Doctor doctor = doctorRepository.findByUser_UserId(user.getUserId()).orElseThrow(() -> {
            log.error("Doctor profile not found for userId: {}", user.getUserId());
            throw new UsersNotFoundException("doctor not found");
        });

        doctorMapper.updateDoctorFromDto(updateDoctorDto, doctor);
        doctor.setUser(user);
        doctorRepository.save(doctor);

        log.info("Doctor profile updated successfully for userId: {}", user.getUserId());
    }

    public ResponseEntity<List<AppointmentBooking>> getDoctorAppointments(HttpServletRequest request) {
        log.debug("Fetching doctor appointments from request...");

        String token = jwtService.getTokenFromAuthorization(request);
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email: {}", email);

        Doctor doctor = doctorRepository.findByUserEmail(email).orElseThrow(() -> {
            log.error("Doctor not found with email: {}", email);
            throw new UsersNotFoundException("doctor not found");
        });

        List<AppointmentBooking> appointments =
                appointmentBookingRepository.findByDoctor_DoctorIdAndStatus(doctor.getDoctorId(), BookingStatus.CONFIRMED);

        log.info("Retrieved {} confirmed appointments for doctorId: {}", appointments.size(), doctor.getDoctorId());
        return ResponseEntity.ok(appointments);
    }

    public ResponseEntity<?> getAllDoctorPatients(HttpServletRequest request) {
        log.debug("Fetching all patients of doctor from request...");

        String token = jwtService.getTokenFromAuthorization(request);
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email: {}", email);

        Doctor doctor = doctorRepository.findByUserEmail(email).orElseThrow(() -> {
            log.error("Doctor not found with email: {}", email);
            throw new UsersNotFoundException("doctor not found");
        });

        var patients = appointmentBookingRepository.findDistinctPatientsByDoctorId(doctor.getDoctorId());
        log.info("Retrieved {} unique patients for doctorId: {}", patients.size(), doctor.getDoctorId());

        return ResponseEntity.ok(patients);
    }
}
