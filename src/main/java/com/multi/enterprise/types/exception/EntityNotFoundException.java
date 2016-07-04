/**
 * 
 */
package com.multi.enterprise.types.exception;

import java.net.HttpURLConnection;

/**
 * @author Robot
 *
 */
public class EntityNotFoundException extends ServiceException {

	private static final long serialVersionUID = 1L;
	public static final int HTTP_STATUS = HttpURLConnection.HTTP_NOT_FOUND;

	/**
	 * @param message
	 */
	public EntityNotFoundException(String message) {
		super(message);
	}

	public EntityNotFoundException(String message, Throwable th) {
		super(message, th);
	}

	@Override
	public int getHttpStatus() {
		return HTTP_STATUS;
	}

}
