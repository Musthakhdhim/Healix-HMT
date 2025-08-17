package com.hmt.healix.Mapper;


import com.hmt.healix.Dtos.RegisterDoctorDto;
import com.hmt.healix.Dtos.UpdateDoctorDto;
import com.hmt.healix.Entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface DoctorMapper {

    Doctor toDoctor(RegisterDoctorDto registerDoctorDto);

    void updateDoctorFromDto(UpdateDoctorDto updateDoctorDto,@MappingTarget Doctor doctor);
}
