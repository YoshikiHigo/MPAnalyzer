package yoshikihigo.cpanalyzer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yoshikihigo.cpanalyzer.Config;
import yoshikihigo.cpanalyzer.lexer.token.IDENTIFIER;
import yoshikihigo.cpanalyzer.lexer.token.Token;

public class Statement {

	public static List<Statement> getStatements(final List<Token> tokens) {

		final List<Statement> statements = new ArrayList<Statement>();
		List<Token> tokensForaStatement = new ArrayList<Token>();

		for (final Token token : tokens) {

			if (token.value.equals("{") || token.value.equals("}")
					|| token.value.equals(";")) {

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
		final Map<String, String> identifiers = new HashMap<>();
		for (final Token token : this.tokens) {

			// normalize identifiers if "-normalize" is specified.
			if (Config.getInstance().isNORMALIZATION()
					&& (token instanceof IDENTIFIER)
					&& Character.isLowerCase(token.value.charAt(0))) {
				String normalizedValue = identifiers.get(token.value);
				if (null == normalizedValue) {
					normalizedValue = "$" + identifiers.size();
					identifiers.put(token.value, normalizedValue);
				}
				text.append(normalizedValue);
			}

			else {
				text.append(token.value);
			}

			text.append(" ");
		}
		text.deleteCharAt(text.length() - 1);
		return text.toString();
	}

	public int getStartLine() {
		return this.tokens.get(0).line;
	}

	public int getEndLine() {
		return this.tokens.get(this.tokens.size() - 1).line;
	}
}
