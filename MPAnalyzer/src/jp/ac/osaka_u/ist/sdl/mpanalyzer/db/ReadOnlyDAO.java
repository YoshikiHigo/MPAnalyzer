package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Inconsistency;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ChangeType;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ModificationType;

public class ReadOnlyDAO extends DAO {

	static private ReadOnlyDAO SINGLETON = null;

	static public ReadOnlyDAO getInstance() throws Exception {
		if (null == SINGLETON) {
			SINGLETON = new ReadOnlyDAO();
		}
		return SINGLETON;
	}

	static public void deleteInstance() throws Exception {
		if (null != SINGLETON) {
			SINGLETON.modificationStatement.close();
			SINGLETON.patternStatement.close();
			SINGLETON = null;
		}
	}

	final private PreparedStatement modificationStatement;
	final private PreparedStatement patternStatement;
	final private PreparedStatement inconsistencyStatement;

	private ReadOnlyDAO() throws Exception {

		super(false, false, false, false, false, false);

		final StringBuilder patternSQL = new StringBuilder();
		patternSQL
				.append("select id, beforeHash, afterHash, type, support, confidence");
		patternSQL
				.append(" from pattern where ? <= support and ? <= confidence");
		this.patternStatement = this.connector.prepareStatement(patternSQL
				.toString());

		final StringBuilder modificationSQL = new StringBuilder();
		modificationSQL.append("select M.id, M.filepath, ");
		modificationSQL
				.append("(select C1.text from codefragment C1 where C1.hash=M.beforeHash), ");
		modificationSQL
				.append("(select C3.text from codefragment C3 where C3.hash=M.afterHash), ");
		modificationSQL.append("M.revision, M.type, ");
		modificationSQL
				.append("(select R.date from revision R where R.number = M.revision), ");
		modificationSQL
				.append("(select R.message from revision R where R.number = M.revision) ");
		modificationSQL
				.append("from modification M where beforeHash = ? and afterHash = ?");
		this.modificationStatement = this.connector
				.prepareStatement(modificationSQL.toString());

		final StringBuilder inconsistencySQL = new StringBuilder();
		inconsistencySQL
				.append("select filepath, start, end, problempattern, problemID, presentCode, proposedCode from problem where problemID in ");
		inconsistencySQL.append("(select problemID from problem ");
		inconsistencySQL
				.append("group by problemID having count(problemID) <= ?)");
		this.inconsistencyStatement = this.connector
				.prepareStatement(inconsistencySQL.toString());
	}

	public List<Modification> getModifications(final int beforeHash,
			final int afterHash) throws Exception {

		this.modificationStatement.setInt(1, beforeHash);
		this.modificationStatement.setInt(2, afterHash);
		final ResultSet result = this.modificationStatement.executeQuery();

		final List<Modification> modifications = new ArrayList<Modification>();
		while (result.next()) {
			final int id = result.getInt(1);
			final String filepath = result.getString(2);
			final String beforeText = result.getString(3);
			final String afterText = result.getString(4);
			final long number = result.getLong(5);
			final ModificationType modificationType = beforeText.isEmpty() ? ModificationType.ADD
					: afterText.isEmpty() ? ModificationType.DELETE
							: ModificationType.CHANGE;
			final ChangeType changeType = ChangeType.getType(result.getInt(6));
			final String date = result.getString(7);
			final String message = result.getString(8);
			final Modification modification = new Modification(id, filepath,
					new CodeFragment(beforeText), new CodeFragment(afterText),
					new Revision(number, date, message), modificationType,
					changeType);
			modifications.add(modification);
		}

		return modifications;
	}

	public List<ModificationPattern> getModificationPatterns(
			final int supportThreshold, final float confidenceThreshold)
			throws Exception {

		this.patternStatement.setInt(1, supportThreshold);
		this.patternStatement.setFloat(2, confidenceThreshold);
		final ResultSet result = this.patternStatement.executeQuery();

		final List<ModificationPattern> patterns = new ArrayList<ModificationPattern>();
		while (result.next()) {
			final int id = result.getInt(1);
			final int beforeHash = result.getInt(2);
			final int afterHash = result.getInt(3);
			final ModificationType modificationType = (0 == beforeHash) ? ModificationType.ADD
					: (0 == afterHash) ? ModificationType.DELETE
							: ModificationType.CHANGE;
			final ChangeType changeType = ChangeType.getType(result.getInt(4));
			final int support = result.getInt(5);
			final float confidence = result.getFloat(6);
			final ModificationPattern pattern = new ModificationPattern(id,
					support, confidence, beforeHash, afterHash,
					modificationType, changeType);
			patterns.add(pattern);
		}

		return patterns;
	}

	public List<Inconsistency> getInconsistency(final int place)
			throws Exception {

		this.inconsistencyStatement.setInt(1, place);
		final ResultSet result = this.inconsistencyStatement.executeQuery();

		final List<Inconsistency> inconsistencies = new ArrayList<Inconsistency>();
		while (result.next()) {

			final String filepath = result.getString(1);
			final int startLine = result.getInt(2);
			final int endLine = result.getInt(3);
			final String pattern = result.getString(4);
			final int patternID = result.getInt(5);
			final String presentCode = result.getString(6);
			final String suggestedCode= result.getString(7);

			final Inconsistency inconsistency = new Inconsistency(filepath,
					startLine, endLine, pattern, patternID, presentCode,
					suggestedCode, 0, 0f);

			inconsistencies.add(inconsistency);
		}

		return inconsistencies;
	}

	public SortedSet<Revision> getRevisions() throws Exception {

		final Statement revisionStatement = this.connector.createStatement();
		final ResultSet result = revisionStatement
				.executeQuery("select number, date, message from revision");

		final SortedSet<Revision> revisions = new TreeSet<Revision>();
		while (result.next()) {
			final long number = result.getLong(1);
			final String date = result.getString(2);
			final String message = result.getString(3);
			final Revision revision = new Revision(number, date, message);
			revisions.add(revision);
		}

		return revisions;
	}
}
