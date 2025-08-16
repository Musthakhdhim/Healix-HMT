package com.hmt.healix.Mapper;

import com.hmt.healix.Dtos.RegisterPatientDto;
import com.hmt.healix.Dtos.UpdatePatientDto;
import com.hmt.healix.Entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    Patient toPatient(RegisterPatientDto registerPatientDto);

//    Patient loginToPatient(UpdatePatientDto updatePatientDto);

    void updatePatientFromDto(UpdatePatientDto dto, @MappingTarget Patient patient);
}
