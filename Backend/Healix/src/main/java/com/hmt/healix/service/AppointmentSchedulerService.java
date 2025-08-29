package com.hmt.healix.service;

import com.hmt.healix.entity.AppointmentBooking;
import com.hmt.healix.entity.BookingStatus;
import com.hmt.healix.entity.DoctorSlot;
import com.hmt.healix.entity.SlotStatus;
import com.hmt.healix.repository.AppointmentBookingRepository;
import com.hmt.healix.repository.DoctorSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentSchedulerService {

    private final AppointmentBookingRepository bookingRepository;
    private final DoctorSlotRepository doctorSlotRepository;


    @Scheduled(initialDelay = 0, fixedRate = 300000)
    public void cancelUnpaidAppointments() {

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);

        List<AppointmentBooking> expiredBookings =
                bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoff);


        for (AppointmentBooking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            DoctorSlot slot = booking.getSlot();
            slot.setStatus(SlotStatus.AVAILABLE);

            doctorSlotRepository.save(slot);
            bookingRepository.save(booking);

        }
    }
}































// runs every 5 minutes
//    @Scheduled(fixedRate = 300000)
//    public void cancelUnpaidAppointments() {
//        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
//
//        List<AppointmentBooking> expiredBookings =
//                bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoff);
//
//        for (AppointmentBooking booking : expiredBookings) {
//            booking.setStatus(BookingStatus.CANCELLED);
//            DoctorSlot slot =booking.getSlot();
//            slot.setStatus(SlotStatus.AVAILABLE);
//            doctorSlotRepository.save(slot);
//            bookingRepository.save(booking);
//        }
//    }

//System.out.println("Cancelled booking " + booking.getAppointmentId() +
//        " and freed slot " + slot.getSlotId());

//System.out.println("Running scheduler at " + LocalDateTime.now());
//        System.out.println("Expired bookings found: " + expiredBookings.size());