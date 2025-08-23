package com.hmt.healix.Controller;

import com.hmt.healix.Dtos.SlotCreationRequest;
import com.hmt.healix.Entity.DoctorSlot;
import com.hmt.healix.Service.DoctorSlotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/doctor/slots")
public class DoctorSlotController {

    private final DoctorSlotService doctorSlotService;

    @PostMapping
    public ResponseEntity<List<DoctorSlot>> createSlots(@RequestBody @Valid SlotCreationRequest request,HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(doctorSlotService.createSlots(request, httpServletRequest));
    }

    @GetMapping
    public ResponseEntity<List<DoctorSlot>> getDoctorSlots(HttpServletRequest request){
        return ResponseEntity.ok(doctorSlotService.getDoctorSlots(request));
    }
}
