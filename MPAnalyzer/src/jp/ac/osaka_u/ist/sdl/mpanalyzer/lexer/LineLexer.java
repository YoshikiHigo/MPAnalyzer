package jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer;

import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.Token;

public interface LineLexer {

	List<Token> lexFile(final String text);

	List<Token> lexLine(final String line);
}
