package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.sql.PreparedStatement;
import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Inconsistency;

public class InconsistencyDAO extends DAO {

	public InconsistencyDAO() throws Exception {
		super(false, false, false, false, true, false);

	}

	public void addInconsistency(final Inconsistency inconsistency)
			throws Exception {
		final PreparedStatement insert = this.connector
				.prepareStatement("insert into problem values (?, ?, ?, ?, ?, ?, ?)");
		insert.setString(1, inconsistency.filepath);
		insert.setInt(2, inconsistency.startLine);
		insert.setInt(3, inconsistency.endLine);
		insert.setString(4, inconsistency.pattern);
		insert.setInt(5, inconsistency.patternID);
		insert.setString(6, inconsistency.presentCode);
		insert.setString(7, inconsistency.suggestedCode);
		insert.execute();
		insert.close();
	}

	public void addInconsistencies(final List<Inconsistency> inconsistencies)
			throws Exception {

		final PreparedStatement insert = this.connector
				.prepareStatement("insert into problem values (?, ?, ?, ?, ?, ?, ?)");
		for (final Inconsistency inconsistency : inconsistencies) {
			insert.setString(1, inconsistency.filepath);
			insert.setInt(2, inconsistency.startLine);
			insert.setInt(3, inconsistency.endLine);
			insert.setString(4, inconsistency.pattern);
			insert.setInt(5, inconsistency.patternID);
			insert.setString(6, inconsistency.presentCode);
			insert.setString(7, inconsistency.suggestedCode);
			insert.addBatch();
		}
		insert.executeBatch();
		insert.close();
	}
}
