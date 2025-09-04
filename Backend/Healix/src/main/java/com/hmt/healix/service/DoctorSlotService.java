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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DoctorSlotService {

    private final JwtService jwtService;
    private DoctorSlotRepository doctorSlotRepository;
    private DoctorRepository doctorRepository;

    @Transactional
    public List<DoctorSlot> createSlots(SlotCreationRequest request, HttpServletRequest httpServletRequest) {
        log.debug("Creating slots with request: {}", request);

        if (request.getDate().isBefore(LocalDate.now())) {
            log.warn("Provided date {} is expired", request.getDate());
            throw new SlotExpiredException("provided date is expired");
        }

        if (request.getDate().isEqual(LocalDate.now()) && request.getAvailableFrom().isBefore(LocalTime.now())) {
            log.warn("Provided start time {} is expired for today's date", request.getAvailableFrom());
            throw new SlotExpiredException("provided time is expired");
        }

        if (request.getAvailableTo().isBefore(request.getAvailableFrom())) {
            log.error("Invalid slot range: availableTo {} is before availableFrom {}", request.getAvailableTo(), request.getAvailableFrom());
            throw new SlotExpiredException("available to is before from time");
        }

        String token = jwtService.getTokenFromAuthorization(httpServletRequest);
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email from token: {}", email);

        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> {
                    log.error("Doctor not found with email: {}", email);
                    return new UsersNotFoundException("Doctor not found");
                });

        List<DoctorSlot> doctorSlots = new ArrayList<>();
        LocalTime current = request.getAvailableFrom();

        while (current.plusMinutes(request.getSlotDurationMinutes()).isBefore(request.getAvailableTo())
                || current.plusMinutes(request.getSlotDurationMinutes()).equals(request.getAvailableTo())) {

            LocalTime end = current.plusMinutes(request.getSlotDurationMinutes());

            boolean exists = doctorSlotRepository.existsOverlappingSlot(
                    doctor.getDoctorId(),
                    request.getDate(),
                    current,
                    end
            );

            if (exists) {
                log.warn("Overlapping slot detected for doctorId: {} from {} to {}", doctor.getDoctorId(), current, end);
                throw new AlreadyExistsException("Overlapping slot exists for " + current + " - " + end);
            }

            DoctorSlot doctorSlot = DoctorSlot.builder()
                    .doctor(doctor)
                    .date(request.getDate())
                    .startTime(current)
                    .endTime(end)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            doctorSlots.add(doctorSlot);
            log.debug("Added slot: {} - {}", current, end);

            current = end;
        }

        List<DoctorSlot> savedSlots = doctorSlotRepository.saveAll(doctorSlots);
        log.info("Created {} slots successfully for doctorId: {}", savedSlots.size(), doctor.getDoctorId());
        return savedSlots;
    }

    public List<DoctorSlot> getDoctorSlots(HttpServletRequest request) {
        log.debug("Fetching doctor slots...");

        String token = jwtService.getTokenFromAuthorization(request);
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email from token: {}", email);

        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> {
                    log.error("Doctor not found with email: {}", email);
                    return new UsersNotFoundException("Doctor not found");
                });

        List<DoctorSlot> slots = doctorSlotRepository.findAvailableOrCancelledFutureSlots(
                doctor.getDoctorId(),
                LocalDate.now(),
                LocalTime.now(),
                List.of(SlotStatus.AVAILABLE, SlotStatus.CANCELLED)
        );

        log.info("Retrieved {} available/cancelled future slots for doctorId: {}", slots.size(), doctor.getDoctorId());
        return slots;
    }
}
