package com.hmt.healix.mapper;

import com.hmt.healix.dtos.RegisterRequest;
import com.hmt.healix.entity.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    Users toUser(RegisterRequest registerRequest);
}
