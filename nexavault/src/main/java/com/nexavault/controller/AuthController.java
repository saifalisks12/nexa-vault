package com.nexavault.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexavault.requestdto.AddUserRequestDto;
import com.nexavault.requestdto.JwtLoginRequestDto;
import com.nexavault.responsedto.AuthResponseDto;
import com.nexavault.responsedto.UserResponseDto;
import com.nexavault.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Tag(name = "User Controller API", description = "API for managing users")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

	private final AuthService authService;
	/**
	 * API to add a new user.
	 * 
	 * @param addUserRequestDto - The user details to be added.
	 * @return ResponseEntity containing UserResponseDto and HTTP status.
	 */
	@PostMapping("/register")
	@Operation(summary = "Add a new user", description = "This API allows you to add a new user to the system.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User added successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content) })
	public ResponseEntity<UserResponseDto> addUser(@Valid @RequestBody AddUserRequestDto addUserRequestDto) {
		return new ResponseEntity<>(authService.addUser(addUserRequestDto), HttpStatus.CREATED);
	}

	/**
	 * API to authenticate a user and generate a JWT token.
	 * 
	 * @param jwtRequest - Login request containing the user's email and password.
	 * @return ResponseEntity containing the JWT token or error message.
	 */
	@PostMapping("/authenticate")
	@Operation(summary = "Generate JWT Token", description = "This API authenticates the user and generates a JWT token for future requests.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Authentication successful, token generated"),
			@ApiResponse(responseCode = "400", description = "Invalid credentials"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<AuthResponseDto> authenticateAndGetToken(@Valid @RequestBody JwtLoginRequestDto jwtRequest) {

		return ResponseEntity.ok(authService.authenticateAndGenerateToken(jwtRequest));
	}
}
