package com.multi.enterprise.types.exception;

import java.net.HttpURLConnection;

public class IllegalArgumentServiceException extends ServiceException{

	private static final long serialVersionUID = 1L;
	public static final int HTTP_STATUS = HttpURLConnection.HTTP_BAD_REQUEST;
	
	public IllegalArgumentServiceException(String message) {
		super(message);
	}

	public IllegalArgumentServiceException(String message, Throwable throwable){
		super(message,throwable);
	}

	@Override
	public int getHttpStatus(){
		return HTTP_STATUS;
	}
	
}
