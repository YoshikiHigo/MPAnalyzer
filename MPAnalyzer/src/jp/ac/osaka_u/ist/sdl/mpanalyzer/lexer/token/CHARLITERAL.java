package jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token;

public class CHARLITERAL extends Token {

	public CHARLITERAL(final String value) {
		super("\'" + value + "\'");
	}
}
