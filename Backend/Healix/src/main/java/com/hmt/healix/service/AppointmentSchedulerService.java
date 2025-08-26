package com.hmt.healix.service;

import com.hmt.healix.entity.AppointmentBooking;
import com.hmt.healix.entity.BookingStatus;
import com.hmt.healix.repository.AppointmentBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentSchedulerService {

    private final AppointmentBookingRepository bookingRepository;

    // runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void cancelUnpaidAppointments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);

        List<AppointmentBooking> expiredBookings =
                bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoff);

        for (AppointmentBooking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
    }
}

