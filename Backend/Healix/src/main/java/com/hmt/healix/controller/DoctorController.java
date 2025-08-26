package com.hmt.healix.controller;

import com.hmt.healix.dtos.RegisterDoctorDto;
import com.hmt.healix.dtos.UpdateDoctorDto;
import com.hmt.healix.entity.Doctor;
import com.hmt.healix.service.DoctorService;
import com.hmt.healix.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/doctor")
@AllArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> saveDoctorDetails(HttpServletRequest request,@RequestBody RegisterDoctorDto registerDoctorDto) {
        String token= jwtService.getTokenFromAuthorization(request);

        doctorService.saveDoctor(token, registerDoctorDto);
        return ResponseEntity.ok().body("doctor profile added successfully");
    }
    @GetMapping
    public ResponseEntity<Doctor> getDoctorDetails(HttpServletRequest request) {
        String token= jwtService.getTokenFromAuthorization(request);

        return ResponseEntity.ok().body(doctorService.getDoctorDetails(token));
    }
    @PutMapping
    public ResponseEntity<?> updateDoctorDetails(HttpServletRequest request,@RequestBody UpdateDoctorDto updateDoctorDto) {
        String token= jwtService.getTokenFromAuthorization(request);

        doctorService.updateDoctor(token, updateDoctorDto);
        return ResponseEntity.ok().body("doctor profile updated successfully");
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAllDoctorAppointment(HttpServletRequest request) {
        return doctorService.getDoctorAppointments(request);
    }

    @GetMapping("/patients")
    public ResponseEntity<?> getAllDoctorPatient(HttpServletRequest request) {
        return doctorService.getAllDoctorPatients(request);
    }

}
