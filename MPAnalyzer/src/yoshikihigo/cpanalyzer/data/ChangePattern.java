package yoshikihigo.cpanalyzer.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.data.Change.ChangeType;
import yoshikihigo.cpanalyzer.data.Change.DiffType;
import yoshikihigo.cpanalyzer.db.ReadOnlyDAO;

public class ChangePattern implements Comparable<ChangePattern> {

	public final int id;
	public final int support;
	public final float confidence;
	public final byte[] beforeHash;
	public final byte[] afterHash;
	public final ChangeType changeType;
	public final DiffType diffType;

	private final List<Change> changes;

	public ChangePattern(final int id, final int support,
			final float confidence, final byte[] beforeHash,
			final byte[] afterHash, final ChangeType changeType,
			final DiffType diffType) {
		this.id = id;
		this.support = support;
		this.confidence = confidence;
		this.beforeHash = beforeHash;
		this.afterHash = afterHash;
		this.changeType = changeType;
		this.diffType = diffType;
		this.changes = new ArrayList<Change>();
	}

	@Override
	public int compareTo(final ChangePattern o) {
		if (this.id > o.id) {
			return 1;
		} else if (this.id < o.id) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof ChangePattern)) {
			return false;
		}
		return this.hashCode() == obj.hashCode();
	}

	public List<Change> getChanges() {
		if (0 == this.changes.size()) {
			this.setChanges();
		}
		return Collections.unmodifiableList(this.changes);
	}

	public SortedSet<String> getFilePaths() {
		final SortedSet<String> paths = new TreeSet<String>();
		if (0 == this.changes.size()) {
			this.setChanges();
		}
		for (final Change modification : this.changes) {
			paths.add(modification.filepath);
		}
		return paths;
	}

	public SortedSet<Revision> getRevisions() {
		final SortedSet<Revision> revisions = new TreeSet<Revision>();
		if (0 == this.changes.size()) {
			this.setChanges();
		}
		for (final Change modification : this.changes) {
			revisions.add(modification.revision);
		}
		return revisions;
	}

	public int getNOF() {
		return this.getFilePaths().size();
	}

	public int getNOD() {

		if (0 == this.changes.size()) {
			this.setChanges();
		}
		final Revision firstRevision = this.changes.get(0).revision;
		final Revision lastRevision = this.changes.get(this.changes.size() - 1).revision;
		final Date firstDate = StringUtility.getDateObject(firstRevision.date);
		final Date lastDate = StringUtility.getDateObject(lastRevision.date);
		final long day = (lastDate.getTime() - firstDate.getTime())
				/ (24 * 60 * 60 * 1000);
		return (int) day;
	}

	public int getNOR() {
		if (0 == this.changes.size()) {
			this.setChanges();
		}
		final Revision firstRevision = this.changes.get(0).revision;
		final Revision lastRevision = this.changes.get(this.changes.size() - 1).revision;
		return (int) (lastRevision.number - firstRevision.number);
	}

	public int getLBM() {
		if (0 == this.changes.size()) {
			this.setChanges();
		}
		return this.changes.get(0).before.statements.size();
	}

	public int getLAM() {
		if (0 == this.changes.size()) {
			this.setChanges();
		}
		return this.changes.get(0).after.statements.size();
	}

	private void setChanges() {
		this.changes.clear();
		try {

			final List<Change> modifications = ReadOnlyDAO.getInstance()
					.getChanges(this.beforeHash, this.afterHash);
			this.changes.addAll(modifications);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
