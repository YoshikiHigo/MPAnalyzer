package jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token;

public abstract class Token {

	final public String value;

	Token(final String value) {
		this.value = value;
	}
}
