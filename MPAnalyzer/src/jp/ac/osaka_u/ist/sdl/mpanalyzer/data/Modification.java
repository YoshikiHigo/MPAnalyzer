package jp.ac.osaka_u.ist.sdl.mpanalyzer.data;

public class Modification implements Comparable<Modification> {

	public static enum ModificationType {
		CHANGE() {
			@Override
			public int getValue() {
				return 1;
			}

			@Override
			public String toString() {
				return "CHANGE";
			}
		},

		ADD() {
			@Override
			public int getValue() {
				return 2;
			}

			@Override
			public String toString() {
				return "ADD";
			}
		},

		DELETE() {
			@Override
			public int getValue() {
				return 3;
			}

			@Override
			public String toString() {
				return "DELETE";
			}
		};

		abstract public int getValue();

		@Override
		abstract public String toString();
	}

	public static enum ChangeType {
		TYPE1() {
			@Override
			public int getValue() {
				return 1;
			}

			@Override
			public String toString() {
				return "TYPE1";
			}
		},

		TYPE2() {
			@Override
			public int getValue() {
				return 2;
			}

			@Override
			public String toString() {
				return "TYPE2";
			}
		},

		TYPE3() {
			@Override
			public int getValue() {
				return 3;
			}

			@Override
			public String toString() {
				return "TYPE3";
			}
		};

		public abstract int getValue();

		@Override
		public abstract String toString();

		public static ChangeType getType(final int value) {
			if (2 == value) {
				return TYPE2;
			} else {
				return TYPE3;
			}
		}
	}

	public final int id;
	public final String filepath;
	public final CodeFragment before;
	public final CodeFragment after;
	public final Revision revision;
	public final ModificationType modificationType;
	public final ChangeType changeType;

	public Modification(final String filepath, final CodeFragment before,
			final CodeFragment after, final Revision revision,
			final ModificationType modificationType, final ChangeType changeType) {
		this(-1, filepath, before, after, revision, modificationType,
				changeType);
	}

	public Modification(final int id, final String filepath,
			final CodeFragment before, final CodeFragment after,
			final Revision revision, final ModificationType modificationType,
			final ChangeType changeType) {

		this.id = id;
		this.filepath = filepath;
		this.before = before;
		this.after = after;
		this.revision = revision;
		this.modificationType = modificationType;
		this.changeType = changeType;
	}

	@Override
	public int compareTo(final Modification o) {

		final int revisionComparisonResult = this.revision
				.compareTo(o.revision);
		if (0 != revisionComparisonResult) {
			return revisionComparisonResult;
		}

		final int fileComparisonResult = this.filepath.compareTo(o.filepath);
		if (0 != fileComparisonResult) {
			return fileComparisonResult;
		}
		return new Integer(this.id).compareTo(o.id);
	}

	public boolean isSamePattern(final Modification m) {
		return this.before.equals(m.before) && this.after.equals(m.after);
	}

	public boolean isCondition() {
		for (final Statement s : this.before.statements) {
			final String text = s.toString();
			if (text.startsWith("if ") || text.startsWith("while ")
					|| text.startsWith("for ")) {
				return true;
			}
		}
		for (final Statement s : this.after.statements) {
			final String text = s.toString();
			if (text.startsWith("if ") || text.startsWith("while ")
					|| text.startsWith("for ")) {
				return true;
			}
		}
		return false;
	}
}
