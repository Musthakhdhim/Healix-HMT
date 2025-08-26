package com.hmt.healix.mapper;

import com.hmt.healix.dtos.RegisterPatientDto;
import com.hmt.healix.dtos.UpdatePatientDto;
import com.hmt.healix.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    Patient toPatient(RegisterPatientDto registerPatientDto);

//    Patient loginToPatient(UpdatePatientDto updatePatientDto);

    void updatePatientFromDto(UpdatePatientDto dto, @MappingTarget Patient patient);
}
