package com.hmt.healix.Controller;


import com.hmt.healix.Dtos.ChangePasswordDto;
import com.hmt.healix.Dtos.RegisterPatientDto;
import com.hmt.healix.Dtos.UpdatePatientDto;
import com.hmt.healix.Entity.Doctor;
import com.hmt.healix.Entity.DoctorSlot;
import com.hmt.healix.Entity.Patient;
import com.hmt.healix.Service.JwtService;
import com.hmt.healix.Service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/patient")
public class PatientController {

    private final JwtService jwtService;
    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<?> addPatient(HttpServletRequest request, @RequestBody RegisterPatientDto patient) {
        String token=jwtService.getTokenFromAuthorization(request);
        patientService.savePatient(token, patient);
        return ResponseEntity.ok().build();
    }

//    @PostMapping
//    public ResponseEntity<?> addPatient(HttpServletRequest request, @RequestBody Patient patient) {
//        String token=jwtService.getTokenFromAuthorization(request);
//        patientService.savePatient(token, patient);
//        return ResponseEntity.ok().build();
//    }

    @GetMapping
    public ResponseEntity<Patient> getPatient(HttpServletRequest request) {
        String token=jwtService.getTokenFromAuthorization(request);
        return ResponseEntity.ok().body(patientService.getPatientDetails(token));
    }


    @PutMapping
    public ResponseEntity<?> updatePatient(HttpServletRequest request, @RequestBody UpdatePatientDto updatePatientDto) {
        String token=jwtService.getTokenFromAuthorization(request);
        patientService.updatePatientDetails(token,updatePatientDto);
        return ResponseEntity.ok().build();

    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request,@Valid @RequestBody ChangePasswordDto changePasswordDto){
        return patientService.changePassword(request,changePasswordDto);
    }


    @GetMapping("/doctors")
    public Page<Doctor> getDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ){
        return patientService.getDoctorsFromPatients(page, size);
    }

    @GetMapping("/doctors/search")
    public Page<Doctor> searchDoctors(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ){
        return patientService.searchDoctorFromPatients(keyword, page, size);
    }


    @GetMapping("/doctors/{doctorId}/slots")
    public ResponseEntity<List<DoctorSlot>> getDoctorSlotsForPatient(@PathVariable Long doctorId) {
        return ResponseEntity.ok(patientService.getDoctorSlotsForPatient(doctorId));
    }

}
