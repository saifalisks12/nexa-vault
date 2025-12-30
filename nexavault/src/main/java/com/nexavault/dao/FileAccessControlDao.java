package com.nexavault.dao;

import com.nexavault.model.FileAccessControl;
import com.nexavault.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileAccessControlDao extends JpaRepository<FileAccessControl, String> {
    boolean existsByFileIpfsHashAndUserEmailAndAccessLevel(String ipfsHash, String email,
                                                           FileAccessControl.AccessLevel level);

    List<FileAccessControl> findByFileIpfsHash(String ipfsHash);

    @Modifying
    @Transactional
    void deleteByFile(FileMetadata file);

    @Query("""
                select fac.file
                from FileAccessControl fac
                where fac.userEmail = :userEmail
            """)
    List<FileMetadata> findFilesSharedWithUser(@Param("userEmail") String userEmail);
}