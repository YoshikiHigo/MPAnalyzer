package jp.ac.osaka_u.ist.sdl.mpanalyzer.clone;

import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;

public class Clone {

	final public String path;
	final public long revision;
	final public List<Statement> statements;

	public Clone(final String path, final long revision,
			final List<Statement> statements) {
		this.path = path;
		this.revision = revision;
		this.statements = statements;
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
					&& this.statements.equals(clone.statements);
		}

		return false;
	}
}
