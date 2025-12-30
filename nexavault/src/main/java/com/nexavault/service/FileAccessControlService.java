package com.nexavault.service;

import com.nexavault.model.FileAccessControl;
import com.nexavault.model.FileAccessControl.AccessLevel;
import com.nexavault.model.FileMetadata;

import java.util.List;

public interface FileAccessControlService {
	void addAccess(FileMetadata file, String email, FileAccessControl.AccessLevel level);

	boolean hasAccess(String ipfsHash, String email, FileAccessControl.AccessLevel level);

	List<FileAccessControl> getFileAccess(String ipfsHash);

	String shareFile(String ipfsHash, String email, AccessLevel level);
}