package com.nexavault.exception;

import org.springframework.http.HttpStatus;

public class EncryptionException extends GenericException {

	private static final long serialVersionUID = 1L;

	public EncryptionException(String fieldName, String message) {
		super(fieldName, HttpStatus.INTERNAL_SERVER_ERROR, message);
	}

	public EncryptionException(String message) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, message);
	}

}
