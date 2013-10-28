package jp.ac.osaka_u.ist.sdl.mpanalyzer.data;

import java.util.ArrayList;
import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.ast.C3ASTVisitor;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class SimpleToken {

	private static CompilationUnit createAST(final String text) {
		final ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(text.toCharArray());
		return (CompilationUnit) parser.createAST(new NullProgressMonitor());
	}

	public static List<SimpleToken> getTokens(final String text) {
		final CompilationUnit unit = createAST(text);
		final List<SimpleToken> tokens = new ArrayList<SimpleToken>();
		final C3ASTVisitor visitor = new C3ASTVisitor(tokens, unit);
		unit.accept(visitor);
		return tokens;
	}

	public enum TokenType {
		IDENTIFIER, PRESERVED, LITERAL, OPERATOR, COMMA, COLON, HATENA, SEMICOLON, PIRIOD, PAREN, BRACE, BRACKET, ANGLE, STATEMENT, WILDCARD;
	}

	final public String name;
	final public TokenType type;
	final public int line;

	public SimpleToken(final String name, final TokenType type, final int line) {
		this.name = name;
		this.type = type;
		this.line = line;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public String toString() {
		return this.name;
	}
}
