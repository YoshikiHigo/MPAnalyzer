package jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer;

import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.lexer.token.Token;

public interface LineLexer {

	List<Token> lex(final String line);
}
