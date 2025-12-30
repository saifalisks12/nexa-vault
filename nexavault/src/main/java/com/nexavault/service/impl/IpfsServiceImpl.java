package com.nexavault.service.impl;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nexavault.config.WebclientConfig;
import com.nexavault.dao.FileMetadataDao;
import com.nexavault.exception.EncryptionException;
import com.nexavault.exception.FileOperationException;
import com.nexavault.model.FileAccessControl;
import com.nexavault.model.FileMetadata;
import com.nexavault.requestdto.UploadFileRequestDto;
import com.nexavault.responsedto.UploadFileResponseDto;
import com.nexavault.service.FileAccessControlService;
import com.nexavault.service.IpfsService;

@Service
@RequiredArgsConstructor
@Slf4j
public class IpfsServiceImpl implements IpfsService {

	@Value("${encryption.key}")
	private String encryptionKey;

	private final WebclientConfig webClientConfig;
	private final FileMetadataDao fileMetadataDao;
	private final FileAccessControlService fileAccessControlService;
	private final ModelMapper mapper;

	@Override
	public UploadFileResponseDto uploadToIPFS(UploadFileRequestDto file) {
		byte[] encryptedImage = encryptFile(file);
		MultipartBodyBuilder bodyBuilder = buildMultipartBody(file, encryptedImage);

		Map<String, Object> response = webClientConfig.postToPinata("/pinFileToIPFS", bodyBuilder.build()).block();
		log.info("Pinata Response: {}", response);

		if (response == null || !response.containsKey("IpfsHash")) {
			throw new FileOperationException("Failed to upload to IPFS. No IpfsHash in response.");
		}

		UploadFileResponseDto responseDto = saveFileMetadata(file, extractIpfsHash(response));
		responseDto.setMessage("File uploaded successfully");
		responseDto.setUploadTime(LocalDateTime.now());

		// Grant file owner (uploader) full access
		fileAccessControlService.addAccess(fileMetadataDao.findByIpfsHash(responseDto.getIpfsHash()).get(),
				SecurityContextHolder.getContext().getAuthentication().getName(), FileAccessControl.AccessLevel.ADMIN);

		return responseDto;
	}

	@Override
	public byte[] downloadAndDecrypt(String ipfsHash) {
		String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

		// Fetch file metadata
		FileMetadata metadata = fileMetadataDao.findByIpfsHash(ipfsHash)
				.orElseThrow(() -> new FileOperationException("File not found for the provided IPFS hash."));

		// Bypass access control if the current user is the uploader
		if (!metadata.getUploadedBy().equals(currentUser)
				&& !fileAccessControlService.hasAccess(ipfsHash, currentUser, FileAccessControl.AccessLevel.DOWNLOAD)) {
			throw new FileOperationException("You do not have permission to download this file.");
		}

		// Proceed to download and decrypt the file
		byte[] encryptedData = webClientConfig.getFromIPFS(ipfsHash).block();
		if (encryptedData == null || encryptedData.length == 0) {
			throw new FileOperationException("Failed to download file from IPFS.");
		}

		log.info("Downloaded encrypted file of size {} bytes", encryptedData.length);
		return decryptFile(encryptedData);
	}

	@Override
	public void deleteFromIPFS(String ipfsHash) {
		String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
		// Fetch file metadata
		FileMetadata metadata = fileMetadataDao.findByIpfsHash(ipfsHash)
				.orElseThrow(() -> new FileOperationException("File not found for the provided IPFS hash."));

		// Bypass access control if the current user is the uploader
		if (!metadata.getUploadedBy().equals(currentUser)
				&& !fileAccessControlService.hasAccess(ipfsHash, currentUser, FileAccessControl.AccessLevel.EDITOR)) {
			throw new FileOperationException("You do not have permission to delete this file.");
		}

		webClientConfig.deleteFromPinata(ipfsHash)
				.doOnSuccess(unused -> log.info("Successfully deleted IPFS hash: {}", ipfsHash)).doOnError(e -> {
					log.error("Failed to delete IPFS hash: {}", ipfsHash, e);
					throw new FileOperationException("Failed to delete file from IPFS: " + e.getMessage());
				}).block();
	}

	// Encrypt and Upload Helper
	private byte[] encryptFile(UploadFileRequestDto file) {
		try {
			return encryptImage(file.getFile().getBytes());
		} catch (IOException e) {
			throw new FileOperationException("Failed to read file bytes for encryption.");
		}
	}

	// Build Multipart Form Data for Pinata
	private MultipartBodyBuilder buildMultipartBody(UploadFileRequestDto file, byte[] encryptedImage) {
		MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
		bodyBuilder.part("file", new ByteArrayResource(encryptedImage) {
			@Override
			public String getFilename() {
				return file.getFile().getOriginalFilename();
			}
		}).header("Content-Disposition", "form-data; name=file; filename=" + file.getFile().getOriginalFilename());

		return bodyBuilder;
	}

	// Save File Metadata to DB
	private UploadFileResponseDto saveFileMetadata(UploadFileRequestDto file, String ipfsHash) {
		String fileName = (file.getFileName() == null || file.getFileName().isBlank())
				? file.getFile().getOriginalFilename()
				: file.getFileName();

		FileMetadata metadata = new FileMetadata();
		metadata.setFileName(fileName);
		metadata.setIpfsHash(ipfsHash);
		metadata.setTags(file.getTags());
		metadata.setDescription(file.getDescription());
		metadata.setUploadedBy(SecurityContextHolder.getContext().getAuthentication().getName());

		fileMetadataDao.save(metadata);
		log.info("File metadata saved with IPFS hash: {}", ipfsHash);
		return mapper.map(metadata, UploadFileResponseDto.class);
	}

	// Encryption Logic
	private byte[] encryptImage(byte[] imageData) {
		byte[] iv = new byte[12];
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(iv);

		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			byte[] decodedKey = decodeEncryptionKey();

			SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");
			GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
			byte[] encryptedData = cipher.doFinal(imageData);

			return combineIvWithData(iv, encryptedData);
		} catch (Exception e) {
			throw new EncryptionException("Failed to encrypt image data");
		}
	}

	// Decryption Logic
	private byte[] decryptFile(byte[] encryptedData) {
		try {
			byte[] iv = extractIV(encryptedData);
			byte[] encryptedBytes = extractEncryptedBytes(encryptedData);

			byte[] decodedKey = decodeEncryptionKey();

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");
			GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

			cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
			return cipher.doFinal(encryptedBytes);
		} catch (Exception e) {
			throw new EncryptionException("Failed to decrypt image data");
		}
	}

	private byte[] decodeEncryptionKey() {
		byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
		if (decodedKey.length != 32) {
			throw new EncryptionException("AES key must be 32 bytes. Current length: " + decodedKey.length);
		}
		return decodedKey;
	}

	private byte[] combineIvWithData(byte[] iv, byte[] encryptedData) {
		byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
		System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
		System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);
		return encryptedWithIv;
	}

	private byte[] extractIV(byte[] encryptedData) {
		byte[] iv = new byte[12];
		System.arraycopy(encryptedData, 0, iv, 0, 12);
		return iv;
	}

	private byte[] extractEncryptedBytes(byte[] encryptedData) {
		byte[] encryptedBytes = new byte[encryptedData.length - 12];
		System.arraycopy(encryptedData, 12, encryptedBytes, 0, encryptedBytes.length);
		return encryptedBytes;
	}

	private String extractIpfsHash(Map<String, Object> responseBody) {
		return responseBody.get("IpfsHash").toString();
	}
}
