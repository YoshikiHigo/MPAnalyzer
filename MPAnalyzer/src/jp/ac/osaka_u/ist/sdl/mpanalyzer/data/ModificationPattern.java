package jp.ac.osaka_u.ist.sdl.mpanalyzer.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ChangeType;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ModificationType;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;

public class ModificationPattern implements Comparable<ModificationPattern> {

	public final int id;
	public final int support;
	public final float confidence;
	public final int beforeHash;
	public final int afterHash;
	public final ModificationType modificationType;
	public final ChangeType changeType;

	private final List<Modification> modifications;

	public ModificationPattern(final int id, final int support,
			final float confidence, final int beforeHash, final int afterHash,
			final ModificationType modificationType, final ChangeType changeType) {
		this.id = id;
		this.support = support;
		this.confidence = confidence;
		this.beforeHash = beforeHash;
		this.afterHash = afterHash;
		this.modificationType = modificationType;
		this.changeType = changeType;
		this.modifications = new ArrayList<Modification>();
	}

	@Override
	public int compareTo(final ModificationPattern o) {
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
		if (!(obj instanceof ModificationPattern)) {
			return false;
		}
		return this.hashCode() == obj.hashCode();
	}

	public List<Modification> getModifications() {
		if (0 == this.modifications.size()) {
			this.setModifications();
		}
		return Collections.unmodifiableList(this.modifications);
	}

	public SortedSet<String> getFilePaths() {
		final SortedSet<String> paths = new TreeSet<String>();
		if (0 == this.modifications.size()) {
			this.setModifications();
		}
		for (final Modification modification : this.modifications) {
			paths.add(modification.filepath);
		}
		return paths;
	}

	public SortedSet<Revision> getRevisions() {
		final SortedSet<Revision> revisions = new TreeSet<Revision>();
		if (0 == this.modifications.size()) {
			this.setModifications();
		}
		for (final Modification modification : this.modifications) {
			revisions.add(modification.revision);
		}
		return revisions;
	}

	public int getNOF() {
		return this.getFilePaths().size();
	}

	public int getNOD() {

		if (0 == this.modifications.size()) {
			this.setModifications();
		}
		final Revision firstRevision = this.modifications.get(0).revision;
		final Revision lastRevision = this.modifications.get(this.modifications
				.size() - 1).revision;
		final Date firstDate = StringUtility.getDateObject(firstRevision.date);
		final Date lastDate = StringUtility.getDateObject(lastRevision.date);
		final long day = (lastDate.getTime() - firstDate.getTime())
				/ (24 * 60 * 60 * 1000);
		return (int) day;
	}

	public int getNOR() {
		if (0 == this.modifications.size()) {
			this.setModifications();
		}
		final Revision firstRevision = this.modifications.get(0).revision;
		final Revision lastRevision = this.modifications.get(this.modifications
				.size() - 1).revision;
		return (int) (lastRevision.number - firstRevision.number);
	}

	public int getLBM() {
		if (0 == this.modifications.size()) {
			this.setModifications();
		}
		return this.modifications.get(0).before.statements.size();
	}
	
	public int getLAM(){
		if(0 == this.modifications.size()){
			this.setModifications();
		}
		return this.modifications.get(0).after.statements.size();
	}

	private void setModifications() {
		this.modifications.clear();
		try {

			final List<Modification> modifications = ReadOnlyDAO.getInstance()
					.getModifications(this.beforeHash, this.afterHash);
			this.modifications.addAll(modifications);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
