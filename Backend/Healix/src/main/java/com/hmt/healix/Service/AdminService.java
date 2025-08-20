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
        return patientRepository.findAllByUserEnabledTrue(pageable);
    }

    public Page<Doctor> getDoctors(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return doctorRepository.findAllByUserAdminAuthorisedTrueAndUserEnabledTrue(pageable);
    }

    public Page<Patient> searchPatient(String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return patientRepository.findAllByUserUsernameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(keyword,keyword,pageable);
    }

    public Page<Doctor> searchDoctor(String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return doctorRepository.findAllByUserUsernameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(keyword, keyword, pageable);

    }

    public void toggleAccountLockPatient(Long userId){
//        Patient patient=patientRepository.findById(Math.toIntExact(patientId)).orElseThrow(
//                ()-> {throw new UserNotFoundException("Patient not found");}
//        );

        Users user=userRepository.findById(userId).orElseThrow();

        user.setAccountLocked(!user.isAccountLocked());
        userRepository.save(user);
        ResponseEntity.ok().build();
    }


    public void toggleAccountLockDoctor(Long userId){

//        Doctor doctor=doctorRepository.findById(Math.toIntExact(doctorId)).orElseThrow(
//                ()-> new UserNotFoundException("Doctor not found")
//        );
        Users user=userRepository.findById(userId).orElseThrow(
                ()-> new UserNotFoundException("User not found")
        );
        System.out.println(user.toString());
        user.setAccountLocked(!user.isAccountLocked());
        userRepository.save(user);
        ResponseEntity.ok().build();
    }


}
