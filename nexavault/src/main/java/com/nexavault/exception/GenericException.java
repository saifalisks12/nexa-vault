package com.nexavault.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class GenericException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String fieldName;
    private final HttpStatus httpStatusCode;

    /**
     * Constructs a new GenericException with the specified field name, message, and HTTP error code.
     *
     * @param fieldName  The name of the field associated with the exception.
     * @param message    The error message.
     * @param httpStatusCode The HTTP error code.
     */
    public GenericException(final String fieldName, final HttpStatus httpStatusCode, final String message) {
        super(message);
        this.fieldName = fieldName;
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Constructs a new GenericException with no field name, just a message and HTTP error code.
     *
     * @param httpStatusCode The HTTP error code.
     * @param message        The error message.
     */
    public GenericException(final HttpStatus httpStatusCode, final String message) {
        super(message);
        this.fieldName = null;
        this.httpStatusCode = httpStatusCode;
    }
}