package jp.ac.osaka_u.ist.sdl.mpanalyzer.clone;

import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;

public class Clone {

	final public String path;
	final public long revision;
	final public List<Statement> statements;
	final public int startLine;
	final public int endLine;
	final public int changed;

	public Clone(final String path, final long revision,
			final List<Statement> statements) {
		this.path = path;
		this.revision = revision;
		this.statements = statements;
		this.startLine = statements.get(0).getStartLine();
		this.endLine = statements.get(statements.size() - 1).getEndLine();
		this.changed = 0;
	}

	public Clone(final String path, final long revision, final int startLine,
			final int endLine) {
		this.path = path;
		this.revision = revision;
		this.statements = null;
		this.startLine = startLine;
		this.endLine = endLine;
		this.changed = 0;
	}

	public Clone(final String path, final long revision, final int startLine,
			final int endLine, final int changed) {
		this.path = path;
		this.revision = revision;
		this.statements = null;
		this.startLine = startLine;
		this.endLine = endLine;
		this.changed = changed;
	}

	@Override
	public int hashCode() {
		return this.path.hashCode() + this.statements.hashCode();
	}

	@Override
	public boolean equals(final Object o) {

		if (o instanceof Clone) {
			final Clone clone = (Clone) o;
			return this.path.equals(clone.path)
					&& (this.startLine == clone.startLine)
					&& (this.endLine == clone.endLine);
		}

		return false;
	}
}
