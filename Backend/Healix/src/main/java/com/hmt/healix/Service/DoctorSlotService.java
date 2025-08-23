package com.hmt.healix.Service;

import com.hmt.healix.Dtos.SlotCreationRequest;
import com.hmt.healix.Entity.Doctor;
import com.hmt.healix.Entity.DoctorSlot;
import com.hmt.healix.Entity.SlotStatus;
import com.hmt.healix.Exception.AlreadyExistsException;
import com.hmt.healix.Exception.UsersNotFoundException;
import com.hmt.healix.Repository.DoctorRepository;
import com.hmt.healix.Repository.DoctorSlotRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DoctorSlotService {
    private final JwtService jwtService;
    private DoctorSlotRepository doctorSlotRepository;
    private DoctorRepository doctorRepository;

    @Transactional
    public List<DoctorSlot> createSlots(SlotCreationRequest request,HttpServletRequest httpServletRequest) {

        String token= jwtService.getTokenFromAuthorization(httpServletRequest);
        String email= jwtService.extractEmail(token);

        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsersNotFoundException("Doctor not found"));

        List<DoctorSlot> doctorSlots = new ArrayList<>();

        LocalTime current=request.getAvailableFrom();

        while(current.plusMinutes(request.getSlotDurationMinutes()).isBefore(request.getAvailableTo())
        || current.plusMinutes(request.getSlotDurationMinutes()).equals(request.getAvailableTo())){

            LocalTime end=current.plusMinutes(request.getSlotDurationMinutes());

            boolean exists = doctorSlotRepository.existsOverlappingSlot(
                    doctor.getDoctorId(),
                    request.getDate(),
                    current,
                    end
            );

            if (exists) {
                throw new AlreadyExistsException("Overlapping slot exists for " + current + " - " + end);
            }

            DoctorSlot doctorSlot=DoctorSlot.builder()
                    .doctor(doctor)
                    .date(request.getDate())
                    .startTime(current)
                    .endTime(end)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            doctorSlots.add(doctorSlot);

            current=end;
        }
        return doctorSlotRepository.saveAll(doctorSlots);
    }

    public List<DoctorSlot> getDoctorSlots(HttpServletRequest request) {
        String token=jwtService.getTokenFromAuthorization(request);
        String email=jwtService.extractEmail(token);

        Doctor doctor=doctorRepository.findByUserEmail(email)
                .orElseThrow(()->new UsersNotFoundException("Doctor not found"));


        return doctorSlotRepository.findByDoctor_DoctorId(doctor.getDoctorId());
    }
}
