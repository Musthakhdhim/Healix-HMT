package com.hmt.healix.repository;

import com.hmt.healix.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentBookingRepository extends JpaRepository<AppointmentBooking, Long> {

    boolean existsBySlot(DoctorSlot slot);

    List<AppointmentBooking> findByPatient_PatientId(Long patientId);

    List<AppointmentBooking> findByDoctor_DoctorIdAndStatus(Long doctorId, BookingStatus status);

    @Query("SELECT DISTINCT a.patient FROM AppointmentBooking a WHERE a.doctor.doctorId = :doctorId")
    List<Patient> findDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);

    Optional<AppointmentBooking> findByRazorpayOrderId(String orderId);

    List<AppointmentBooking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime cutoff);
}


