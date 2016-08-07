/**
 * 
 */
package com.multi.enterprise.types.service;

import com.multi.enterprise.types.Persistable;
import com.multi.enterprise.types.exception.ServiceException;

/**
 * @author Robot
 *
 */
public interface RecordService<T extends Persistable> {

	public Class<T> entityClass();

	public T create(T entity) throws ServiceException;

	public T getById(String id) throws ServiceException;

	public T update(T entity) throws ServiceException;

	public void delete(String id) throws ServiceException;

}
