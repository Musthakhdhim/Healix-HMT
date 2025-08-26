package com.hmt.healix.controller;


import com.hmt.healix.entity.Doctor;
import com.hmt.healix.entity.Patient;
import com.hmt.healix.entity.Users;
import com.hmt.healix.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/pending-approval")
    public ResponseEntity<List<Users>> getNonAuthorizedUsers() {
        return adminService.getPatientWithPendingAuthorisation();
    }

    @PostMapping("/approve-doctor/{doctorId}")
    public ResponseEntity<?> approveDoctor(@PathVariable Long doctorId) {
        return adminService.approveDoctor(doctorId);
    }

    @PostMapping("/reject-doctor/{doctorId}")
    public ResponseEntity<?> rejectDoctor(@PathVariable Long doctorId) {
        return adminService.rejectDoctor(doctorId);
    }

    @GetMapping("/patients")
    public Page<Patient> getPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ){
        return adminService.getPatients(page, size);
    }

    @GetMapping("/doctors")
    public Page<Doctor> getDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ){
        return adminService.getDoctors(page, size);
    }

    @GetMapping("/patients/search")
    public Page<Patient> searchPatients(
            @RequestParam String keyword,
//            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
            ){

        return adminService.searchPatient(keyword, page, size);
    }

    @GetMapping("/doctors/search")
    public Page<Doctor> searchDoctors(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ){
        return adminService.searchDoctor(keyword, page, size);
    }


    @PutMapping("/patients/{userId}/toggle-lock")
    public ResponseEntity<?> toggleBlockPatient(@PathVariable long userId) {
        adminService.toggleAccountLockPatient(userId);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/doctors/{userId}/toggle-lock")
    public ResponseEntity<?> toggleBlockDoctor(@PathVariable long userId) {
        adminService.toggleAccountLockDoctor(userId);
        return ResponseEntity.ok().build();
    }
}
