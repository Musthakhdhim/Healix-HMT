package com.hmt.healix.service;

import com.hmt.healix.dtos.ChangePasswordDto;
import com.hmt.healix.dtos.RegisterPatientDto;
import com.hmt.healix.dtos.UpdatePatientDto;
import com.hmt.healix.entity.*;
import com.hmt.healix.exception.AlreadyExistsException;
import com.hmt.healix.exception.PasswordNotMatchingException;
import com.hmt.healix.exception.UsersNotFoundException;
import com.hmt.healix.mapper.PatientMapper;
import com.hmt.healix.mapper.UserMapper;
import com.hmt.healix.repository.DoctorRepository;
import com.hmt.healix.repository.DoctorSlotRepository;
import com.hmt.healix.repository.PatientRepository;
import com.hmt.healix.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PatientMapper patientMapper;
    private final UserMapper userMapper;
    private final DoctorRepository doctorRepository;
    private final DoctorSlotRepository doctorSlotRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> savePatient(String token, RegisterPatientDto registerPatientDto) {
        log.info("Saving new patient with token");
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email from token: {}", email);

        Users user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found for email: {}", email);
            return new UsersNotFoundException("user not found");
        });

        if (patientRepository.findByUser_UserId(user.getUserId()).isPresent()) {
            log.warn("Patient already registered for userId: {}", user.getUserId());
            throw new AlreadyExistsException("Patient already registered");
        }

        Patient patient = patientMapper.toPatient(registerPatientDto);
        patient.setUser(user);
        patientRepository.save(patient);
        log.info("Patient registered successfully for userId: {}", user.getUserId());

        return ResponseEntity.ok().build();
    }

    public Patient getPatientDetails(String token) {
        log.info("Fetching patient details");
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email from token: {}", email);

        Users user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found for email: {}", email);
            return new UsersNotFoundException("user not found");
        });

        Patient patient = patientRepository.findByUser_UserId(user.getUserId()).orElseThrow(() -> {
            log.error("Patient not found for userId: {}", user.getUserId());
            return new UsersNotFoundException("Patient data not found");
        });

        log.info("Fetched patient details successfully for userId: {}", user.getUserId());
        return patient;
    }

    public void updatePatientDetails(String token, UpdatePatientDto updatePatientDto) {
        log.info("Updating patient details");
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email from token: {}", email);

        Users user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found for email: {}", email);
            return new UsersNotFoundException("User not found");
        });

        Patient patient = patientRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> {
                    log.error("Patient not found for userId: {}", user.getUserId());
                    return new UsersNotFoundException("Patient not found");
                });

        patientMapper.updatePatientFromDto(updatePatientDto, patient);
        patient.setUser(user);
        patientRepository.save(patient);

        log.info("Updated patient details successfully for userId: {}", user.getUserId());
    }

    public ResponseEntity<?> changePassword(HttpServletRequest request, ChangePasswordDto changePasswordDto) {
        log.info("Attempting to change password");
        String token = jwtService.getTokenFromAuthorization(request);
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email from token: {}", email);

        Users user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found for email: {}", email);
            return new UsersNotFoundException("User not found");
        });

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            log.warn("Old password does not match for user: {}", email);
            throw new PasswordNotMatchingException("old password is not matching");
        }

        if (!(changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword()))) {
            log.warn("New password and confirm password do not match for user: {}", email);
            throw new PasswordNotMatchingException("your new password is not matching with confirmation password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", email);
        return ResponseEntity.ok("Password changed successfully");
    }

    public Page<Doctor> getDoctorsFromPatients(int page, int size) {
        log.info("Fetching doctors list for patients. Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return doctorRepository.findAllByUserAdminAuthorisedTrueAndUserEnabledTrue(pageable);
    }

    public Page<Doctor> searchDoctorFromPatients(String keyword, int page, int size) {
        log.info("Searching doctors with keyword: {}, Page: {}, Size: {}", keyword, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return doctorRepository.findAllBySpecializationContainingIgnoreCaseOrUserUsernameContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    public List<DoctorSlot> getDoctorSlotsForPatient(Long doctorId) {
        log.info("Fetching available/cancelled doctor slots for doctorId: {}", doctorId);
        return doctorSlotRepository.findAvailableOrCancelledFutureSlots(
                doctorId,
                LocalDate.now(),
                LocalTime.now(),
                List.of(SlotStatus.AVAILABLE, SlotStatus.CANCELLED)
        );
    }
}
