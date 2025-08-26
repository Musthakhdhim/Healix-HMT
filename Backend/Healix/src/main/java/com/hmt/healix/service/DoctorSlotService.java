package com.hmt.healix.service;

import com.hmt.healix.dtos.SlotCreationRequest;
import com.hmt.healix.entity.Doctor;
import com.hmt.healix.entity.DoctorSlot;
import com.hmt.healix.entity.SlotStatus;
import com.hmt.healix.exception.AlreadyExistsException;
import com.hmt.healix.exception.SlotExpiredException;
import com.hmt.healix.exception.UsersNotFoundException;
import com.hmt.healix.repository.DoctorRepository;
import com.hmt.healix.repository.DoctorSlotRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        if(request.getDate().isBefore(LocalDate.now())) {
            throw new SlotExpiredException("provided date is expired");
        }

        if(request.getDate().isEqual(LocalDate.now()) &&request.getAvailableFrom().isBefore(LocalTime.now())){
            throw new SlotExpiredException("provided time is expired");
        }

        if(request.getAvailableTo().isBefore(request.getAvailableFrom())){
            throw new SlotExpiredException("available to is before from time");
        }

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


        return doctorSlotRepository.findAvailableFutureSlots(
                doctor.getDoctorId(),
                LocalDate.now(),
                LocalTime.now()
        );
    }
}
