/**
 * 
 */
package com.multi.enterprise.types;

import java.util.Date;

/**
 * @author Robot
 *
 */
public interface Persistable {
	public String getId();

	public void setId(String id);

	public Long getCasValue();

	public void setCasValue(Long casValue);

	public Date getCreatedDate();

	public void setCreatedDate(Date createdDate);

	public Date getModifiedDate();

	public void setModifiedDate(Date modifiedDate);

}
