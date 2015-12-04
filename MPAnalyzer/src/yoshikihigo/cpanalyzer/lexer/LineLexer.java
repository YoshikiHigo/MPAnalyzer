package yoshikihigo.cpanalyzer.lexer;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import yoshikihigo.cpanalyzer.lexer.token.Token;

public abstract class LineLexer {

	final public List<Token> lexFile(final String text) {

		final List<Token> tokens = new ArrayList<>();
		try (final LineNumberReader reader = new LineNumberReader(
				new StringReader(text))) {

			String line;
			while (null != (line = reader.readLine())) {
				for (final Token t : this.lexLine(line)) {
					t.line = reader.getLineNumber();
					tokens.add(t);
				}
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}

		return tokens;
	}

	abstract public List<Token> lexLine(final String line);

	static protected boolean isAlphabet(final char c) {
		return Character.isLowerCase(c) || Character.isUpperCase(c);
	}

	static protected boolean isDigit(final char c) {
		return '0' == c || '1' == c || '2' == c || '3' == c || '4' == c
				|| '5' == c || '6' == c || '7' == c || '8' == c || '9' == c;
	}
}
