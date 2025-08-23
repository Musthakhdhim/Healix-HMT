package com.hmt.healix.Dtos;

import jakarta.persistence.GeneratedValue;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlotCreationRequest {
//    private Long doctorId;
    private LocalDate date;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private int slotDurationMinutes;

}
