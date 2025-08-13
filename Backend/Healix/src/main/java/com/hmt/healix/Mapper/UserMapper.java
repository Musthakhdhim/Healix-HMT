package com.hmt.healix.Mapper;

import com.hmt.healix.Dtos.RegisterRequest;
import com.hmt.healix.Entity.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    Users toUser(RegisterRequest registerRequest);
}
