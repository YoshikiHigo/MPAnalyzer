package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;

public abstract class DAO {

	final protected Connection connector;

	static public final String REVISION_SCHEMA = "number integer, date string, message string";
	static public final String CODEFRAGMENT_SCHEMA = "text string, hash integer";
	static public final String MODIFICATION_SCHEMA = "id integer primary key autoincrement, filepath string, beforeText string, beforeHash integer, afterText string, afterHash integer, revision integer, type integer";
	static public final String PATTERN_SCHEMA = "id integer primary key autoincrement, beforeHash integer, afterHash integer, type integer, support integer, confidence real";
	static public final String PROBLEM_SCHEMA = "filepath string, start int, end int, problempattern string, problemID int, presentCode string, proposedCode string";
	static public final String CLONE_SCHEMA = "filepath string, start inteter, end integer, revision integer, setID";

	DAO(final boolean createRevisionTable,
			final boolean createCodeFragmentTable,
			final boolean createModificationTable,
			final boolean createPatternTable, final boolean createProblemTable,
			final boolean createCloneTable) throws Exception {

		final String databaseLocation = Config.getDATABASELOCATION();
		final String databaseName = Config.getDATABASENAME();

		Class.forName("org.sqlite.JDBC");

		final StringBuilder url = new StringBuilder();
		url.append("jdbc:sqlite:");
		url.append(databaseLocation);
		if (url.charAt(url.length() - 1) != File.separatorChar) {
			url.append(File.separator);
		}
		url.append(databaseName);
		this.connector = DriverManager.getConnection(url.toString());

		final Statement statement = this.connector.createStatement();

		statement.executeUpdate("create table if not exists revision ("
				+ REVISION_SCHEMA + ")");
		statement.executeUpdate("create table if not exists codefragment ("
				+ CODEFRAGMENT_SCHEMA + ")");
		statement.executeUpdate("create table if not exists modification ("
				+ MODIFICATION_SCHEMA + ")");
		statement.executeUpdate("create table if not exists pattern ("
				+ PATTERN_SCHEMA + ")");
		statement.executeUpdate("create table if not exists problem ("
				+ PROBLEM_SCHEMA + ")");

		if (createRevisionTable) {
			statement.executeUpdate("drop table if exists revision");
			statement.executeUpdate("create table revision (" + REVISION_SCHEMA
					+ ")");
			statement
					.executeUpdate("create index numberIndex on revision (number)");
		}
		if (createCodeFragmentTable) {
			statement.executeUpdate("drop table if exists codefragment");
			statement.executeUpdate("create table codefragment ("
					+ CODEFRAGMENT_SCHEMA + ")");
			statement
					.executeUpdate("create index textIndex on codefragment (text)");
			statement
					.executeUpdate("create index hashIndex on codefragment (hash)");
		}
		if (createModificationTable) {
			statement.executeUpdate("drop table if exists modification");
			statement.executeUpdate("create table modification ("
					+ MODIFICATION_SCHEMA + ")");
			statement
					.executeUpdate("create index idIndex on modification (id)");
			statement
					.executeUpdate("create index beforeHashIndex on modification (beforeHash)");
			statement
					.executeUpdate("create index afterHashIndex on modification (afterHash)");
		}
		if (createPatternTable) {
			statement.executeUpdate("drop table if exists pattern");
			statement.executeUpdate("create table pattern (" + PATTERN_SCHEMA
					+ ")");
			statement.executeUpdate("create index typeIndex on pattern (type)");
			statement
					.executeUpdate("create index supportIndex on pattern (support)");
			statement
					.executeUpdate("create index confidenceIndex on pattern (confidence)");
		}
		if (createProblemTable) {
			statement.executeUpdate("drop table if exists problem");
			statement.executeUpdate("create table problem (" + PROBLEM_SCHEMA
					+ ")");
			statement
					.executeUpdate("create index problemIDIndex on problem (problemID)");
		}
		if (createCloneTable) {
			statement.executeUpdate("drop table if exists problem");
			statement.executeUpdate("create table clone (" + CLONE_SCHEMA
					+ ")");
			statement.executeUpdate("create index setIDIndex on clone (setID)");
		}
		statement.close();
	}

	public void close() throws Exception {
		this.connector.close();
	}
}
