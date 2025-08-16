package com.hmt.healix.Controller;


import com.hmt.healix.Dtos.RegisterPatientDto;
import com.hmt.healix.Dtos.UpdatePatientDto;
import com.hmt.healix.Entity.Patient;
import com.hmt.healix.Service.JwtService;
import com.hmt.healix.Service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
