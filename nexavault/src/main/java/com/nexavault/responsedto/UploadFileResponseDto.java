package com.nexavault.responsedto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileResponseDto {
	private String ipfsHash;
	private String fileName;
	private String description;
	private String tags;
	private LocalDateTime uploadTime;
	private String message;
}
