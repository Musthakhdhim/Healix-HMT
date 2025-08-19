package com.hmt.healix.Service;

import com.hmt.healix.Entity.Doctor;
import com.hmt.healix.Entity.Patient;
import com.hmt.healix.Entity.Role;
import com.hmt.healix.Entity.Users;
import com.hmt.healix.Exception.AlreadyApprovedException;
import com.hmt.healix.Exception.UserIsNotDoctorException;
import com.hmt.healix.Exception.UserNotFoundException;
import com.hmt.healix.Repository.DoctorRepository;
import com.hmt.healix.Repository.PatientRepository;
import com.hmt.healix.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public ResponseEntity<List<Users>> getPatientWithPendingAuthorisation(){
        return ResponseEntity.ok(userRepository.findByRoleAndAdminAuthorised(Role.DOCTOR, false));
    }

    public ResponseEntity<?> approveDoctor(Long doctorId){
        Users doctor=userRepository.findById(doctorId).orElseThrow(
                ()->new UserNotFoundException("Doctor not found")
        );

        if(doctor.getRole() != Role.DOCTOR){
            throw new UserIsNotDoctorException("User is not doctor");
        }

        if(doctor.isAdminAuthorised()){
            throw new AlreadyApprovedException("your account is already approved");
        }

        doctor.setAdminAuthorised(true);

        userRepository.save(doctor);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> rejectDoctor(Long doctorId){
        Users doctor=userRepository.findById(doctorId).orElseThrow(
                ()->new UserNotFoundException("Doctor not found")
        );

        if (doctor.getRole() != Role.DOCTOR) {
            throw new RuntimeException("User is not a doctor");
        }

        userRepository.delete(doctor);
        return new ResponseEntity<>("you haven't met our requirement to be our doctor",HttpStatus.NON_AUTHORITATIVE_INFORMATION);
    }


    public Page<Patient> getPatients(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return patientRepository.findAll(pageable);
    }

    public Page<Doctor> getDoctors(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return doctorRepository.findAll(pageable);
    }
}
