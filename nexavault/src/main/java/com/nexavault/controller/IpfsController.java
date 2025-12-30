package com.nexavault.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.nexavault.requestdto.UploadFileRequestDto;
import com.nexavault.responsedto.UploadFileResponseDto;
import com.nexavault.service.IpfsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/ipfs")
@PreAuthorize("hasAuthority('USER')")
@CrossOrigin(origins = "http://localhost:3000")
public class IpfsController {

	private final IpfsService fileService;

	@PostMapping("/upload")
	public ResponseEntity<UploadFileResponseDto> uploadImage(UploadFileRequestDto request) {

		return ResponseEntity.ok(fileService.uploadToIPFS(request));
	}

	@GetMapping("/download/{ipfsHash}")
	public ResponseEntity<byte[]> downloadFile(@PathVariable String ipfsHash) {
		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(fileService.downloadAndDecrypt(ipfsHash));
	}

	@DeleteMapping("/delete/{ipfsHash}")
	public ResponseEntity<String> deleteFile(@PathVariable String ipfsHash) {
		fileService.deleteFromIPFS(ipfsHash);
		return ResponseEntity.ok("File deleted successfully for hash: " + ipfsHash);
	}
}
