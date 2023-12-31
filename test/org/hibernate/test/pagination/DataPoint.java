//$Id$
package org.hibernate.test.pagination;

import java.math.BigDecimal;

/**
 * @author Gavin King
 */
public class DataPoint {
	private long id;
	private int sequence;
	private BigDecimal x;
	private BigDecimal y;
	private String description;

	/**
	 * @return Returns the id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Getter for property 'sequence'.
	 *
	 * @return Value for property 'sequence'.
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * Setter for property 'sequence'.
	 *
	 * @param sequence Value to set for property 'sequence'.
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the x.
	 */
	public BigDecimal getX() {
		return x;
	}

	/**
	 * @param x The x to set.
	 */
	public void setX(BigDecimal x) {
		this.x = x;
	}

	/**
	 * @return Returns the y.
	 */
	public BigDecimal getY() {
		return y;
	}

	/**
	 * @param y The y to set.
	 */
	public void setY(BigDecimal y) {
		this.y = y;
	}
}
