package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.sql.PreparedStatement;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.clone.Clone;

public class CloneDAO extends DAO {

	static final private AtomicInteger SETID = new AtomicInteger(0);

	final private PreparedStatement insertPS;
	final private PreparedStatement update1PS;
	final private PreparedStatement update2PS;
	private int numberOfInsertions;

	public CloneDAO(final boolean create) throws Exception {
		super(false, false, false, false, false, create);

		this.insertPS = this.connector
				.prepareStatement("insert into clone (filepath, start, end, revision, setID, groupID, changed) values (?, ?, ?, ?, ?, ?, ?)");
		this.update1PS = this.connector
				.prepareStatement("update clone set changed=? where filepath=? and start=? and end=?");
		this.update2PS = this.connector
				.prepareStatement("update clone set groupID=? where setID=?");
		this.numberOfInsertions = 0;
	}

	public void addClone(final Set<Clone> cloneset) {

		try {

			for (final Clone clone : cloneset) {
				this.insertPS.setString(1, clone.path);
				this.insertPS.setInt(2, clone.statements.get(0).getStartLine());
				this.insertPS.setInt(3,
						clone.statements.get(clone.statements.size() - 1)
								.getEndLine());
				this.insertPS.setLong(4, clone.revision);
				this.insertPS.setInt(5, SETID.intValue());
				this.insertPS.setInt(6, SETID.intValue());
				this.insertPS.setBoolean(7, false);
				this.insertPS.addBatch();
				this.numberOfInsertions++;
			}

			SETID.incrementAndGet();

			if (2000 < this.numberOfInsertions) {
				this.insertPS.executeBatch();
				this.numberOfInsertions = 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateChanged(final Clone clone, final int changed) {
		try {
			this.update1PS.setInt(1, changed);
			this.update1PS.setString(2, clone.path);
			this.update1PS.setInt(3, clone.startLine);
			this.update1PS.setInt(4, clone.endLine);
			this.update1PS.addBatch();
			this.update1PS.executeBatch();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void updateGroupID(final int groupID, final SortedSet<Integer> setIDs) {
		try {
			for (final Integer setID : setIDs) {
				this.update2PS.setInt(1, groupID);
				this.update2PS.setInt(2, setID);
				this.update2PS.addBatch();
			}
			this.update2PS.executeBatch();
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void close() {

		try {

			if (0 < this.numberOfInsertions) {
				this.insertPS.executeBatch();
				this.numberOfInsertions = 0;
			}

			this.insertPS.close();
			this.update1PS.close();
			this.update2PS.close();
			super.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
