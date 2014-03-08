package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;

import java.sql.PreparedStatement;
import java.util.Collection;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;

public class ModificationDAO extends DAO {

	final private PreparedStatement codefragmentPS;
	final private PreparedStatement modificationPS;
	private int numberOfCodefragmentPS;
	private int numberOfModificationPS;

	public ModificationDAO() throws Exception {
		super(false, true, true, false, false, false);

		this.codefragmentPS = this.connector
				.prepareStatement("insert into codefragment values (?, ?, ?, ?, ?)");
		this.modificationPS = this.connector
				.prepareStatement("insert into modification (filepath, beforeID, beforeHash, afterID, afterHash, revision, type) values (?, ?, ?, ?, ?, ?, ?)");

		this.numberOfCodefragmentPS = 0;
		this.numberOfModificationPS = 0;
	}

	public void addModification(final Modification modification)
			throws Exception {

		this.codefragmentPS.setInt(1, modification.before.getID());
		this.codefragmentPS.setString(2, modification.before.text);
		this.codefragmentPS.setInt(3, modification.before.hash);
		final int beforeStart = modification.before.statements.isEmpty() ? 0
				: modification.before.statements.get(0).getStartLine();
		final int beforeEnd = modification.before.statements.isEmpty() ? 0
				: modification.before.statements.get(
						modification.before.statements.size() - 1).getEndLine();
		this.codefragmentPS.setInt(4, beforeStart);
		this.codefragmentPS.setInt(5, beforeEnd);
		this.codefragmentPS.addBatch();
		this.numberOfCodefragmentPS++;

		this.codefragmentPS.setInt(1, modification.after.getID());
		this.codefragmentPS.setString(2, modification.after.text);
		this.codefragmentPS.setInt(3, modification.after.hash);
		final int afterStart = modification.after.statements.isEmpty() ? 0
				: modification.after.statements.get(0).getStartLine();
		final int afterEnd = modification.after.statements.isEmpty() ? 0
				: modification.after.statements.get(
						modification.after.statements.size() - 1).getEndLine();
		this.codefragmentPS.setInt(4, afterStart);
		this.codefragmentPS.setInt(5, afterEnd);
		this.codefragmentPS.addBatch();
		this.numberOfCodefragmentPS++;

		this.modificationPS.setString(1, modification.filepath);
		this.modificationPS.setInt(2, modification.before.getID());
		this.modificationPS.setInt(3, modification.before.hash);
		this.modificationPS.setInt(4, modification.after.getID());
		this.modificationPS.setInt(5, modification.after.hash);
		this.modificationPS.setInt(6, (int) modification.revision.number);
		this.modificationPS.setInt(7, modification.changeType.getValue());
		this.modificationPS.addBatch();
		this.numberOfModificationPS++;

		if (1000 < this.numberOfCodefragmentPS) {
			System.out.println("writing \'codefragment\' table ...");
			this.codefragmentPS.executeBatch();
			this.numberOfCodefragmentPS = 0;
		}

		if (1000 < this.numberOfModificationPS) {
			System.out.println("writing \'modification\' table ...");
			this.modificationPS.executeBatch();
			this.numberOfModificationPS = 0;
		}
	}

	public void addModifications(Collection<Modification> modifications)
			throws Exception {
		for (final Modification modification : modifications) {
			this.addModification(modification);
		}
	}

	public void flush() throws Exception {
		if (0 < this.numberOfCodefragmentPS) {
			System.out.println("writing \'codefragment\' table ...");
			this.codefragmentPS.executeBatch();
			this.numberOfCodefragmentPS = 0;
		}

		if (0 < this.numberOfModificationPS) {
			System.out.println("writing \'modification\' table ...");
			this.modificationPS.executeBatch();
			this.numberOfModificationPS = 0;
		}
	}

	@Override
	public void close() throws Exception {
		this.codefragmentPS.close();
		this.modificationPS.close();
		super.close();
	}
}
