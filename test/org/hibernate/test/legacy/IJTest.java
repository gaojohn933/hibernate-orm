//$Id$
package org.hibernate.test.legacy;

import java.io.Serializable;

import junit.framework.Test;

import org.hibernate.LockMode;
import org.hibernate.classic.Session;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.junit.functional.FunctionalTestClassTestSuite;

/**
 * @author Gavin King
 */
public class IJTest extends LegacyTestCase {

	public IJTest(String x) {
		super(x);
	}

	public String[] getMappings() {
		return new String[] { "legacy/IJ.hbm.xml" };
	}

	public static Test suite() {
		return new FunctionalTestClassTestSuite( IJTest.class );
	}

	public void testFormulaDiscriminator() throws Exception {
		if ( getDialect() instanceof HSQLDialect ) return;
		Session s = getSessions().openSession();
		I i = new I();
		i.setName( "i" );
		i.setType( 'a' );
		J j = new J();
		j.setName( "j" );
		j.setType( 'x' );
		j.setAmount( 1.0f );
		Serializable iid = s.save(i);
		Serializable jid = s.save(j);
		s.flush();
		s.connection().commit();
		s.close();

		getSessions().evict(I.class);

		s = getSessions().openSession();
		j = (J) s.get(I.class, jid);
		i = (I) s.get(I.class, iid);
		assertTrue( i.getClass()==I.class );
		j.setAmount( 0.5f );
		s.lock(i, LockMode.UPGRADE);
		s.flush();
		s.connection().commit();
		s.close();

		s = getSessions().openSession();
		j = (J) s.get(I.class, jid, LockMode.UPGRADE);
		i = (I) s.get(I.class, iid, LockMode.UPGRADE);
		s.flush();
		s.connection().commit();
		s.close();

		s = getSessions().openSession();
		assertTrue( s.find("from I").size()==2 );
		assertTrue( s.find("from J").size()==1 );
		assertTrue( s.find("from I i where i.class = 0").size()==1 );
		assertTrue( s.find("from I i where i.class = 1").size()==1 );
		s.connection().commit();
		s.close();

		s = getSessions().openSession();
		j = (J) s.get(J.class, jid);
		i = (I) s.get(I.class, iid);
		s.delete(j);
		s.delete(i);
		s.flush();
		s.connection().commit();
		s.close();

	}
}
