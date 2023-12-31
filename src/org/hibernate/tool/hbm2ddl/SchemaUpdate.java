//$Id$
package org.hibernate.tool.hbm2ddl;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.cfg.Settings;
import org.hibernate.dialect.Dialect;
import org.hibernate.pretty.DDLFormatter;
import org.hibernate.util.ReflectHelper;

/**
 * A commandline tool to update a database schema. May also be called from
 * inside an application.
 *
 * @author Christoph Sturm
 */
public class SchemaUpdate {

	private static final Log log = LogFactory.getLog( SchemaUpdate.class );
	private ConnectionHelper connectionHelper;
	private Configuration configuration;
	private Dialect dialect;
	private List exceptions;
	private boolean haltOnError = false;
	private boolean format = true;
	private String outputFile = null;
	private String delimiter;

	public SchemaUpdate(Configuration cfg) throws HibernateException {
		this( cfg, cfg.getProperties() );
	}

	public SchemaUpdate(Configuration cfg, Properties connectionProperties) throws HibernateException {
		this.configuration = cfg;
		dialect = Dialect.getDialect( connectionProperties );
		Properties props = new Properties();
		props.putAll( dialect.getDefaultProperties() );
		props.putAll( connectionProperties );
		connectionHelper = new ManagedProviderConnectionHelper( props );
		exceptions = new ArrayList();
	}

	public SchemaUpdate(Configuration cfg, Settings settings) throws HibernateException {
		this.configuration = cfg;
		dialect = settings.getDialect();
		connectionHelper = new SuppliedConnectionProviderConnectionHelper(
				settings.getConnectionProvider()
		);
		exceptions = new ArrayList();
	}

	public static void main(String[] args) {
		try {
			Configuration cfg = new Configuration();

			boolean script = true;
			// If true then execute db updates, otherwise just generate and display updates
			boolean doUpdate = true;
			String propFile = null;

			for ( int i = 0; i < args.length; i++ ) {
				if ( args[i].startsWith( "--" ) ) {
					if ( args[i].equals( "--quiet" ) ) {
						script = false;
					}
					else if ( args[i].startsWith( "--properties=" ) ) {
						propFile = args[i].substring( 13 );
					}
					else if ( args[i].startsWith( "--config=" ) ) {
						cfg.configure( args[i].substring( 9 ) );
					}
					else if ( args[i].startsWith( "--text" ) ) {
						doUpdate = false;
					}
					else if ( args[i].startsWith( "--naming=" ) ) {
						cfg.setNamingStrategy(
								( NamingStrategy ) ReflectHelper.classForName( args[i].substring( 9 ) ).newInstance()
						);
					}
				}
				else {
					cfg.addFile( args[i] );
				}

			}

			if ( propFile != null ) {
				Properties props = new Properties();
				props.putAll( cfg.getProperties() );
				props.load( new FileInputStream( propFile ) );
				cfg.setProperties( props );
			}

			new SchemaUpdate( cfg ).execute( script, doUpdate );
		}
		catch ( Exception e ) {
			log.error( "Error running schema update", e );
			e.printStackTrace();
		}
	}

	/**
	 * Execute the schema updates
	 *
	 * @param script print all DDL to the console
	 */
	public void execute(boolean script, boolean doUpdate) {

		log.info( "Running hbm2ddl schema update" );

		Connection connection = null;
		Statement stmt = null;
		Writer outputFileWriter = null;

		exceptions.clear();

		try {

			DatabaseMetadata meta;
			try {
				log.info( "fetching database metadata" );
				connectionHelper.prepare( true );
				connection = connectionHelper.getConnection();
				meta = new DatabaseMetadata( connection, dialect );
				stmt = connection.createStatement();
			}
			catch ( SQLException sqle ) {
				exceptions.add( sqle );
				log.error( "could not get database metadata", sqle );
				throw sqle;
			}

			log.info( "updating schema" );

			
			if ( outputFile != null ) {
				log.info( "writing generated schema to file: " + outputFile );
				outputFileWriter = new FileWriter( outputFile );
			}
			 
			String[] createSQL = configuration.generateSchemaUpdateScript( dialect, meta );
			for ( int j = 0; j < createSQL.length; j++ ) {

				final String sql = createSQL[j];
				String formatted = format( sql );
				try {
					if ( delimiter != null ) {
						formatted += delimiter;
					}
					if ( script ) {
						System.out.println( formatted );
					}
					if ( outputFile != null ) {
						outputFileWriter.write( formatted + "\n" );
					}
					if ( doUpdate ) {
						log.debug( sql );
						stmt.executeUpdate( formatted );
					}
				}
				catch ( SQLException e ) {
					if ( haltOnError ) {
						throw new JDBCException( "Error during DDL export", e );
					}
					exceptions.add( e );
					log.error( "Unsuccessful: " + sql );
					log.error( e.getMessage() );
				}
			}

			log.info( "schema update complete" );

		}
		catch ( Exception e ) {
			exceptions.add( e );
			log.error( "could not complete schema update", e );
		}
		finally {

			try {
				if ( stmt != null ) {
					stmt.close();
				}
				connectionHelper.release();
			}
			catch ( Exception e ) {
				exceptions.add( e );
				log.error( "Error closing connection", e );
			}
			try {
				if( outputFileWriter != null ) {
					outputFileWriter.close();
				}
			}
			catch(Exception e) {
				exceptions.add(e);
				log.error( "Error closing connection", e );
			}
		}
	}

	/**
	 * Returns a List of all Exceptions which occured during the export.
	 *
	 * @return A List containig the Exceptions occured during the export
	 */
	public List getExceptions() {
		return exceptions;
	}

	public void setHaltOnError(boolean haltOnError) {
		this.haltOnError = haltOnError;
	}

	public void setFormat(boolean format) {
		this.format = format;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	private String format(String sql) {
		return format ?
		       new DDLFormatter( sql ).format() :
		       sql;
	}
}
