package yoshikihigo.cpanalyzer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Change.ChangeType;
import yoshikihigo.cpanalyzer.data.Change.DiffType;
import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.data.Code;
import yoshikihigo.cpanalyzer.data.Revision;

public class ReadOnlyDAO {

	static private ReadOnlyDAO SINGLETON = null;

	static public ReadOnlyDAO getInstance() throws Exception {
		if (null == SINGLETON) {
			SINGLETON = new ReadOnlyDAO();
		}
		return SINGLETON;
	}

	static public void deleteInstance() throws Exception {
		if (null != SINGLETON) {
			SINGLETON.clone();
			SINGLETON = null;
		}
	}

	private Connection connector;

	private ReadOnlyDAO() {

		try {
			Class.forName("org.sqlite.JDBC");
			final String database = CPAConfig.getInstance().getDATABASE();
			this.connector = DriverManager.getConnection("jdbc:sqlite:"
					+ database);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public List<Change> getChanges(final byte[] beforeHash,
			final byte[] afterHash) {

		final List<Change> changes = new ArrayList<Change>();

		try {
			final StringBuilder text = new StringBuilder();
			text.append("select T.software, T.id, T.filepath, T.beforeHash, T.beforeText, ");
			text.append("T.beforeStart, T.beforeEnd, T.afterHash, T.afterText, ");
			text.append("T.afterStart, T.afterEnd, T.revision, T.changetype, T.difftype, T.date, T.message from ");
			text.append("(select M.software software, M.id id, M.filepath filepath, M.beforeHash beforeHash, ");
			text.append("(select C2.text from codes C2 where C2.id = M.beforeID) beforeText, ");
			text.append("(select C3.start from codes C3 where C3.id = M.beforeID) beforeStart, ");
			text.append("(select C4.end from codes C4 where C4.id = M.beforeID) beforeEnd, ");
			text.append("M.afterHash afterHash, ");
			text.append("(select C6.text from codes C6 where C6.id = M.afterID) afterText, ");
			text.append("(select C7.start from codes C7 where C7.id = M.afterID) afterStart, ");
			text.append("(select C8.end from codes C8 where C8.id = M.afterID) afterEnd, ");
			text.append("M.revision revision, ");
			text.append("M.changetype changetype, ");
			text.append("M.difftype difftype, ");
			text.append("(select R1.date from revisions R1 where R1.number = M.revision) date, ");
			text.append("(select R2.message from revisions R2 where R2.number = M.revision) message ");
			text.append("from changes M) T where T.beforeHash=? and T.afterHash=?");
			final PreparedStatement statement = this.connector
					.prepareStatement(text.toString());

			statement.setBytes(1, beforeHash);
			statement.setBytes(2, afterHash);
			final ResultSet result = statement.executeQuery();

			while (result.next()) {
				final String software = result.getString(1);
				final int id = result.getInt(2);
				final String filepath = result.getString(3);
				final int beforeID = result.getInt(4);
				final String beforeText = result.getString(5);
				final int beforeStart = result.getInt(6);
				final int beforeEnd = result.getInt(7);
				final int afterID = result.getInt(8);
				final String afterText = result.getString(9);
				final int afterStart = result.getInt(10);
				final int afterEnd = result.getInt(11);
				final long number = result.getLong(12);
				final ChangeType changeType = ChangeType.getType(result
						.getInt(13));
				final DiffType diffType = DiffType.getType(result.getInt(14));
				final String date = result.getString(15);
				final String message = result.getString(16);
				final Change change = new Change(software, id, filepath,
						new Code(software, beforeID, beforeText, beforeStart,
								beforeEnd), new Code(software, afterID,
								afterText, afterStart, afterEnd), new Revision(
								software, number, date, message), changeType,
						diffType);
				changes.add(change);
			}
			statement.close();
		}

		catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		return changes;
	}

	public List<ChangePattern> getChangePatterns(final int supportThreshold,
			final float confidenceThreshold) {

		final List<ChangePattern> patterns = new ArrayList<ChangePattern>();

		try {
			final StringBuilder text = new StringBuilder();
			text.append("select id, beforeHash, afterHash, changetype, difftype, support, confidence");
			text.append(" from patterns where ? <= support and ? <= confidence");
			final PreparedStatement statement = this.connector
					.prepareStatement(text.toString());

			statement.setInt(1, supportThreshold);
			statement.setFloat(2, confidenceThreshold);
			final ResultSet result = statement.executeQuery();

			while (result.next()) {
				final int id = result.getInt(1);
				final byte[] beforeHash = result.getBytes(2);
				final byte[] afterHash = result.getBytes(3);
				final ChangeType changeType = ChangeType.getType(result
						.getInt(4));
				final DiffType diffType = DiffType.getType(result.getInt(5));
				final int support = result.getInt(6);
				final float confidence = result.getFloat(7);
				final ChangePattern pattern = new ChangePattern(id, support,
						confidence, beforeHash, afterHash, changeType, diffType);
				patterns.add(pattern);
			}

			statement.close();
		}

		catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		return patterns;
	}

	public SortedSet<Revision> getRevisions() throws Exception {

		final Statement revisionStatement = this.connector.createStatement();
		final ResultSet result = revisionStatement
				.executeQuery("select software, number, date, message from revision");

		final SortedSet<Revision> revisions = new TreeSet<Revision>();
		while (result.next()) {
			final String software = result.getString(1);
			final long number = result.getLong(2);
			final String date = result.getString(3);
			final String message = result.getString(4);
			final Revision revision = new Revision(software, number, date,
					message);
			revisions.add(revision);
		}

		return revisions;
	}

	public void close() {
		try {
			this.connector.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
