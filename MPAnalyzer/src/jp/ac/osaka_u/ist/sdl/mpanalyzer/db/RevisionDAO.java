package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.sql.PreparedStatement;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;

public class RevisionDAO extends DAO {

	final private PreparedStatement revisionPS;
	private int numberOfRevisionPS;

	public RevisionDAO() throws Exception {
		super(true, false, false, false, false);

		System.out.println("table \'revision\' was initialized.");

		this.revisionPS = this.connector
				.prepareStatement("insert into revision values (?, ?, ?)");
		this.numberOfRevisionPS = 0;
	}

	public void addRevision(final Revision revision) {

		try {

			this.revisionPS.setLong(1, revision.number);
			this.revisionPS.setString(2, revision.date);
			this.revisionPS.setString(3, revision.message);
			this.revisionPS.addBatch();
			this.numberOfRevisionPS++;

			if (20000 < this.numberOfRevisionPS) {
				System.out.println("writing revision table ...");
				this.revisionPS.executeBatch();
				this.numberOfRevisionPS = 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {

		try {

			if (0 < this.numberOfRevisionPS) {
				System.out.println("writing revision table ...");
				this.revisionPS.executeBatch();
				this.numberOfRevisionPS = 0;
			}

			this.revisionPS.close();
			super.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
