//$Id$
package org.hibernate.loader;

import java.util.List;
import java.util.ArrayList;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.BagType;

/**
 * Uses the default mapping from property to result set column 
 * alias defined by the entities' persisters. Used when Hibernate
 * is generating result set column aliases.
 * 
 * @author Gavin King
 */
public abstract class BasicLoader extends Loader {

	protected static final String[] NO_SUFFIX = {""};

	private EntityAliases[] descriptors;
	private CollectionAliases[] collectionDescriptors;

	public BasicLoader(SessionFactoryImplementor factory) {
		super(factory);
	}

	protected final EntityAliases[] getEntityAliases() {
		return descriptors;
	}

	protected final CollectionAliases[] getCollectionAliases() {
		return collectionDescriptors;
	}

	protected abstract String[] getSuffixes();
	protected abstract String[] getCollectionSuffixes();

	protected void postInstantiate() {
		Loadable[] persisters = getEntityPersisters();
		String[] suffixes = getSuffixes();
		descriptors = new EntityAliases[persisters.length];
		for ( int i=0; i<descriptors.length; i++ ) {
			descriptors[i] = new DefaultEntityAliases( persisters[i], suffixes[i] );
		}

		CollectionPersister[] collectionPersisters = getCollectionPersisters();
		List bagRoles = null;
		if ( collectionPersisters != null ) {
			String[] collectionSuffixes = getCollectionSuffixes();
			collectionDescriptors = new CollectionAliases[collectionPersisters.length];
			for ( int i = 0; i < collectionPersisters.length; i++ ) {
				if ( isBag( collectionPersisters[i] ) ) {
					if ( bagRoles == null ) {
						bagRoles = new ArrayList();
					}
					bagRoles.add( collectionPersisters[i].getRole() );
				}
				collectionDescriptors[i] = new GeneratedCollectionAliases(
						collectionPersisters[i],
						collectionSuffixes[i]
					);
			}
		}
		else {
			collectionDescriptors = null;
		}
		if ( bagRoles != null && bagRoles.size() > 1 ) {
			throw new MultipleBagFetchException( bagRoles );
		}
	}

	private boolean isBag(CollectionPersister collectionPersister) {
		return collectionPersister.getCollectionType().getClass().isAssignableFrom( BagType.class );
	}

	/**
	 * Utility method that generates 0_, 1_ suffixes. Subclasses don't
	 * necessarily need to use this algorithm, but it is intended that
	 * they will in most cases.
	 * <p/>
	 * This form simply calls {@link #generateSuffixes(int, int) generateSuffixes(0,length}
	 *
	 * @param length The number of suffixes to generate
	 *
	 * @return The array of generated suffixes; the array length = length.
	 */
	public static String[] generateSuffixes(int length) {
		return generateSuffixes( 0, length );
	}

	/**
	 * Utility method that generates alias suffixes.
	 *
	 * @param seed The number from which to begin the suffix sequencing.  For example,
	 * a seed of 0 would return 0_ as the first suffix; a seed of 5 would return 5_ as
	 * the first suffix.
	 * @param length The number of suffixes to generate
	 *
	 * @return The array of generated suffixes; the array length = length.
	 */
	public static String[] generateSuffixes(int seed, int length) {
		if ( length == 0 ) {
			return NO_SUFFIX;
		}

		String[] suffixes = new String[length];
		for ( int i = 0; i < length; i++ ) {
			suffixes[i] = Integer.toString( i + seed ) + "_";
		}
		return suffixes;
	}

}
