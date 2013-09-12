package jp.ac.osaka_u.ist.sdl.mpanalyzer.data;

import java.util.ArrayList;
import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;

public class CodeFragment {

	public final List<Statement> statements;
	public final String text;
	public final int hash;

	public CodeFragment(final List<Statement> statements) {
		this.statements = statements;
		final StringBuilder tmp = new StringBuilder();
		for (final Statement statement : this.statements) {
			tmp.append(statement);
			tmp.append(System.getProperty("line.separator"));
		}
		this.text = tmp.toString();
		this.hash = this.text.hashCode();
	}

	public CodeFragment(final String text) {
		this.statements = StringUtility.splitToStatements(text);
		this.text = text;
		this.hash = this.text.hashCode();

	}

	@Override
	public boolean equals(final Object o) {

		if (!(o instanceof CodeFragment)) {
			return false;
		}

		final CodeFragment target = (CodeFragment) o;
		if (this.statements.size() != target.statements.size()) {
			return false;
		}

		for (int i = 0; i < this.statements.size(); i++) {
			if (this.statements.get(i).hash != target.statements.get(i).hash) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return this.hash;
	}

	@Override
	public String toString() {
		return this.text;
	}

	public int getStartLine() {
		if (this.statements.isEmpty()) {
			return 0;
		} else {
			return this.statements.get(0).tokens.get(0).line;
		}
	}

	public int getEndLine() {
		if (this.statements.isEmpty()) {
			return 0;
		} else {
			return this.statements.get(this.statements.size() - 1).tokens
					.get(this.statements.get(this.statements.size() - 1).tokens
							.size() - 1).line;
		}
	}

	public List<Token> getTokens() {
		final List<Token> tokens = new ArrayList<Token>();
		for (final Statement statement : this.statements) {
			tokens.addAll(statement.tokens);
		}
		return tokens;
	}
}
