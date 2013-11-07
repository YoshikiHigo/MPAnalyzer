package jp.ac.osaka_u.ist.sdl.mpanalyzer.data;

import java.util.ArrayList;
import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.Token;

public class CodeFragment implements Comparable<CodeFragment> {

	public final List<Statement> statements;
	public final String text;
	public final String position;
	public final int hash;

	public CodeFragment(final List<Statement> statements) {
		this.statements = statements;
		{
			final StringBuilder tmp = new StringBuilder();
			for (final Statement statement : this.statements) {
				tmp.append(statement);
				tmp.append(System.getProperty("line.separator"));
			}
			this.text = tmp.toString();
		}
		{

			if (!statements.isEmpty()) {
				final StringBuilder tmp2 = new StringBuilder();
				tmp2.append(statements.get(0).getStartLine());
				tmp2.append(" --- ");
				tmp2.append(statements.get(statements.size() - 1).getEndLine());
				this.position = tmp2.toString();
			} else {
				this.position = "not exist.";
			}

		}
		this.hash = this.text.hashCode();
	}

	public CodeFragment(final String text, final int startLine,
			final int endLine) {
		this(StringUtility.splitToStatements(text, startLine, endLine));
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

	@Override
	public int compareTo(final CodeFragment o) {
		return this.text.compareTo(o.text);
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
