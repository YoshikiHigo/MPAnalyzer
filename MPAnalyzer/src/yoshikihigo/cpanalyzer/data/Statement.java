package yoshikihigo.cpanalyzer.data;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.lexer.token.ABSTRACT;
import yoshikihigo.cpanalyzer.lexer.token.ANNOTATION;
import yoshikihigo.cpanalyzer.lexer.token.CHARLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.CLASS;
import yoshikihigo.cpanalyzer.lexer.token.DEF;
import yoshikihigo.cpanalyzer.lexer.token.FALSE;
import yoshikihigo.cpanalyzer.lexer.token.FINAL;
import yoshikihigo.cpanalyzer.lexer.token.IDENTIFIER;
import yoshikihigo.cpanalyzer.lexer.token.INTERFACE;
import yoshikihigo.cpanalyzer.lexer.token.LEFTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.LEFTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.LINEEND;
import yoshikihigo.cpanalyzer.lexer.token.LINEINTERRUPTION;
import yoshikihigo.cpanalyzer.lexer.token.NUMBERLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.PRIVATE;
import yoshikihigo.cpanalyzer.lexer.token.PROTECTED;
import yoshikihigo.cpanalyzer.lexer.token.PUBLIC;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.SEMICOLON;
import yoshikihigo.cpanalyzer.lexer.token.STATIC;
import yoshikihigo.cpanalyzer.lexer.token.STRICTFP;
import yoshikihigo.cpanalyzer.lexer.token.STRINGLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.TAB;
import yoshikihigo.cpanalyzer.lexer.token.TRANSIENT;
import yoshikihigo.cpanalyzer.lexer.token.TRUE;
import yoshikihigo.cpanalyzer.lexer.token.Token;

public class Statement {

	final private static ConcurrentMap<Thread, Map<String, String>> IDENTIFIERS = new ConcurrentHashMap<>();
	final private static ConcurrentMap<Thread, Map<String, String>> TYPES = new ConcurrentHashMap<>();

	public static List<Statement> getJCStatements(final List<Token> allTokens) {

		final List<Statement> statements = new ArrayList<Statement>();
		List<Token> tokens = new ArrayList<Token>();

		final Stack<Integer> nestLevel = new Stack<>();
		nestLevel.push(new Integer(0));
		int inParenDepth = 0;
		int index = 0;
		for (final Token token : allTokens) {

			token.index = index++;
			tokens.add(token);

			if ((0 == inParenDepth) && (token instanceof RIGHTBRACKET)) {
				if (0 == nestLevel.peek().intValue()) {
					nestLevel.pop();
					nestLevel.pop();
				} else {
					nestLevel.pop();
				}
			}

			if (token instanceof RIGHTPAREN) {
				inParenDepth--;
			}

			if ((0 == inParenDepth)
					&& (token instanceof LEFTBRACKET
							|| token instanceof RIGHTBRACKET
							|| token instanceof SEMICOLON || token instanceof ANNOTATION)) {

				if (1 < tokens.size()) {

					if (isJCTypeDefinition(tokens)) {
						nestLevel.push(new Integer(0));
					}
					final int nestDepth = nestLevel.peek().intValue();

					final int fromLine = tokens.get(0).line;
					final int toLine = tokens.get(tokens.size() - 1).line;
					// System.out.print(Integer.toString(nestDepth) + ": ");
					final byte[] hash = makeJCHash(tokens);
					final Statement statement = new Statement(fromLine, toLine,
							nestDepth, 1 < nestDepth, tokens, hash);
					statements.add(statement);
					tokens = new ArrayList<Token>();
				}

				else {
					tokens.clear();
				}
			}

			if ((0 == inParenDepth) && (token instanceof LEFTBRACKET)) {
				nestLevel.push(new Integer(nestLevel.peek().intValue() + 1));
			}

			if (token instanceof LEFTPAREN) {
				inParenDepth++;
			}

		}

		return statements;
	}

	public static List<Statement> getPYStatements(final List<Token> allTokens) {

		final List<Statement> statements = new ArrayList<Statement>();
		List<Token> tokens = new ArrayList<Token>();

		final Stack<Integer> methodDefinitionDepth = new Stack<>();

		int nestLevel = 0;
		int index = 0;
		int inParenDepth = 0;
		boolean interrupted = false;
		boolean isIndent = true;
		for (final Token token : allTokens) {

			if (token instanceof TAB) {
				if (isIndent && !interrupted) {
					nestLevel++;
				}
			} else {
				isIndent = false;
			}

			if (!(token instanceof TAB) && !(token instanceof LINEEND)) {
				token.index = index++;
			}

			if (!(token instanceof TAB) && !(token instanceof LINEEND)
					&& !(token instanceof SEMICOLON)
					&& !(token instanceof LINEINTERRUPTION)) {
				tokens.add(token);
			}

			if (token instanceof RIGHTPAREN) {
				inParenDepth--;
			}

			if (token instanceof LEFTPAREN) {
				inParenDepth++;
			}

			if (token instanceof LINEINTERRUPTION) {
				interrupted = true;
			} else {
				interrupted = false;
			}

			// make a statement
			if (!interrupted
					&& (0 == inParenDepth)
					&& ((token instanceof LINEEND) || (token instanceof SEMICOLON))) {
				if (!tokens.isEmpty()) {

					if (isPYMethodDefinition(tokens)) {
						methodDefinitionDepth.push(new Integer(nestLevel));
					}

					if (!methodDefinitionDepth.isEmpty()
							&& (nestLevel < methodDefinitionDepth.peek()
									.intValue())) {
						methodDefinitionDepth.pop();
					}

					final int fromLine = tokens.get(0).line;
					final int toLine = tokens.get(tokens.size() - 1).line;
					final boolean isTarget = (!methodDefinitionDepth.isEmpty() && (methodDefinitionDepth
							.peek().intValue() < nestLevel));
					// System.out.print(Integer.toString(nestLevel) + ": "
					// + Boolean.toString(isTarget) + ": ");
					final byte[] hash = makePYHash(tokens);
					final Statement statement = new Statement(fromLine, toLine,
							nestLevel, isTarget, tokens, hash);
					statements.add(statement);
					tokens = new ArrayList<Token>();
				}
				if (token instanceof LINEEND) {
					nestLevel = 0;
					isIndent = true;
				}
			}
		}

		return statements;
	}

	private static byte[] makeJCHash(final List<Token> tokens) {

		final List<Token> nonTrivialTokens = removeJCTrivialTokens(tokens);
		final StringBuilder builder = new StringBuilder();
		final Map<String, String> identifiers = new HashMap<>();

		for (int index = 0; index < nonTrivialTokens.size(); index++) {

			final Token token = nonTrivialTokens.get(index);

			if (token instanceof IDENTIFIER) {

				if (nonTrivialTokens.size() == (index + 1)
						|| !(nonTrivialTokens.get(index + 1) instanceof LEFTPAREN)) {
					final String name = token.value;
					String normalizedName = identifiers.get(name);
					if (null == normalizedName) {
						normalizedName = "$" + identifiers.size();
						identifiers.put(name, normalizedName);
					}
					builder.append(normalizedName);
				}

				// not normalize if identifier is method name
				else {
					builder.append(token.value);
				}
			}

			else {
				builder.append(token.value);
			}

			builder.append(" ");
		}

		final String text = builder.toString();
		// System.out.println(text);
		final byte[] md5 = getMD5(text);
		return md5;
	}

	private static byte[] makePYHash(final List<Token> tokens) {

		final List<Token> nonTrivialTokens = /* removePYTrivialTokens(tokens) */tokens;
		final StringBuilder builder = new StringBuilder();
		final Map<String, String> identifiers = new HashMap<>();

		for (int index = 0; index < nonTrivialTokens.size(); index++) {

			final Token token = nonTrivialTokens.get(index);

			if (token instanceof IDENTIFIER) {

				if (nonTrivialTokens.size() == (index + 1)
						|| !(nonTrivialTokens.get(index + 1) instanceof LEFTPAREN)) {
					final String name = token.value;
					String normalizedName = identifiers.get(name);
					if (null == normalizedName) {
						normalizedName = "$" + identifiers.size();
						identifiers.put(name, normalizedName);
					}
					builder.append(normalizedName);
				}

				// not normalize if identifier is method name
				else {
					builder.append(token.value);
				}
			}

			else {
				builder.append(token.value);
			}

			builder.append(" ");
		}

		final String text = builder.toString();
		// System.out.println(text);
		final byte[] md5 = getMD5(text);
		return md5;
	}

	private static List<Token> removeJCTrivialTokens(final List<Token> tokens) {
		final List<Token> nonTrivialTokens = new ArrayList<>();
		for (final Token token : tokens) {

			if (token instanceof ABSTRACT || token instanceof FINAL
					|| token instanceof PRIVATE || token instanceof PROTECTED
					|| token instanceof PUBLIC || token instanceof STATIC
					|| token instanceof STRICTFP || token instanceof TRANSIENT) {
				// not used for making hash
				continue;
			}

			else {
				nonTrivialTokens.add(token);
			}
		}

		return nonTrivialTokens;
	}

	private static List<Token> removePYTrivialTokens(final List<Token> tokens) {
		final List<Token> nonTrivialTokens = new ArrayList<>();
		for (final Token token : tokens) {
			nonTrivialTokens.add(token);
		}

		return nonTrivialTokens;
	}

	public static byte[] getMD5(final String text) {
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

	private static boolean isJCTypeDefinition(final List<Token> tokens) {
		final List<Token> nonTrivialTokens = removeJCTrivialTokens(tokens);
		final Token firstToken = nonTrivialTokens.get(0);
		if (firstToken instanceof CLASS || firstToken instanceof INTERFACE) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isPYMethodDefinition(final List<Token> tokens) {
		final List<Token> nonTrivialTokens = removeJCTrivialTokens(tokens);
		final Token firstToken = nonTrivialTokens.get(0);
		if (firstToken instanceof DEF) {
			return true;
		} else {
			return false;
		}
	}

	final public int fromLine;
	final public int toLine;
	final public int nestLevel;
	final public boolean isTarget;
	final public List<Token> tokens;
	final public byte[] hash;

	public Statement(final int fromLine, final int toLine, final int nestLevel,
			final boolean isTarget, final List<Token> tokens, final byte[] hash) {
		this.fromLine = fromLine;
		this.toLine = toLine;
		this.tokens = tokens;
		this.nestLevel = nestLevel;
		this.isTarget = isTarget;
		this.hash = hash;
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
