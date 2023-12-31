//$Id$
package org.hibernate.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.cfg.Environment;
import org.hibernate.util.CalendarComparator;

/**
 * <tt>calendar</tt>: A type mapping for a <tt>Calendar</tt> object that
 * represents a datetime.
 * @author Gavin King
 */
public class CalendarType extends MutableType implements VersionType {

	public Object get(ResultSet rs, String name) throws HibernateException, SQLException {

		Timestamp ts = rs.getTimestamp(name);
		if (ts!=null) {
			Calendar cal = new GregorianCalendar();
			if ( Environment.jvmHasTimestampBug() ) {
				cal.setTime( new Date( ts.getTime() + ts.getNanos() / 1000000 ) );
			}
			else {
				cal.setTime(ts);
			}
			return cal;
		}
		else {
			return null;
		}

	}

	public void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		final Calendar cal = (Calendar) value;
		//st.setTimestamp( index,  new Timestamp( cal.getTimeInMillis() ), cal ); //JDK 1.5 only
		st.setTimestamp( index,  new Timestamp( cal.getTime().getTime() ), cal );
	}

	public int sqlType() {
		return Types.TIMESTAMP;
	}

	public String toString(Object value) throws HibernateException {
		return Hibernate.TIMESTAMP.toString( ( (Calendar) value ).getTime() );
	}

	public Object fromStringValue(String xml) throws HibernateException {
		Calendar result = new GregorianCalendar();
		result.setTime( ( (Date) Hibernate.TIMESTAMP.fromStringValue(xml) ) );
		return result;
	}

	public Object deepCopyNotNull(Object value) throws HibernateException {
		return ( (Calendar) value ).clone();
	}

	public Class getReturnedClass() {
		return Calendar.class;
	}
	
	public int compare(Object x, Object y, EntityMode entityMode) {
		return CalendarComparator.INSTANCE.compare(x, y);
	}

	public boolean isEqual(Object x, Object y) {
		if (x==y) return true;
		if (x==null || y==null) return false;

		Calendar calendar1 = (Calendar) x;
		Calendar calendar2 = (Calendar) y;

		return calendar1.get(Calendar.MILLISECOND) == calendar2.get(Calendar.MILLISECOND)
			&& calendar1.get(Calendar.SECOND) == calendar2.get(Calendar.SECOND)
			&& calendar1.get(Calendar.MINUTE) == calendar2.get(Calendar.MINUTE)
			&& calendar1.get(Calendar.HOUR_OF_DAY) == calendar2.get(Calendar.HOUR_OF_DAY)
			&& calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)
			&& calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
			&& calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
	}

	public int getHashCode(Object x, EntityMode entityMode) {
		Calendar calendar = (Calendar) x;
		int hashCode = 1;
		hashCode = 31 * hashCode + calendar.get(Calendar.MILLISECOND);
		hashCode = 31 * hashCode + calendar.get(Calendar.SECOND);
		hashCode = 31 * hashCode + calendar.get(Calendar.MINUTE);
		hashCode = 31 * hashCode + calendar.get(Calendar.HOUR_OF_DAY);
		hashCode = 31 * hashCode + calendar.get(Calendar.DAY_OF_MONTH);
		hashCode = 31 * hashCode + calendar.get(Calendar.MONTH);
		hashCode = 31 * hashCode + calendar.get(Calendar.YEAR);
		return hashCode;
	}

	public String getName() {
		return "calendar";
	}

	public Object next(Object current, SessionImplementor session) {
		return seed( session );
	}

	public Object seed(SessionImplementor session) {
		return Calendar.getInstance();
	}

	public Comparator getComparator() {
		return CalendarComparator.INSTANCE;
	}

}
