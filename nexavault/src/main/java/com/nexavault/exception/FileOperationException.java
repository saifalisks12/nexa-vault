package com.nexavault.exception;

import org.springframework.http.HttpStatus;

public class FileOperationException extends GenericException {

	private static final long serialVersionUID = 1L;

	public FileOperationException(String fieldName, String message) {
		super(fieldName, HttpStatus.INTERNAL_SERVER_ERROR, message);
	}
	
	public FileOperationException(String message) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, message);
	}
	
	
}
