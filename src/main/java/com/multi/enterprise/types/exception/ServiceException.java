package com.multi.enterprise.types.exception;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ServiceException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int HTTP_STATUS = HttpURLConnection.HTTP_INTERNAL_ERROR;

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public int getHttpStatus() {
		return HTTP_STATUS;
	}

	public static ServiceException valueOf(int httpStatus, String message) {
		switch (httpStatus) {
		case IllegalArgumentServiceException.HTTP_STATUS:
			return new IllegalArgumentServiceException(message);
		case ServiceException.HTTP_STATUS:
			return new ServiceException(message);
		case EntityNotFoundException.HTTP_STATUS:
			return new EntityNotFoundException(message);
		default:
			return null;
		}
	}
}
