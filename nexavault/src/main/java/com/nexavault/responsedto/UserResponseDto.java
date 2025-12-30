package com.nexavault.responsedto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserResponseDto {
	
	private String id;
	private String email;
	private String userName;
	private LocalDateTime updatedAt;
}