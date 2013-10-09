package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.sql.PreparedStatement;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.clone.Clone;

public class CloneDAO extends DAO {

	static final private AtomicInteger SETID = new AtomicInteger(0);

	final private PreparedStatement clonePS;
	private int numberOfClonePS;

	public CloneDAO() throws Exception {
		super(false, false, false, false, false, true);

		this.clonePS = this.connector
				.prepareStatement("insert into clone (filepath, start, end, revision, setID, changed) values (?, ?, ?, ?, ?, ?)");
		this.numberOfClonePS = 0;
	}

	public void addClone(final Set<Clone> cloneset) {

		try {

			for (final Clone clone : cloneset) {
				this.clonePS.setString(1, clone.path);
				this.clonePS.setInt(2, clone.statements.get(0).getStartLine());
				this.clonePS.setInt(3,
						clone.statements.get(clone.statements.size() - 1)
								.getEndLine());
				this.clonePS.setLong(4, clone.revision);
				this.clonePS.setInt(5, SETID.intValue());
				this.clonePS.setBoolean(6, false);
				this.clonePS.addBatch();
				this.numberOfClonePS++;
			}

			SETID.incrementAndGet();

			if (2000 < this.numberOfClonePS) {
				System.out.println("writing clone table ...");
				this.clonePS.executeBatch();
				this.numberOfClonePS = 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {

		try {

			if (0 < this.numberOfClonePS) {
				this.clonePS.executeBatch();
				this.numberOfClonePS = 0;
			}

			this.clonePS.close();
			super.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
