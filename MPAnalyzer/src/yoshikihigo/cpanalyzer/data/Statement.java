package yoshikihigo.cpanalyzer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yoshikihigo.cpanalyzer.Config;
import yoshikihigo.cpanalyzer.lexer.token.CHARLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.FALSE;
import yoshikihigo.cpanalyzer.lexer.token.FINAL;
import yoshikihigo.cpanalyzer.lexer.token.IDENTIFIER;
import yoshikihigo.cpanalyzer.lexer.token.NUMBERLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.PRIVATE;
import yoshikihigo.cpanalyzer.lexer.token.PROTECTED;
import yoshikihigo.cpanalyzer.lexer.token.PUBLIC;
import yoshikihigo.cpanalyzer.lexer.token.STRINGLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.TRUE;
import yoshikihigo.cpanalyzer.lexer.token.Token;

public class Statement {

	public static List<Statement> getStatements(final List<Token> tokens) {

		final List<Statement> statements = new ArrayList<Statement>();
		List<Token> tokensForaStatement = new ArrayList<Token>();

		for (final Token token : tokens) {

			tokensForaStatement.add(token);

			if (token.value.equals("{") || token.value.equals("}")
					|| token.value.equals(";") || token.value.startsWith("@")) {
				final Statement statement = new Statement(tokensForaStatement);
				statements.add(statement);
				tokensForaStatement = new ArrayList<Token>();
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
		final Map<String, String> types = new HashMap<>();
		for (final Token token : this.tokens) {

			// normalize identifiers if "-normalize" is specified.
			if (Config.getInstance().isNORMALIZATION()) {

				if ((token instanceof IDENTIFIER)
						&& Character.isLowerCase(token.value.charAt(0))) {
					String normalizedValue = identifiers.get(token.value);
					if (null == normalizedValue) {
						normalizedValue = "$V" + identifiers.size();
						identifiers.put(token.value, normalizedValue);
					}
					text.append(normalizedValue);
					text.append(" ");
				}

				else if ((token instanceof IDENTIFIER)
						&& Character.isUpperCase(token.value.charAt(0))) {
					String normalizedValue = types.get(token.value);
					if (null == normalizedValue) {
						normalizedValue = "$T" + types.size();
						types.put(token.value, normalizedValue);
					}
					text.append(normalizedValue);
					text.append(" ");
				}

				else if ((token instanceof CHARLITERAL)
						|| (token instanceof NUMBERLITERAL)
						|| (token instanceof STRINGLITERAL
								|| (token instanceof TRUE) || (token instanceof FALSE))) {
					text.append("$L");
					text.append(" ");
				}

				else if (token instanceof PRIVATE || token instanceof PROTECTED
						|| token instanceof PUBLIC || token instanceof FINAL) {
					// do nothing
				}

				else {
					text.append(token.value);
					text.append(" ");
				}
			}

			else {
				text.append(token.value);
				text.append(" ");
			}
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
