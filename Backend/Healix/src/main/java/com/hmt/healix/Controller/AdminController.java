package com.hmt.healix.Controller;


import com.hmt.healix.Entity.Doctor;
import com.hmt.healix.Entity.Patient;
import com.hmt.healix.Entity.Users;
import com.hmt.healix.Service.AdminService;
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
    
}
