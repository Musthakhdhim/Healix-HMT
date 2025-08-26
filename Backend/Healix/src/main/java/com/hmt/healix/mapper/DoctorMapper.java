package com.hmt.healix.mapper;


import com.hmt.healix.dtos.RegisterDoctorDto;
import com.hmt.healix.dtos.UpdateDoctorDto;
import com.hmt.healix.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface DoctorMapper {

    Doctor toDoctor(RegisterDoctorDto registerDoctorDto);

    void updateDoctorFromDto(UpdateDoctorDto updateDoctorDto,@MappingTarget Doctor doctor);
}
