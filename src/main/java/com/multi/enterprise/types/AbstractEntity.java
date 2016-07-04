/**
 * 
 */
package com.multi.enterprise.types;

import java.util.Objects;

/**
 * @author Robot
 *
 */
public abstract class AbstractEntity extends AbstractPersistable implements Entity {

	protected String revisionId;

	/**
	 * @return the revisionId
	 */
	@Override
	public String getRevisionId() {
		return revisionId;
	}

	/**
	 * @param revisionId the revisionId to set
	 */
	@Override
	public void setRevisionId(String revisionId) {
		this.revisionId = revisionId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!super.equals(obj)) {
			return false;
		}

		final AbstractEntity other = (AbstractEntity) obj;
		if (!Objects.equals(this.revisionId, other.revisionId)) {
			return false;
		}

		return true;
	}

}
