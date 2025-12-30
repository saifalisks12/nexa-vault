package com.nexavault.service;

import java.util.List;
import com.nexavault.model.FileMetadata;
import com.nexavault.responsedto.UploadFileResponseDto;

public interface FileMetadataService {

	List<FileMetadata> allFile();

	void deleteFileMetadata(String ipfsHash);

	FileMetadata updateFileMetadata(String ipfsHash, UploadFileResponseDto request);

	List<FileMetadata> getFilesByUser(String username);

	FileMetadata getFileByHash(String ipfsHash);

	List<FileMetadata> searchFiles(String fileName, String tag, String description);

}
