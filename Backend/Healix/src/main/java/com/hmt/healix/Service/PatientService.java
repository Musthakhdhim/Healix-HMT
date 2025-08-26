package com.hmt.healix.Service;

import com.hmt.healix.Dtos.ChangePasswordDto;
import com.hmt.healix.Dtos.RegisterPatientDto;
import com.hmt.healix.Dtos.UpdatePatientDto;
import com.hmt.healix.Entity.Doctor;
import com.hmt.healix.Entity.DoctorSlot;
import com.hmt.healix.Entity.Patient;
import com.hmt.healix.Entity.Users;
import com.hmt.healix.Exception.AlreadyExistsException;
import com.hmt.healix.Exception.PasswordNotMatchingException;
import com.hmt.healix.Exception.UserNotFoundException;
import com.hmt.healix.Exception.UsersNotFoundException;
import com.hmt.healix.Mapper.PatientMapper;
import com.hmt.healix.Mapper.UserMapper;
import com.hmt.healix.Repository.DoctorRepository;
import com.hmt.healix.Repository.DoctorSlotRepository;
import com.hmt.healix.Repository.PatientRepository;
import com.hmt.healix.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
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
        String email= jwtService.extractEmail(token);


        Users user=userRepository.findByEmail(email).orElseThrow(
                () -> new UsersNotFoundException("user not found")
        );

        if (patientRepository.findByUser_UserId(user.getUserId()).isPresent()) {
            throw new AlreadyExistsException("Patient already registered");
        }

        Patient patient=patientMapper.toPatient(registerPatientDto);

        patient.setUser(user);
        patientRepository.save(patient);

        return ResponseEntity.ok().build();
    }

    public Patient getPatientDetails(String token) {
        String email= jwtService.extractEmail(token);
        Users user=userRepository.findByEmail(email).orElseThrow(
                () -> new UsersNotFoundException("user not found")
        );

        Patient patient=patientRepository.findByUser_UserId(user.getUserId()).orElseThrow(
                () -> new UsersNotFoundException("Patient data not found")
        );

        return patient;
    }

    public void updatePatientDetails(String token, UpdatePatientDto updatePatientDto) {
        String email = jwtService.extractEmail(token);
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsersNotFoundException("User not found"));

        Patient patient = patientRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new UsersNotFoundException("Patient not found"));

        patientMapper.updatePatientFromDto(updatePatientDto, patient);
        patient.setUser(user); // maintain relationship

        patientRepository.save(patient);
    }


    public ResponseEntity<?> changePassword(HttpServletRequest request, ChangePasswordDto changePasswordDto){
        String token=jwtService
                .getTokenFromAuthorization(request);

        String email= jwtService.extractEmail(token);

        Users user=userRepository.findByEmail(email).orElseThrow(
                () -> new UsersNotFoundException("User not found")
        );

//        Users user=userRepository.findById(userId).orElseThrow(()->{
//            throw new UserNotFoundException("user is not found");
//        });

        if(!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())){
            throw new PasswordNotMatchingException("old password is not matching");
        }

        if(!(changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword()))){
            throw new PasswordNotMatchingException("your new password is not matching with confirmation password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    public Page<Doctor> getDoctorsFromPatients(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return doctorRepository.findAllByUserAdminAuthorisedTrueAndUserEnabledTrue(pageable);
    }

    public Page<Doctor> searchDoctorFromPatients(String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return doctorRepository.findAllBySpecializationContainingIgnoreCaseOrUserUsernameContainingIgnoreCase(keyword, keyword, pageable);

    }

    public List<DoctorSlot> getDoctorSlotsForPatient(Long doctorId) {
        return doctorSlotRepository.findAvailableFutureSlots(
                doctorId,
                LocalDate.now(),
                LocalTime.now()
        );
    }


}
