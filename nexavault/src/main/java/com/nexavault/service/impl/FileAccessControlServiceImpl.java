package com.nexavault.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.nexavault.dao.FileAccessControlDao;
import com.nexavault.dao.FileMetadataDao;
import com.nexavault.exception.FileOperationException;
import com.nexavault.model.FileAccessControl;
import com.nexavault.model.FileMetadata;
import com.nexavault.model.FileAccessControl.AccessLevel;
import com.nexavault.service.FileAccessControlService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileAccessControlServiceImpl implements FileAccessControlService {

	private final FileAccessControlDao fileAccessControlDao;
	private final FileMetadataDao fileMetadataDao;

	@Override
	public void addAccess(FileMetadata file, String email, FileAccessControl.AccessLevel level) {
		FileAccessControl access = new FileAccessControl();
		access.setFile(file);
		access.setUserEmail(email);
		access.setAccessLevel(level);
		fileAccessControlDao.save(access);
	}

	@Override
	public boolean hasAccess(String ipfsHash, String email, FileAccessControl.AccessLevel level) {
		return fileAccessControlDao.existsByFileIpfsHashAndUserEmailAndAccessLevel(ipfsHash, email, level);
	}

	@Override
	public List<FileAccessControl> getFileAccess(String ipfsHash) {
		return fileAccessControlDao.findByFileIpfsHash(ipfsHash);
	}

	@Override
	public String shareFile(String ipfsHash, String email, AccessLevel level) {
		FileMetadata metadata = fileMetadataDao.findByIpfsHash(ipfsHash)
				.orElseThrow(() -> new FileOperationException("ipfsHash", "Failed to share - file not found"));

		addAccess(metadata, email, level);

		// Generate and return a link to view/download the image
		String baseUrl = "https://localhost:6000/api/ipfs/download/";
		return baseUrl + ipfsHash;
	}
}
