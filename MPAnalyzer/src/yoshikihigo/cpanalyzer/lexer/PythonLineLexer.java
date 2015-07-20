package yoshikihigo.cpanalyzer.lexer;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import yoshikihigo.cpanalyzer.lexer.token.AND;
import yoshikihigo.cpanalyzer.lexer.token.AND2;
import yoshikihigo.cpanalyzer.lexer.token.ANDEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.ANNOTATION;
import yoshikihigo.cpanalyzer.lexer.token.ASSERT;
import yoshikihigo.cpanalyzer.lexer.token.ASSIGN;
import yoshikihigo.cpanalyzer.lexer.token.BACKQUOTELITERAL;
import yoshikihigo.cpanalyzer.lexer.token.CHARLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.CLASS;
import yoshikihigo.cpanalyzer.lexer.token.COLON;
import yoshikihigo.cpanalyzer.lexer.token.COMMA;
import yoshikihigo.cpanalyzer.lexer.token.CONTINUE;
import yoshikihigo.cpanalyzer.lexer.token.DEF;
import yoshikihigo.cpanalyzer.lexer.token.DEL;
import yoshikihigo.cpanalyzer.lexer.token.DIVIDE;
import yoshikihigo.cpanalyzer.lexer.token.DIVIDEDIVIDEEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.DIVIDEEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.DOT;
import yoshikihigo.cpanalyzer.lexer.token.ELIF;
import yoshikihigo.cpanalyzer.lexer.token.ELSE;
import yoshikihigo.cpanalyzer.lexer.token.EQUAL;
import yoshikihigo.cpanalyzer.lexer.token.EXCEPT;
import yoshikihigo.cpanalyzer.lexer.token.EXCLUSIVEOR;
import yoshikihigo.cpanalyzer.lexer.token.EXCLUSIVEOREQUAL;
import yoshikihigo.cpanalyzer.lexer.token.FALSE2;
import yoshikihigo.cpanalyzer.lexer.token.FINALLY;
import yoshikihigo.cpanalyzer.lexer.token.FOR;
import yoshikihigo.cpanalyzer.lexer.token.FROM;
import yoshikihigo.cpanalyzer.lexer.token.GLOBAL;
import yoshikihigo.cpanalyzer.lexer.token.GREAT;
import yoshikihigo.cpanalyzer.lexer.token.GREATEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.IDENTIFIER;
import yoshikihigo.cpanalyzer.lexer.token.IF;
import yoshikihigo.cpanalyzer.lexer.token.IMPORT;
import yoshikihigo.cpanalyzer.lexer.token.IN;
import yoshikihigo.cpanalyzer.lexer.token.IS;
import yoshikihigo.cpanalyzer.lexer.token.LAMBDA;
import yoshikihigo.cpanalyzer.lexer.token.LEFTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.LEFTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.LEFTSHIFTEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.LEFTSQUAREBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.LESS;
import yoshikihigo.cpanalyzer.lexer.token.LESSEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.LINEEND;
import yoshikihigo.cpanalyzer.lexer.token.LINEINTERRUPTION;
import yoshikihigo.cpanalyzer.lexer.token.MINUS;
import yoshikihigo.cpanalyzer.lexer.token.MINUSEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.MOD;
import yoshikihigo.cpanalyzer.lexer.token.MODEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.NONE;
import yoshikihigo.cpanalyzer.lexer.token.NONLOCAL;
import yoshikihigo.cpanalyzer.lexer.token.NOT;
import yoshikihigo.cpanalyzer.lexer.token.NOT2;
import yoshikihigo.cpanalyzer.lexer.token.NOTEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.NUMBERLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.OR;
import yoshikihigo.cpanalyzer.lexer.token.OR2;
import yoshikihigo.cpanalyzer.lexer.token.OREQUAL;
import yoshikihigo.cpanalyzer.lexer.token.PASS;
import yoshikihigo.cpanalyzer.lexer.token.PLUS;
import yoshikihigo.cpanalyzer.lexer.token.PLUSEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.QUESTION;
import yoshikihigo.cpanalyzer.lexer.token.RAISE;
import yoshikihigo.cpanalyzer.lexer.token.RETURN;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTSHIFTEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTSQUAREBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.SEMICOLON;
import yoshikihigo.cpanalyzer.lexer.token.STAR;
import yoshikihigo.cpanalyzer.lexer.token.STAREQUAL;
import yoshikihigo.cpanalyzer.lexer.token.STARSTAREQUAL;
import yoshikihigo.cpanalyzer.lexer.token.STRINGLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.TAB;
import yoshikihigo.cpanalyzer.lexer.token.TILDA;
import yoshikihigo.cpanalyzer.lexer.token.TRUE2;
import yoshikihigo.cpanalyzer.lexer.token.TRY;
import yoshikihigo.cpanalyzer.lexer.token.Token;
import yoshikihigo.cpanalyzer.lexer.token.WHILE;
import yoshikihigo.cpanalyzer.lexer.token.WITH;
import yoshikihigo.cpanalyzer.lexer.token.YIELD;

public class PythonLineLexer implements LineLexer {

	enum STATE {
		CODE, SINGLEQUOTELITERAL, DOUBLEQUOTELITERAL, BACKQUOTELITERAL;
	}

	@Override
	public List<Token> lexFile(final String text) {

		final List<Token> tokens = new ArrayList<Token>();

		try (final LineNumberReader reader = new LineNumberReader(
				new StringReader(text))) {

			String line;
			final PythonLineLexer lexer = new PythonLineLexer();
			while (null != (line = reader.readLine())) {
				for (final Token t : lexer.lexLine(line)) {
					t.line = reader.getLineNumber();
					tokens.add(t);
				}
			}

		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return tokens;
	}

	final private Stack<STATE> states;

	public PythonLineLexer() {
		this.states = new Stack<STATE>();
		this.states.push(STATE.CODE);
	}

	@Override
	public List<Token> lexLine(final String line) {

		final List<Token> tokens = new ArrayList<Token>();
		final StringBuilder text = new StringBuilder(line);
		boolean interrupted = false;

		while (0 < text.length()) {

			final String string = text.toString();
			if (STATE.CODE == this.states.peek()) {

				if (string.startsWith("**=")) {
					text.delete(0, 3);
					tokens.add(new STARSTAREQUAL());
				} else if (string.startsWith("//=")) {
					text.delete(0, 3);
					tokens.add(new DIVIDEDIVIDEEQUAL());
				} else if (string.startsWith("<<=")) {
					text.delete(0, 3);
					tokens.add(new LEFTSHIFTEQUAL());
				} else if (string.startsWith(">>=")) {
					text.delete(0, 3);
					tokens.add(new RIGHTSHIFTEQUAL());
				} else if (string.startsWith("-=")) {
					text.delete(0, 2);
					tokens.add(new MINUSEQUAL());
				} else if (string.startsWith("+=")) {
					text.delete(0, 2);
					tokens.add(new PLUSEQUAL());
				} else if (string.startsWith("/=")) {
					text.delete(0, 2);
					tokens.add(new DIVIDEEQUAL());
				} else if (string.startsWith("*=")) {
					text.delete(0, 2);
					tokens.add(new STAREQUAL());
				} else if (string.startsWith("%=")) {
					text.delete(0, 2);
					tokens.add(new MODEQUAL());

				} else if (string.startsWith("&=")) {
					text.delete(0, 2);
					tokens.add(new ANDEQUAL());
				} else if (string.startsWith("|=")) {
					text.delete(0, 2);
					tokens.add(new OREQUAL());
				} else if (string.startsWith("^=")) {
					text.delete(0, 2);
					tokens.add(new EXCLUSIVEOREQUAL());

				} else if (string.startsWith("<=")) {
					text.delete(0, 2);
					tokens.add(new LESSEQUAL());
				} else if (string.startsWith(">=")) {
					text.delete(0, 2);
					tokens.add(new GREATEQUAL());
				} else if (string.startsWith("==")) {
					text.delete(0, 2);
					tokens.add(new EQUAL());
				} else if (string.startsWith("<>")) {
					text.delete(0, 2);
					tokens.add(new NOTEQUAL());
				} else if (string.startsWith("!")) {
					text.delete(0, 1);
					tokens.add(new NOT());
				}

				else if (string.startsWith(":")) {
					text.delete(0, 1);
					tokens.add(new COLON());
				} else if (string.startsWith(";")) {
					text.delete(0, 1);
					tokens.add(new SEMICOLON());
				} else if (string.startsWith("=")) {
					text.delete(0, 1);
					tokens.add(new ASSIGN());
				} else if (string.startsWith("-")) {
					text.delete(0, 1);
					tokens.add(new MINUS());
				} else if (string.startsWith("+")) {
					text.delete(0, 1);
					tokens.add(new PLUS());
				} else if (string.startsWith("/")) {
					text.delete(0, 1);
					tokens.add(new DIVIDE());
				} else if (string.startsWith("*")) {
					text.delete(0, 1);
					tokens.add(new STAR());
				} else if (string.startsWith("%")) {
					text.delete(0, 1);
					tokens.add(new MOD());
				} else if (string.startsWith("?")) {
					text.delete(0, 1);
					tokens.add(new QUESTION());
				} else if (string.startsWith("<")) {
					text.delete(0, 1);
					tokens.add(new LESS());
				} else if (string.startsWith(">")) {
					text.delete(0, 1);
					tokens.add(new GREAT());
				} else if (string.startsWith("&")) {
					text.delete(0, 1);
					tokens.add(new AND());
				} else if (string.startsWith("|")) {
					text.delete(0, 1);
					tokens.add(new OR());
				} else if (string.startsWith("~")) {
					text.delete(0, 1);
					tokens.add(new TILDA());
				} else if (string.startsWith("^")) {
					text.delete(0, 1);
					tokens.add(new EXCLUSIVEOR());
				} else if (string.startsWith("(")) {
					text.delete(0, 1);
					tokens.add(new LEFTPAREN());
				} else if (string.startsWith(")")) {
					text.delete(0, 1);
					tokens.add(new RIGHTPAREN());
				} else if (string.startsWith("{")) {
					text.delete(0, 1);
					tokens.add(new LEFTBRACKET());
				} else if (string.startsWith("}")) {
					text.delete(0, 1);
					tokens.add(new RIGHTBRACKET());
				} else if (string.startsWith("[")) {
					text.delete(0, 1);
					tokens.add(new LEFTSQUAREBRACKET());
				} else if (string.startsWith("]")) {
					text.delete(0, 1);
					tokens.add(new RIGHTSQUAREBRACKET());
				} else if (string.startsWith(",")) {
					text.delete(0, 1);
					tokens.add(new COMMA());
				} else if (string.startsWith(".")) {
					text.delete(0, 1);
					tokens.add(new DOT());
				} else if (string.startsWith("\\")) {
					text.delete(0, 1);
					tokens.add(new LINEINTERRUPTION());
					interrupted = false;
				}

				else if ('\"' == string.charAt(0)) {
					this.states.push(STATE.DOUBLEQUOTELITERAL);
					int index = 1;
					LITERAL: while (index < string.length()) {
						if ('\"' == string.charAt(index)) {
							this.states.pop();
							break;
						} else if ('\\' == string.charAt(index)) {
							index++;
							if (index == string.length()) {
								break LITERAL;
							}
						}
						index++;
					}
					final String value = text.substring(1, index);
					text.delete(0, index + 1);
					tokens.add(new STRINGLITERAL(value));
				}

				else if ('\'' == string.charAt(0)) {
					this.states.push(STATE.SINGLEQUOTELITERAL);
					int index = 1;
					LITERAL: while (index < string.length()) {
						if ('\'' == string.charAt(index)) {
							this.states.pop();
							break;
						} else if ('\\' == string.charAt(index)) {
							index++;
							if (index == string.length()) {
								break LITERAL;
							}
						}
						index++;
					}
					final String value = text.substring(1, index);
					text.delete(0, index + 1);
					tokens.add(new CHARLITERAL(value));
				}

				else if ('`' == string.charAt(0)) {
					this.states.push(STATE.BACKQUOTELITERAL);
					int index = 1;
					LITERAL: while (index < string.length()) {
						if ('`' == string.charAt(index)) {
							this.states.pop();
							break;
						} else if ('\\' == string.charAt(index)) {
							index++;
							if (index == string.length()) {
								break LITERAL;
							}
						}
						index++;
					}
					final String value = text.substring(1, index);
					text.delete(0, index + 1);
					tokens.add(new BACKQUOTELITERAL(value));
				}

				else if (isDigit(string.charAt(0))) {
					int index = 1;
					while (index < string.length()) {
						if (!isDigit(string.charAt(index))) {
							break;
						}
						index++;
					}
					text.delete(0, index);
					final String sconstant = string.substring(0, index);
					tokens.add(new NUMBERLITERAL(sconstant));
				}

				else if (isAlphabet(string.charAt(0))
						|| ('_' == string.charAt(0))) {
					int index = 1;
					while (index < string.length()) {
						if (!isAlphabet(string.charAt(index))
								&& !isDigit(string.charAt(index))
								&& '_' != string.charAt(index)) {
							break;
						}
						index++;
					}
					text.delete(0, index);
					final String identifier = string.substring(0, index);

					if (identifier.equals("False")) {
						tokens.add(new FALSE2());
					} else if (identifier.equals("None")) {
						tokens.add(new NONE());
					} else if (identifier.equals("True")) {
						tokens.add(new TRUE2());
					} else if (identifier.equals("and")) {
						tokens.add(new AND2());
					} else if (identifier.equals("assert")) {
						tokens.add(new ASSERT());
					} else if (identifier.equals("class")) {
						tokens.add(new CLASS());
					} else if (identifier.equals("continue")) {
						tokens.add(new CONTINUE());
					} else if (identifier.equals("def")) {
						tokens.add(new DEF());
					} else if (identifier.equals("del")) {
						tokens.add(new DEL());
					} else if (identifier.equals("elif")) {
						tokens.add(new ELIF());
					} else if (identifier.equals("else")) {
						tokens.add(new ELSE());
					} else if (identifier.equals("except")) {
						tokens.add(new EXCEPT());
					} else if (identifier.equals("finally")) {
						tokens.add(new FINALLY());
					} else if (identifier.equals("for")) {
						tokens.add(new FOR());
					} else if (identifier.equals("from")) {
						tokens.add(new FROM());
					} else if (identifier.equals("global")) {
						tokens.add(new GLOBAL());
					} else if (identifier.equals("if")) {
						tokens.add(new IF());
					} else if (identifier.equals("import")) {
						tokens.add(new IMPORT());
					} else if (identifier.equals("in")) {
						tokens.add(new IN());
					} else if (identifier.equals("is")) {
						tokens.add(new IS());
					} else if (identifier.equals("lambda")) {
						tokens.add(new LAMBDA());
					} else if (identifier.equals("nonlocal")) {
						tokens.add(new NONLOCAL());
					} else if (identifier.equals("not")) {
						tokens.add(new NOT2());
					} else if (identifier.equals("or")) {
						tokens.add(new OR2());
					} else if (identifier.equals("pass")) {
						tokens.add(new PASS());
					} else if (identifier.equals("raise")) {
						tokens.add(new RAISE());
					} else if (identifier.equals("return")) {
						tokens.add(new RETURN());
					} else if (identifier.equals("try")) {
						tokens.add(new TRY());
					} else if (identifier.equals("while")) {
						tokens.add(new WHILE());
					} else if (identifier.equals("with")) {
						tokens.add(new WITH());
					} else if (identifier.equals("yield")) {
						tokens.add(new YIELD());
					} else {
						tokens.add(new IDENTIFIER(identifier));
					}
				}

				else if ('@' == string.charAt(0)) {

					int index = 1;
					while (index < string.length()) {
						if (' ' == string.charAt(index)
								|| '\t' == string.charAt(index)) {

						}
						index++;
					}
					text.delete(0, index);
					final String value = string.substring(0, index);
					tokens.add(new ANNOTATION(value));
				}

				else if (' ' == string.charAt(0)) {
					text.deleteCharAt(0);
				}

				else if ('\t' == string.charAt(0)) {
					text.deleteCharAt(0);
					tokens.add(new TAB());
				}

				else {
					assert false : "unexpected situation: " + string;
					System.exit(0);
				}

			} else if (STATE.SINGLEQUOTELITERAL == this.states.peek()) {

				int index = 1;
				LITERAL: while (index < string.length()) {
					if ('\'' == string.charAt(index)) {
						this.states.pop();
						break;
					} else if ('\\' == string.charAt(index)) {
						index++;
						if (index == string.length()) {
							break LITERAL;
						}
					}
					index++;
				}
				final String value = text.substring(1, index);
				text.delete(0, index + 1);
				tokens.add(new CHARLITERAL(value));

			} else if (STATE.DOUBLEQUOTELITERAL == this.states.peek()) {

				int index = 1;
				LITERAL: while (index < string.length()) {
					if ('\"' == string.charAt(index)) {
						this.states.pop();
						break;
					} else if ('\\' == string.charAt(index)) {
						index++;
						if (index == string.length()) {
							break LITERAL;
						}
					}
					index++;
				}
				final String value = text.substring(1, index);
				text.delete(0, index + 1);
				tokens.add(new STRINGLITERAL(value));
			}

			else {
				assert false : "unexpected situation: " + string;
				System.exit(0);
			}
		}

		if (!interrupted) {
			tokens.add(new LINEEND());
		}

		return tokens;
	}

	private static boolean isAlphabet(final char c) {
		return Character.isLowerCase(c) || Character.isUpperCase(c);
	}

	private static boolean isDigit(final char c) {
		return '0' == c || '1' == c || '2' == c || '3' == c || '4' == c
				|| '5' == c || '6' == c || '7' == c || '8' == c || '9' == c;
	}
}
