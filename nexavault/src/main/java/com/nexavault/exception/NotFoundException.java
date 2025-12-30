package com.nexavault.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends GenericException {

	private static final long serialVersionUID = 1L;

	public NotFoundException(String fieldName, String message) {
		super(fieldName, HttpStatus.CONFLICT, message);
	}
}