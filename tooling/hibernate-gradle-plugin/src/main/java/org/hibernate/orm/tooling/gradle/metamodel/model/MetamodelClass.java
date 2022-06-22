/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.tooling.gradle.metamodel.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.gradle.api.GradleException;

import static java.lang.Character.LINE_SEPARATOR;

/**
 * Descriptor for a class in the JPA static metamodel being generated
 */
public class MetamodelClass {

	private final String domainClassName;
	private final String metamodelClassName;

	private final String metamodelSuperClassName;

	private final TreeSet<MetamodelAttribute> attributes = new TreeSet<>( Comparator.comparing( MetamodelAttribute::getName ) );

	public MetamodelClass(String domainClassName, String superTypeName) {
		this.domainClassName = domainClassName;
		this.metamodelClassName = domainClassName + "_";
		this.metamodelSuperClassName = superTypeName == null ? null : superTypeName + "_";
	}

	public String getMetamodelClassName() {
		return metamodelClassName;
	}

	public String getDomainClassName() {
		return domainClassName;
	}

	public void addAttribute(MetamodelAttribute attribute) {
		assert attribute != null;
		attributes.add( attribute );
	}

	public void writeToFile(File outputFile, GenerationOptions options) {
		prepareOutputFile( outputFile );

		final Path path = outputFile.toPath();

		try ( final BufferedWriter writer = Files.newBufferedWriter( path, StandardCharsets.UTF_8, StandardOpenOption.WRITE ) ) {

			renderClassPreamble( writer, options );
			writer.write( LINE_SEPARATOR );

			writer.write( "public abstract class " + metamodelClassName );
			if ( metamodelSuperClassName != null ) {
				writer.write( " extends " + metamodelSuperClassName );
			}
			writer.write( " {" );
			writer.write( LINE_SEPARATOR );
			writer.write( LINE_SEPARATOR );

			writer.write( "    // Attribute name constants" );
			writer.write( LINE_SEPARATOR );
			attributes.forEach( attribute -> attribute.renderNameConstant( writer ) );
			writer.write( LINE_SEPARATOR );
			writer.write( LINE_SEPARATOR );

			writer.write( "    // JPA static metamodel fields" );
			writer.write( LINE_SEPARATOR );
			attributes.forEach( attribute -> attribute.renderJpaMembers( writer ) );
			writer.write( LINE_SEPARATOR );

			writer.write( "}" );
			writer.write( LINE_SEPARATOR );
		}
		catch (IOException e) {
			throw new IllegalStateException( "Unable to open file : " + outputFile.getAbsolutePath(), e );
		}
	}

	private void renderClassPreamble(BufferedWriter writer, GenerationOptions options) throws IOException {
		final String nowFormatted = DateFormat.getDateInstance().format( new Date() );

		writer.write( "// Generated by Hibernate ORM Gradle tooling - " + nowFormatted );
		writer.write( LINE_SEPARATOR );
		writer.write( LINE_SEPARATOR );

		writer.write( "import jakarta.persistence.*;" );
		writer.write( LINE_SEPARATOR );
		writer.write( "import jakarta.persistence.metamodel.*;" );
		writer.write( LINE_SEPARATOR );
		writer.write( LINE_SEPARATOR );

		writer.write( "/** JPA static metamodel descriptor for the `" + domainClassName + "` domain class */" );
		writer.write( LINE_SEPARATOR );

		// first, the generated annotation
		if ( options.getApplyGeneratedAnnotation().getOrElse( true ) ) {
			final String generatedAnnotationFragment = String.format(
					Locale.ROOT,
					"@jakarta.annotation.Generated( value=\"%s\", date=\"%s\", comments=\"%s\" )",
					JpaStaticMetamodelGenerator.class.getName(),
					nowFormatted,
					"Generated by Hibernate ORM Gradle tooling"
			);
			writer.write( generatedAnnotationFragment );
			writer.write( LINE_SEPARATOR );
		}

		final Set<String> suppressions = options.getSuppressions().getOrElse( Collections.emptySet() );
		if ( ! suppressions.isEmpty() ) {
			writer.write( "@SuppressWarnings( { " );
			for ( String suppression : suppressions ) {
				writer.write( "\"" + suppression + "\", " );
			}
			writer.write( " } )" );
			writer.write( LINE_SEPARATOR );
		}

		writer.write( "@StaticMetamodel( " + domainClassName + ".class )" );
	}

	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	public void prepareOutputFile(File outputFile) {
		try {
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
		}
		catch (IOException e) {
			throw new GradleException( "Unable to prepare output file `" + outputFile.getAbsolutePath() + "`", e );
		}
	}
}