package com.hmt.healix.Repository;

import com.hmt.healix.Entity.AppointmentBooking;
import com.hmt.healix.Entity.Doctor;
import com.hmt.healix.Entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentBookingRepository extends JpaRepository<AppointmentBooking, Long> {

    boolean existsBySlot(DoctorSlot slot);

    List<AppointmentBooking> findByPatient_PatientId(Long patientId);
}
