package com.hmt.healix.Repository;

import com.hmt.healix.Entity.Doctor;
import com.hmt.healix.Entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Integer> {
    List<DoctorSlot> findByDoctor_DoctorId(long doctorId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM DoctorSlot s " +
            "WHERE s.doctor.doctorId = :doctorId " +
            "AND s.date = :date " +
            "AND ((:startTime < s.endTime AND :endTime > s.startTime))")
    boolean existsOverlappingSlot(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime);

    List<DoctorSlot> findByDoctor_DoctorId(Long doctorId);

    @Query("SELECT s FROM DoctorSlot s " +
            "WHERE s.doctor.doctorId = :doctorId " +
            "AND s.status = 'AVAILABLE' " +
            "AND (s.date > :today OR (s.date = :today AND s.endTime > :now))")
    List<DoctorSlot> findAvailableFutureSlots(Long doctorId, LocalDate today, LocalTime now);

}
