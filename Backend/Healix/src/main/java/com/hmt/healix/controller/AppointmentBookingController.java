package com.hmt.healix.controller;

import com.hmt.healix.dtos.AppointmentBookingRequest;
import com.hmt.healix.service.AppointmentBookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patient/doctor")
@AllArgsConstructor
public class AppointmentBookingController {

    private final AppointmentBookingService appointmentBookingService;

    @PostMapping("/appointment")
    public ResponseEntity<?> createAppointment(@RequestBody @Valid AppointmentBookingRequest appointmentBookingRequest,
                                               HttpServletRequest httpServletRequest) {

        return appointmentBookingService.appointmentBooking(appointmentBookingRequest, httpServletRequest);
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAllPatientAppointments(HttpServletRequest httpServletRequest) {
        return appointmentBookingService.getPatientAppointmentBooking(httpServletRequest);
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId, HttpServletRequest httpServletRequest) {
        return appointmentBookingService.cancelAppointment(appointmentId, httpServletRequest);
    }

}
