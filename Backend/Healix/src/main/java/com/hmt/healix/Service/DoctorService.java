package com.hmt.healix.Service;

import com.hmt.healix.Dtos.RegisterDoctorDto;
import com.hmt.healix.Dtos.UpdateDoctorDto;
import com.hmt.healix.Entity.Doctor;
import com.hmt.healix.Entity.Users;
import com.hmt.healix.Exception.AlreadyExistsException;
import com.hmt.healix.Exception.UsersNotFoundException;
import com.hmt.healix.Mapper.DoctorMapper;
import com.hmt.healix.Repository.DoctorRepository;
import com.hmt.healix.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final DoctorMapper doctorMapper;

    public ResponseEntity<?> saveDoctor(String token, RegisterDoctorDto registerDoctorDto) {

        String email= jwtService.extractEmail(token);

        Users user=userRepository.findByEmail(email).orElseThrow(
                ()-> {
                    throw new UsersNotFoundException("user not found");
                }
        );

        if(doctorRepository.findByUser_UserId(user.getUserId()).isPresent()){
            throw new AlreadyExistsException("doctor profile already added");
        }

        Doctor doctor=doctorMapper.toDoctor(registerDoctorDto);

        doctor.setUser(user);
        doctorRepository.save(doctor);
        return ResponseEntity.ok().build();
    }

    public Doctor getDoctorDetails(String token) {
        String email= jwtService.extractEmail(token);
        Users user=userRepository.findByEmail(email).orElseThrow(
                ()->new UsersNotFoundException("user not found")
        );
        Doctor doctor=doctorRepository.findByUser_UserId(user.getUserId()).orElseThrow(
                ()->new UsersNotFoundException("doctor not found")
        );

        return doctor;
    }

    public void updateDoctor(String token, UpdateDoctorDto updateDoctorDto) {
        String email= jwtService.extractEmail(token);
        Users user=userRepository.findByEmail(email).orElseThrow(
                ()->new UsersNotFoundException("user not found")
        );
        Doctor doctor=doctorRepository.findByUser_UserId(user.getUserId()).orElseThrow(
                ()->new UsersNotFoundException("doctor not found")
        );

        doctorMapper.updateDoctorFromDto(updateDoctorDto, doctor);
        doctor.setUser(user);
        doctorRepository.save(doctor);
    }
}
