//$Id$
package org.hibernate.test.idbag;

/**
 * @author Gavin King
 */
public class Group {
	private String name;
	
	Group() {}
	
	public Group(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	void setName(String name) {
		this.name = name;
	}
	
}
