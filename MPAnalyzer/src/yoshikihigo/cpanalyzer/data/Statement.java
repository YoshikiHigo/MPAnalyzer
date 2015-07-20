package yoshikihigo.cpanalyzer.data;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.lexer.token.CHARLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.FALSE;
import yoshikihigo.cpanalyzer.lexer.token.FINAL;
import yoshikihigo.cpanalyzer.lexer.token.IDENTIFIER;
import yoshikihigo.cpanalyzer.lexer.token.LINEEND;
import yoshikihigo.cpanalyzer.lexer.token.LINEINTERRUPTION;
import yoshikihigo.cpanalyzer.lexer.token.NUMBERLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.PRIVATE;
import yoshikihigo.cpanalyzer.lexer.token.PROTECTED;
import yoshikihigo.cpanalyzer.lexer.token.PUBLIC;
import yoshikihigo.cpanalyzer.lexer.token.SEMICOLON;
import yoshikihigo.cpanalyzer.lexer.token.STRINGLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.TAB;
import yoshikihigo.cpanalyzer.lexer.token.TRUE;
import yoshikihigo.cpanalyzer.lexer.token.Token;

public class Statement {

	final private static ConcurrentMap<Thread, Map<String, String>> IDENTIFIERS = new ConcurrentHashMap<>();
	final private static ConcurrentMap<Thread, Map<String, String>> TYPES = new ConcurrentHashMap<>();

	public static List<Statement> getJCStatements(final List<Token> allTokens) {

		final List<Statement> statements = new ArrayList<Statement>();
		List<Token> tokens = new ArrayList<Token>();
		int index = 0;
		for (final Token token : allTokens) {

			if (!token.value.equals("{") && !token.value.equals("}")) {
				token.index = index++;
				tokens.add(token);
			}

			if (token.value.equals("{") || token.value.equals("}")
					|| token.value.equals(";") || token.value.startsWith("@")) {
				if (!tokens.isEmpty()) {
					final Statement statement = new Statement(tokens);
					statements.add(statement);
					tokens = new ArrayList<Token>();
				}
			}
		}

		return statements;
	}

	public static List<Statement> getPYStatements(final List<Token> allTokens) {

		final List<Statement> statements = new ArrayList<Statement>();
		List<Token> tokens = new ArrayList<Token>();
		int index = 0;
		for (final Token token : allTokens) {

			if (token instanceof TAB) {
				// do nothing
			}

			else if (token instanceof LINEINTERRUPTION) {
				// do nothing
			}

			else if ((token instanceof LINEEND) || (token instanceof SEMICOLON)) {
				if (!tokens.isEmpty()) {
					final Statement statement = new Statement(tokens);
					statements.add(statement);
					tokens = new ArrayList<Token>();
				}
			}

			else {
				token.index = index++;
				tokens.add(token);
			}
		}

		return statements;

	}

	private static byte[] makeHash(final List<Token> tokens) {

		final StringBuilder builder = new StringBuilder();
		final Map<String, String> identifiers = new HashMap<>();

		for (final Token token : tokens) {

			if (token instanceof IDENTIFIER) {
				final String name = token.value;
				String normalizedName = identifiers.get(name);
				if (null == normalizedName) {
					normalizedName = "$" + identifiers.size();
					identifiers.put(name, normalizedName);
				}
				builder.append(normalizedName);
			}

			else {
				builder.append(token.value);
			}

			builder.append(" ");
		}

		final String text = builder.toString();
		final byte[] md5 = getMD5(text);
		return md5;
	}

	private static byte[] getMD5(final String text) {
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			final byte[] data = text.getBytes("UTF-8");
			md.update(data);
			final byte[] digest = md.digest();
			return digest;
		} catch (final NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public final List<Token> tokens;
	public final byte[] hash;

	public Statement(final List<Token> tokens) {
		this.tokens = tokens;
		this.hash = makeHash(tokens);
	}

	public String toString() {
		final StringBuilder text = new StringBuilder();
		final Map<String, String> identifiers = this.getIdentifierPool();
		final Map<String, String> types = this.getTypePool();
		identifiers.clear();
		types.clear();

		for (final Token token : this.tokens) {

			// normalize identifiers if "-normalize" is specified.
			if (CPAConfig.getInstance().isNORMALIZATION()) {

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

	private Map<String, String> getIdentifierPool() {
		final Thread thread = Thread.currentThread();
		Map<String, String> map = IDENTIFIERS.get(thread);
		if (null == map) {
			map = new HashMap<>();
			IDENTIFIERS.put(thread, map);
		}
		return map;
	}

	private Map<String, String> getTypePool() {
		final Thread thread = Thread.currentThread();
		Map<String, String> map = TYPES.get(thread);
		if (null == map) {
			map = new HashMap<>();
			TYPES.put(thread, map);
		}
		return map;
	}
}
