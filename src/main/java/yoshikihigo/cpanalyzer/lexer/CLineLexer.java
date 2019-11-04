package yoshikihigo.cpanalyzer.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import yoshikihigo.cpanalyzer.lexer.token.AND;
import yoshikihigo.cpanalyzer.lexer.token.ANDAND;
import yoshikihigo.cpanalyzer.lexer.token.ASM;
import yoshikihigo.cpanalyzer.lexer.token.ASSIGN;
import yoshikihigo.cpanalyzer.lexer.token.AUTO;
import yoshikihigo.cpanalyzer.lexer.token.BREAK;
import yoshikihigo.cpanalyzer.lexer.token.CASE;
import yoshikihigo.cpanalyzer.lexer.token.CHAR;
import yoshikihigo.cpanalyzer.lexer.token.CHARLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.COLON;
import yoshikihigo.cpanalyzer.lexer.token.COMMA;
import yoshikihigo.cpanalyzer.lexer.token.CONST;
import yoshikihigo.cpanalyzer.lexer.token.CONTINUE;
import yoshikihigo.cpanalyzer.lexer.token.DECREMENT;
import yoshikihigo.cpanalyzer.lexer.token.DEFAULT;
import yoshikihigo.cpanalyzer.lexer.token.DIVIDE;
import yoshikihigo.cpanalyzer.lexer.token.DIVIDEEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.DO;
import yoshikihigo.cpanalyzer.lexer.token.DOT;
import yoshikihigo.cpanalyzer.lexer.token.DOUBLE;
import yoshikihigo.cpanalyzer.lexer.token.ELSE;
import yoshikihigo.cpanalyzer.lexer.token.ENDASM;
import yoshikihigo.cpanalyzer.lexer.token.ENTRY;
import yoshikihigo.cpanalyzer.lexer.token.ENUM;
import yoshikihigo.cpanalyzer.lexer.token.EQUAL;
import yoshikihigo.cpanalyzer.lexer.token.EXTERN;
import yoshikihigo.cpanalyzer.lexer.token.FLOAT;
import yoshikihigo.cpanalyzer.lexer.token.FOR;
import yoshikihigo.cpanalyzer.lexer.token.GOTO;
import yoshikihigo.cpanalyzer.lexer.token.GREAT;
import yoshikihigo.cpanalyzer.lexer.token.GREATEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.IDENTIFIER;
import yoshikihigo.cpanalyzer.lexer.token.IF;
import yoshikihigo.cpanalyzer.lexer.token.INCREMENT;
import yoshikihigo.cpanalyzer.lexer.token.INT;
import yoshikihigo.cpanalyzer.lexer.token.LEFTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.LEFTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.LEFTSHIFT;
import yoshikihigo.cpanalyzer.lexer.token.LEFTSHIFTEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.LEFTSQUAREBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.LESS;
import yoshikihigo.cpanalyzer.lexer.token.LESSEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.LONG;
import yoshikihigo.cpanalyzer.lexer.token.MINUS;
import yoshikihigo.cpanalyzer.lexer.token.MINUSEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.MOD;
import yoshikihigo.cpanalyzer.lexer.token.MODEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.NOT;
import yoshikihigo.cpanalyzer.lexer.token.NOTEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.NULL2;
import yoshikihigo.cpanalyzer.lexer.token.NUMBERLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.OR;
import yoshikihigo.cpanalyzer.lexer.token.OROR;
import yoshikihigo.cpanalyzer.lexer.token.PLUS;
import yoshikihigo.cpanalyzer.lexer.token.PLUSEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.QUESTION;
import yoshikihigo.cpanalyzer.lexer.token.REGISTER;
import yoshikihigo.cpanalyzer.lexer.token.RETURN;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTARROW;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTSHIFT;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTSHIFTEQUAL;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTSQUAREBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.SEMICOLON;
import yoshikihigo.cpanalyzer.lexer.token.SHARP;
import yoshikihigo.cpanalyzer.lexer.token.SHORT;
import yoshikihigo.cpanalyzer.lexer.token.SIGNED;
import yoshikihigo.cpanalyzer.lexer.token.SIZEOF;
import yoshikihigo.cpanalyzer.lexer.token.STAR;
import yoshikihigo.cpanalyzer.lexer.token.STAREQUAL;
import yoshikihigo.cpanalyzer.lexer.token.STATIC;
import yoshikihigo.cpanalyzer.lexer.token.STRINGLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.STRUCT;
import yoshikihigo.cpanalyzer.lexer.token.SWITCH;
import yoshikihigo.cpanalyzer.lexer.token.TYPEDEF;
import yoshikihigo.cpanalyzer.lexer.token.Token;
import yoshikihigo.cpanalyzer.lexer.token.UNION;
import yoshikihigo.cpanalyzer.lexer.token.UNSIGNED;
import yoshikihigo.cpanalyzer.lexer.token.VOID;
import yoshikihigo.cpanalyzer.lexer.token.VOLATILE;
import yoshikihigo.cpanalyzer.lexer.token.WHILE;

public class CLineLexer extends LineLexer {

  enum CSTATE {
    CODE, SINGLEQUOTELITERAL, DOUBLEQUOTELITERAL;
  }

  final private Stack<CSTATE> states;

  public CLineLexer() {
    this.states = new Stack<>();
    this.states.push(CSTATE.CODE);
  }

  @Override
  public List<Token> lexLine(final String line) {

    final List<Token> tokenList = new ArrayList<>();
    final StringBuilder text = new StringBuilder(line);

    while (0 < text.length()) {

      final String string = text.toString();

      if (CSTATE.CODE == this.states.peek()) {

        if (string.startsWith("<<=")) {
          text.delete(0, 3);
          tokenList.add(new LEFTSHIFTEQUAL());
        } else if (string.startsWith(">>=")) {
          text.delete(0, 3);
          tokenList.add(new RIGHTSHIFTEQUAL());
        } else if (string.startsWith("-=")) {
          text.delete(0, 2);
          tokenList.add(new MINUSEQUAL());
        } else if (string.startsWith("+=")) {
          text.delete(0, 2);
          tokenList.add(new PLUSEQUAL());
        } else if (string.startsWith("/=")) {
          text.delete(0, 2);
          tokenList.add(new DIVIDEEQUAL());
        } else if (string.startsWith("*=")) {
          text.delete(0, 2);
          tokenList.add(new STAREQUAL());
        } else if (string.startsWith("%=")) {
          text.delete(0, 2);
          tokenList.add(new MODEQUAL());
        } else if (string.startsWith("++")) {
          text.delete(0, 2);
          tokenList.add(new INCREMENT());
        } else if (string.startsWith("--")) {
          text.delete(0, 2);
          tokenList.add(new DECREMENT());
        } else if (string.startsWith("<=")) {
          text.delete(0, 2);
          tokenList.add(new LESSEQUAL());
        } else if (string.startsWith(">=")) {
          text.delete(0, 2);
          tokenList.add(new GREATEQUAL());
        } else if (string.startsWith("==")) {
          text.delete(0, 2);
          tokenList.add(new EQUAL());
        } else if (string.startsWith("!=")) {
          text.delete(0, 2);
          tokenList.add(new NOTEQUAL());
        } else if (string.startsWith("->")) {
          text.delete(0, 2);
          tokenList.add(new RIGHTARROW());
        } else if (string.startsWith("&&")) {
          text.delete(0, 2);
          tokenList.add(new ANDAND());
        } else if (string.startsWith("||")) {
          text.delete(0, 2);
          tokenList.add(new OROR());
        } else if (string.startsWith("<<")) {
          text.delete(0, 2);
          tokenList.add(new LEFTSHIFT());
        } else if (string.startsWith(">>")) {
          text.delete(0, 2);
          tokenList.add(new RIGHTSHIFT());
        }

        else if (string.startsWith("!")) {
          text.delete(0, 1);
          tokenList.add(new NOT());
        } else if (string.startsWith(":")) {
          text.delete(0, 1);
          tokenList.add(new COLON());
        } else if (string.startsWith(";")) {
          text.delete(0, 1);
          tokenList.add(new SEMICOLON());
        } else if (string.startsWith("=")) {
          text.delete(0, 1);
          tokenList.add(new ASSIGN());
        } else if (string.startsWith("-")) {
          text.delete(0, 1);
          tokenList.add(new MINUS());
        } else if (string.startsWith("+")) {
          text.delete(0, 1);
          tokenList.add(new PLUS());
        } else if (string.startsWith("/")) {
          text.delete(0, 1);
          tokenList.add(new DIVIDE());
        } else if (string.startsWith("*")) {
          text.delete(0, 1);
          tokenList.add(new STAR());
        } else if (string.startsWith("%")) {
          text.delete(0, 1);
          tokenList.add(new MOD());
        } else if (string.startsWith("?")) {
          text.delete(0, 1);
          tokenList.add(new QUESTION());
        } else if (string.startsWith("<")) {
          text.delete(0, 1);
          tokenList.add(new LESS());
        } else if (string.startsWith(">")) {
          text.delete(0, 1);
          tokenList.add(new GREAT());
        } else if (string.startsWith("&")) {
          text.delete(0, 1);
          tokenList.add(new AND());
        } else if (string.startsWith("|")) {
          text.delete(0, 1);
          tokenList.add(new OR());
        } else if (string.startsWith("(")) {
          text.delete(0, 1);
          tokenList.add(new LEFTPAREN());
        } else if (string.startsWith(")")) {
          text.delete(0, 1);
          tokenList.add(new RIGHTPAREN());
        } else if (string.startsWith("{")) {
          text.delete(0, 1);
          tokenList.add(new LEFTBRACKET());
        } else if (string.startsWith("}")) {
          text.delete(0, 1);
          tokenList.add(new RIGHTBRACKET());
        } else if (string.startsWith("[")) {
          text.delete(0, 1);
          tokenList.add(new LEFTSQUAREBRACKET());
        } else if (string.startsWith("]")) {
          text.delete(0, 1);
          tokenList.add(new RIGHTSQUAREBRACKET());
        } else if (string.startsWith(",")) {
          text.delete(0, 1);
          tokenList.add(new COMMA());
        } else if (string.startsWith(".")) {
          text.delete(0, 1);
          tokenList.add(new DOT());
        } else if (string.startsWith("#")) {
          text.delete(0, 1);
          tokenList.add(new SHARP());
        }

        else if ('\"' == string.charAt(0)) {
          this.states.push(CSTATE.DOUBLEQUOTELITERAL);
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
          tokenList.add(new STRINGLITERAL(value));
        }

        else if ('\'' == string.charAt(0)) {
          this.states.push(CSTATE.SINGLEQUOTELITERAL);
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
          tokenList.add(new CHARLITERAL(value));
        }

        else if (string.startsWith("0x")) {
          int index = 2;
          while (index < string.length()) {
            if ((!isDigit(string.charAt(index))) && (!isAlphabet(string.charAt(index)))) {
              break;
            }
            index++;
          }
          text.delete(0, index);
          final String sconstant = string.substring(0, index);
          tokenList.add(new NUMBERLITERAL(sconstant));
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
          tokenList.add(new NUMBERLITERAL(sconstant));
        }

        else if (isAlphabet(string.charAt(0))) {
          int index = 1;
          while (index < string.length()) {
            if (!isAlphabet(string.charAt(index)) && !isDigit(string.charAt(index))
                && '_' != string.charAt(index) && '$' != string.charAt(index)) {
              break;
            }
            index++;
          }
          text.delete(0, index);
          final String identifier = string.substring(0, index);

          if (identifier.equals("asm")) {
            tokenList.add(new ASM());
          } else if (identifier.equals("auto")) {
            tokenList.add(new AUTO());
          } else if (identifier.equals("break")) {
            tokenList.add(new BREAK());
          } else if (identifier.equals("case")) {
            tokenList.add(new CASE());
          } else if (identifier.equals("char")) {
            tokenList.add(new CHAR());
          } else if (identifier.equals("const")) {
            tokenList.add(new CONST());
          } else if (identifier.equals("continue")) {
            tokenList.add(new CONTINUE());
          } else if (identifier.equals("default")) {
            tokenList.add(new DEFAULT());
          } else if (identifier.equals("do")) {
            tokenList.add(new DO());
          } else if (identifier.equals("double")) {
            tokenList.add(new DOUBLE());
          } else if (identifier.equals("else")) {
            tokenList.add(new ELSE());
          } else if (identifier.equals("endasm")) {
            tokenList.add(new ENDASM());
          } else if (identifier.equals("entry")) {
            tokenList.add(new ENTRY());
          } else if (identifier.equals("enum")) {
            tokenList.add(new ENUM());
          } else if (identifier.equals("extern")) {
            tokenList.add(new EXTERN());
          } else if (identifier.equals("float")) {
            tokenList.add(new FLOAT());
          } else if (identifier.equals("for")) {
            tokenList.add(new FOR());
          } else if (identifier.equals("goto")) {
            tokenList.add(new GOTO());
          } else if (identifier.equals("if")) {
            tokenList.add(new IF());
          } else if (identifier.equals("int")) {
            tokenList.add(new INT());
          } else if (identifier.equals("long")) {
            tokenList.add(new LONG());
          } else if (identifier.equals("NULL")) {
            tokenList.add(new NULL2());
          } else if (identifier.equals("register")) {
            tokenList.add(new REGISTER());
          } else if (identifier.equals("return")) {
            tokenList.add(new RETURN());
          } else if (identifier.equals("short")) {
            tokenList.add(new SHORT());
          } else if (identifier.equals("signed")) {
            tokenList.add(new SIGNED());
          } else if (identifier.equals("sizeof")) {
            tokenList.add(new SIZEOF());
          } else if (identifier.equals("static")) {
            tokenList.add(new STATIC());
          } else if (identifier.equals("struct")) {
            tokenList.add(new STRUCT());
          } else if (identifier.equals("switch")) {
            tokenList.add(new SWITCH());
          } else if (identifier.equals("typedef")) {
            tokenList.add(new TYPEDEF());
          } else if (identifier.equals("union")) {
            tokenList.add(new UNION());
          } else if (identifier.equals("unsigned")) {
            tokenList.add(new UNSIGNED());
          } else if (identifier.equals("void")) {
            tokenList.add(new VOID());
          } else if (identifier.equals("volatile")) {
            tokenList.add(new VOLATILE());
          } else if (identifier.equals("while")) {
            tokenList.add(new WHILE());
          } else {
            tokenList.add(new IDENTIFIER(identifier));
          }
        }

        else if (' ' == string.charAt(0) || '\t' == string.charAt(0)) {
          text.deleteCharAt(0);
        }

        else {
          // assert false : "unexpected situation: " + string;
          text.delete(0, 1);
        }
      }

      else if (CSTATE.SINGLEQUOTELITERAL == this.states.peek()) {

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
        tokenList.add(new CHARLITERAL(value));

      } else if (CSTATE.DOUBLEQUOTELITERAL == this.states.peek()) {

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
        tokenList.add(new STRINGLITERAL(value));
      }

      else {
        assert false : "unexpected situation: " + string;
        break;
      }
    }

    return tokenList;
  }
}
