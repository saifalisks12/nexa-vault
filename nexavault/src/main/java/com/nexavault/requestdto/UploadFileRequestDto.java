package com.nexavault.requestdto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadFileRequestDto {

    @NotBlank
    private MultipartFile file;

    private String fileName;

    private String tags;

    private String description; 
}
