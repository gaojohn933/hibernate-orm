// $Id$
package org.hibernate.hql.ast.util;

/**
 * Marker interface for nodes (AST impls) wanting to display extra information during {@link ASTPrinter} processing.  The
 * extra display text is output in addition to the node type and text.
 */
public interface DisplayableNode {
	/**
	 * Returns additional display text for the AST node.
	 *
	 * @return The additional display text.
	 */
	public String getDisplayText();
}
