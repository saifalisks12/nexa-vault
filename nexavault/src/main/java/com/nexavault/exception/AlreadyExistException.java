package com.nexavault.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistException extends GenericException {

	private static final long serialVersionUID = 1L;

	public AlreadyExistException(String fieldName, String message) {
		super(fieldName, HttpStatus.CONFLICT, message);
	}
}

