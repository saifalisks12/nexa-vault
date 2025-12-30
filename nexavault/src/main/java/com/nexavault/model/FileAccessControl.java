package com.nexavault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_access_control")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileAccessControl {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne
	@JoinColumn(name = "file_id", nullable = false)
	private FileMetadata file;

	@Column(nullable = false)
	private String userEmail;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccessLevel accessLevel;

	public enum AccessLevel {
		VIEWER, EDITOR, DOWNLOAD, ADMIN
	}
}