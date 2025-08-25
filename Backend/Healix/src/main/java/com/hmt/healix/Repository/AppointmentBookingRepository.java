package com.hmt.healix.Repository;

import com.hmt.healix.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentBookingRepository extends JpaRepository<AppointmentBooking, Long> {

    boolean existsBySlot(DoctorSlot slot);

    List<AppointmentBooking> findByPatient_PatientId(Long patientId);

    List<AppointmentBooking> findByDoctor_DoctorIdAndStatus(Long doctorId, BookingStatus status);

    @Query("SELECT DISTINCT a.patient FROM AppointmentBooking a WHERE a.doctor.doctorId = :doctorId")
    List<Patient> findDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);

}
