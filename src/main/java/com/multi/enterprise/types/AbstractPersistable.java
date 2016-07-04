/**
 * 
 */
package com.multi.enterprise.types;

import java.util.Date;

/**
 * @author Robot
 *
 */
public abstract class AbstractPersistable implements Persistable {

	protected String id;
	private Long casValue;
	private Date createdDate;
	private Date modifiedDate;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the casValue
	 */
	public Long getCasValue() {
		return casValue;
	}

	/**
	 * @param casValue
	 *            the casValue to set
	 */
	public void setCasValue(Long casValue) {
		this.casValue = casValue;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate
	 *            the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the modifiedDate
	 */
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * @param modifiedDate
	 *            the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

}
