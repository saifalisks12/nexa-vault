package com.nexavault.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexavault.model.FileMetadata;

public interface FileMetadataDao extends JpaRepository<FileMetadata, String> {

	Optional<FileMetadata> findByIpfsHash(String ipfsHash);

	List<FileMetadata> findByTagsContaining(String tag);

	List<FileMetadata> findByDescriptionContaining(String description);

	List<FileMetadata> findByUploadedBy(String uploadedBy);

	List<FileMetadata> findByFileNameContainingIgnoreCaseOrTagsContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
			String fileName, String tags, String description);

	@Query("SELECT f FROM FileMetadata f WHERE " + "(:fileName IS NULL OR f.fileName LIKE %:fileName%) AND "
			+ "(:tag IS NULL OR f.tags LIKE %:tag%) AND "
			+ "(:description IS NULL OR f.description LIKE %:description%)")
	List<FileMetadata> findByCustomFilter(@Param("fileName") String fileName, @Param("tag") String tag,
			@Param("description") String description);
}