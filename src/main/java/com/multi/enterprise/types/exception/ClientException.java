package com.multi.enterprise.types.exception;

import java.io.IOException;

public class ClientException extends IOException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClientException( String message ){
		super(message);
	}
	
	public ClientException(String message, Throwable throwable ){
		super(message,throwable);
	}
	
}
