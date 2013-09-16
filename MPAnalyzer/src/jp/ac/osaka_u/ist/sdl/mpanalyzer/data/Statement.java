package jp.ac.osaka_u.ist.sdl.mpanalyzer.data;

import java.util.ArrayList;
import java.util.List;

public class Statement {

	public static List<Statement> getStatements(final List<Token> tokens) {

		final List<Statement> statements = new ArrayList<Statement>();
		List<Token> tokensForaStatement = new ArrayList<Token>();

		for (final Token token : tokens) {

			if (token.name.equals("{") || token.name.equals("}")
					|| token.name.equals(";")) {

				if (!tokensForaStatement.isEmpty()) {
					final Statement statement = new Statement(
							tokensForaStatement);
					statements.add(statement);
					tokensForaStatement = new ArrayList<Token>();
				}

			} else {
				tokensForaStatement.add(token);
			}
		}

		return statements;
	}

	public final List<Token> tokens;
	public final int hash;

	public Statement(final List<Token> tokens) {
		this.tokens = tokens;
		this.hash = this.toString().hashCode();
	}

	public String toString() {
		final StringBuilder text = new StringBuilder();
		for (final Token token : this.tokens) {
			text.append(token.name);
			text.append(" ");
		}
		return text.toString();
	}

	public int getStartLine() {
		return this.tokens.get(0).line;
	}

	public int getEndLine() {
		return this.tokens.get(this.tokens.size() - 1).line;
	}
}
