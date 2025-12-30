package com.nexavault.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.nexavault.config.JwtService;
import com.nexavault.dao.UserDao;
import com.nexavault.exception.AlreadyExistException;
import com.nexavault.exception.GenericException;
import com.nexavault.model.Role;
import com.nexavault.model.User;
import com.nexavault.requestdto.AddUserRequestDto;
import com.nexavault.requestdto.JwtLoginRequestDto;
import com.nexavault.responsedto.AuthResponseDto;
import com.nexavault.responsedto.UserResponseDto;
import com.nexavault.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserDao userDao;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	@Override
	public AuthResponseDto authenticateAndGenerateToken(JwtLoginRequestDto jwtRequest) {

		// Authenticate user credentials
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(jwtRequest.email(), jwtRequest.password()));

		// If authentication fails, throw exception
		if (!authentication.isAuthenticated()) {
			throw new GenericException("Invalid credentials", HttpStatus.UNAUTHORIZED, "Authentication failed.");
		}

		// Generate JWT Token
		String token = jwtService.generateToken(jwtRequest);
		Optional<User> currentUser = userDao.findByEmail(jwtRequest.email());
		currentUser.ifPresent(user -> {
			user.setLastLogin(LocalDateTime.now());
			userDao.save(user);
		});

		return new AuthResponseDto(token, "Authentication successful");
	}

	@Override
	public UserResponseDto addUser(AddUserRequestDto addUserRequestDto) {
		// Check if a user with the provided email already exists
		if (userDao.existsByEmail(addUserRequestDto.email())) {
			throw new AlreadyExistException("Emai", "Constant.USER_ALREADY_EXIST");
		}

		// Create and save the user
		User user = new User();
		user.setEmail(addUserRequestDto.email());
		user.setPassword(passwordEncoder.encode(addUserRequestDto.password()));
		user.setRole(Role.USER);
		if (addUserRequestDto.userName().isBlank()) {
			user.setUserName(addUserRequestDto.email());
		}
		user.setUserName(addUserRequestDto.userName());
		userDao.save(user);

		return modelMapper.map(user, UserResponseDto.class);
	}

}
