package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.sql.PreparedStatement;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.clone.Clone;

public class CloneDAO extends DAO {

	static final private AtomicInteger SETID = new AtomicInteger(0);

	final private PreparedStatement insertPS;
	final private PreparedStatement updatePS;
	private int numberOfInsertions;

	public CloneDAO(final boolean create) throws Exception {
		super(false, false, false, false, false, create);

		this.insertPS = this.connector
				.prepareStatement("insert into clone (filepath, start, end, revision, setID, changed) values (?, ?, ?, ?, ?, ?)");
		this.updatePS = this.connector
				.prepareStatement("update clone set changed=? where filepath=? and start=? and end=?");
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
				this.insertPS.setBoolean(6, false);
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

		final StringBuilder sql = new StringBuilder();
		sql.append("update clone set=");
		sql.append(Integer.toString(changed));
		sql.append(" where filepath=\'");
		sql.append(clone.path);
		sql.append("\' and start=");
		sql.append(Integer.toString(clone.startLine));
		sql.append(" and end=");
		sql.append(Integer.toString(clone.endLine));

		try {

			this.updatePS.setInt(1, changed);
			this.updatePS.setString(2, clone.path);
			this.updatePS.setInt(3, clone.startLine);
			this.updatePS.setInt(4, clone.endLine);
			this.updatePS.addBatch();
			this.updatePS.executeBatch();

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
			this.updatePS.close();
			super.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
