package jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token;

public abstract class Token {

	final public String value;
	public int line;

	Token(final String value) {
		this.value = value;
		this.line = 0;
	}
}
