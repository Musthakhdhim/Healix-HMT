package com.hmt.healix.dtos;

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
