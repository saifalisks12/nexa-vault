package com.nexavault.requestdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddUserRequestDto(@Email String email, String userName,
		@NotBlank(message = "Password cannot be blank") @Size(min = 8, message = "Password must be at least 8 characters long") String password) {
}
