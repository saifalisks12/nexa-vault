package com.nexavault.requestdto;

import jakarta.validation.constraints.NotBlank;

public record JwtLoginRequestDto(@NotBlank String email, @NotBlank String password) {

}
