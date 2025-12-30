package com.nexavault.service;

import com.nexavault.requestdto.AddUserRequestDto;
import com.nexavault.requestdto.JwtLoginRequestDto;
import com.nexavault.responsedto.AuthResponseDto;
import com.nexavault.responsedto.UserResponseDto;

import jakarta.validation.Valid;

public interface AuthService {

	UserResponseDto addUser(@Valid AddUserRequestDto addUserRequestDto);

	AuthResponseDto authenticateAndGenerateToken(@Valid JwtLoginRequestDto jwtRequest);

}
