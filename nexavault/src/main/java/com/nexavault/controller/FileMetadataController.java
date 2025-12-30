package com.nexavault.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.nexavault.model.FileAccessControl;
import com.nexavault.model.FileMetadata;
import com.nexavault.responsedto.UploadFileResponseDto;
import com.nexavault.service.FileAccessControlService;
import com.nexavault.service.FileMetadataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/file")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FileMetadataController {

	private final FileMetadataService fileMetadataService;
	private final FileAccessControlService fileAccessControlService;

	// Get all files
	@GetMapping("/all")
	ResponseEntity<List<FileMetadata>> getAllFiles() {
		return ResponseEntity.ok(fileMetadataService.allFile());
	}

	// Get file by IPFS Hash
	@GetMapping("/{ipfsHash}")
	ResponseEntity<FileMetadata> getFileByHash(@PathVariable String ipfsHash) {
		return ResponseEntity.ok(fileMetadataService.getFileByHash(ipfsHash));
	}

	// Search files by tag or description
	@GetMapping("/search")
	ResponseEntity<List<FileMetadata>> searchFiles(@RequestParam(required = false) String tag,
			@RequestParam(required = false) String description, @RequestParam(required = false) String fileName) {
		return ResponseEntity.ok(fileMetadataService.searchFiles(tag, description, fileName));
	}

	// Get files uploaded by current user
	@GetMapping("/user")
	ResponseEntity<List<FileMetadata>> getFilesByUser() {
		String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(fileMetadataService.getFilesByUser(currentUser));
	}

	// Update file metadata (tags, description)
	@PutMapping("/{ipfsHash}")
	ResponseEntity<FileMetadata> updateFileMetadata(@PathVariable String ipfsHash,
			@RequestBody UploadFileResponseDto request) {
		return ResponseEntity.ok(fileMetadataService.updateFileMetadata(ipfsHash, request));
	}

	// Soft delete file metadata
	@DeleteMapping("/{ipfsHash}")
	ResponseEntity<String> deleteFileMetadata(@PathVariable String ipfsHash) {
		fileMetadataService.deleteFileMetadata(ipfsHash);
		return ResponseEntity.ok("File metadata deleted for IPFS hash: " + ipfsHash);
	}

	@PostMapping("/share/{ipfsHash}")
	public ResponseEntity<Map<String, String>> shareFile(@PathVariable String ipfsHash, @RequestParam String email,
			@RequestParam String level) {

		System.out.println(level);

		String shareableLink = fileAccessControlService.shareFile(ipfsHash, email,
				FileAccessControl.AccessLevel.valueOf(level));
		Map<String, String> response = new HashMap<>();
		response.put("message", "File shared successfully.");
		response.put("shareableLink", shareableLink);

		return ResponseEntity.ok(response);
	}
}