package com.nexavault.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.nexavault.dao.FileAccessControlDao;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexavault.dao.FileMetadataDao;
import com.nexavault.exception.FileOperationException;
import com.nexavault.exception.NotFoundException;
import com.nexavault.model.FileMetadata;
import com.nexavault.responsedto.UploadFileResponseDto;
import com.nexavault.service.FileMetadataService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileMetadataServiceImpl implements FileMetadataService {

    private final FileMetadataDao fileMetadataDao;
    private final FileAccessControlDao fileAccessControlDao;

    @Override
    public List<FileMetadata> allFile() {
        return fileMetadataDao.findAll();
    }

    @Override
    public FileMetadata getFileByHash(String ipfsHash) {
        return fileMetadataDao.findByIpfsHash(ipfsHash)
                .orElseThrow(() -> new NotFoundException("ipfsHash", "File not found for hash"));
    }

    @Override
    public List<FileMetadata> searchFiles(String fileName, String tag, String description) {
        if ((fileName == null || fileName.isEmpty()) && (tag == null || tag.isEmpty())
                && (description == null || description.isEmpty())) {
            return fileMetadataDao.findAll();
        }

        return fileMetadataDao.findByCustomFilter(fileName, tag, description);
    }

    @Override
    public List<FileMetadata> getFilesByUser(String username) {

        // Files owned by the user
        List<FileMetadata> ownedFiles =
                fileMetadataDao.findByUploadedBy(username);

        // Files shared with the user
        List<FileMetadata> sharedFiles =
                fileAccessControlDao.findFilesSharedWithUser(username);

        // Merge results (avoid duplicates)
        Set<FileMetadata> allFiles = new LinkedHashSet<>();
        allFiles.addAll(ownedFiles);
        allFiles.addAll(sharedFiles);

        return new ArrayList<>(allFiles);
    }

    @Override
    @Transactional
    public FileMetadata updateFileMetadata(String ipfsHash, UploadFileResponseDto request) {
        FileMetadata fileMetadata = getFileByHash(ipfsHash);
        validateOwnership(fileMetadata);

        if (request.getTags() != null) {
            fileMetadata.setTags(request.getTags());
        }
        if (request.getDescription() != null) {
            fileMetadata.setDescription(request.getDescription());
        }
        return fileMetadataDao.save(fileMetadata);
    }

    @Override
    @Transactional
    public void deleteFileMetadata(String ipfsHash) {
        FileMetadata fileMetadata = getFileByHash(ipfsHash);
        validateOwnership(fileMetadata);
        fileAccessControlDao.deleteByFile(fileMetadata);
        fileMetadataDao.delete(fileMetadata);
    }

    // Helper method to check if the user is the uploader
    private void validateOwnership(FileMetadata fileMetadata) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!fileMetadata.getUploadedBy().equals(currentUser)) {
            throw new FileOperationException("Unauthorized", "You don't have permission to modify this file.");
        }
    }
}