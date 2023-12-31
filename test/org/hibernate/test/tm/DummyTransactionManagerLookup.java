//$Id$
package org.hibernate.test.tm;

import java.util.Properties;

import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 * @author Gavin King
 */
public class DummyTransactionManagerLookup implements TransactionManagerLookup {

	public TransactionManager getTransactionManager(Properties props)
	throws HibernateException {
		if ( DummyTransactionManager.INSTANCE == null ) {
			DummyTransactionManager.INSTANCE = new DummyTransactionManager(props);
		}
		return DummyTransactionManager.INSTANCE;
	}

	public String getUserTransactionName() {
		throw new UnsupportedOperationException();
	}

}
