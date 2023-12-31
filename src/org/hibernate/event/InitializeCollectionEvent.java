//$Id$
package org.hibernate.event;

import org.hibernate.collection.PersistentCollection;

/**
 * An event that occurs when a collection wants to be
 * initialized
 * 
 * @author Gavin King
 */
public class InitializeCollectionEvent extends AbstractCollectionEvent {

	public InitializeCollectionEvent(PersistentCollection collection, EventSource source ) {
		super( getLoadedCollectionPersister( collection, source ),
				collection,
				source,
				getLoadedOwnerOrNull( collection, source ),
				getLoadedOwnerIdOrNull( collection, source ) );
	}
}
