package yoshikihigo.cpanalyzer.data;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.lexer.token.ABSTRACT;
import yoshikihigo.cpanalyzer.lexer.token.ANNOTATION;
import yoshikihigo.cpanalyzer.lexer.token.BOOLEANLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.CHARLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.CLASS;
import yoshikihigo.cpanalyzer.lexer.token.COLON;
import yoshikihigo.cpanalyzer.lexer.token.DEF;
import yoshikihigo.cpanalyzer.lexer.token.FALSE;
import yoshikihigo.cpanalyzer.lexer.token.FINAL;
import yoshikihigo.cpanalyzer.lexer.token.IDENTIFIER;
import yoshikihigo.cpanalyzer.lexer.token.INTERFACE;
import yoshikihigo.cpanalyzer.lexer.token.LEFTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.LEFTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.LEFTSQUAREBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.LESS;
import yoshikihigo.cpanalyzer.lexer.token.LINEEND;
import yoshikihigo.cpanalyzer.lexer.token.LINEINTERRUPTION;
import yoshikihigo.cpanalyzer.lexer.token.NUMBERLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.PRIVATE;
import yoshikihigo.cpanalyzer.lexer.token.PROTECTED;
import yoshikihigo.cpanalyzer.lexer.token.PUBLIC;
import yoshikihigo.cpanalyzer.lexer.token.QUESTION;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTPAREN;
import yoshikihigo.cpanalyzer.lexer.token.RIGHTSQUAREBRACKET;
import yoshikihigo.cpanalyzer.lexer.token.SEMICOLON;
import yoshikihigo.cpanalyzer.lexer.token.STATIC;
import yoshikihigo.cpanalyzer.lexer.token.STRICTFP;
import yoshikihigo.cpanalyzer.lexer.token.STRINGLITERAL;
import yoshikihigo.cpanalyzer.lexer.token.TAB;
import yoshikihigo.cpanalyzer.lexer.token.TRANSIENT;
import yoshikihigo.cpanalyzer.lexer.token.TRUE;
import yoshikihigo.cpanalyzer.lexer.token.Token;
import yoshikihigo.cpanalyzer.lexer.token.WHITESPACE;

public class Statement {

  public static List<Statement> getJCStatements(final List<Token> allTokens)
      throws EmptyStackException {

    final CPAConfig config = CPAConfig.getInstance();

    final List<Statement> statements = new ArrayList<>();
    List<Token> buildingStatementTokens = new ArrayList<>();

    final Stack<Integer> nestLevel = new Stack<>();
    nestLevel.push(Integer.valueOf(1));
    int inAnnotationDepth = 0;
    int inParenDepth = 0;
    int inTernaryOperationDepth = 0;
    int index = 0;
    Token previousToken = null;
    final boolean isDebug = config.isDEBUG();

    try {
      for (final Token token : allTokens) {

        // token.index = index++;
        if (0 < inAnnotationDepth) {
          final ANNOTATION annotation = new ANNOTATION(token.value);
          annotation.index = index++;
          annotation.line = token.line;
          buildingStatementTokens.add(annotation);
        } else {
          token.index = index++;
          buildingStatementTokens.add(token);
        }

        if ((0 == inParenDepth) && (token instanceof RIGHTBRACKET)) {
          if (0 == nestLevel.peek()
              .intValue()) {
            nestLevel.pop();
            nestLevel.pop();
          } else {
            nestLevel.pop();
          }
        }

        if ((token instanceof QUESTION) && !(previousToken instanceof LESS)) {
          inTernaryOperationDepth++;
        }

        if (token instanceof RIGHTPAREN) {
          inParenDepth--;
          if (0 < inAnnotationDepth) {
            inAnnotationDepth--;
          }
        }

        if ((0 == inParenDepth) && (0 == inTernaryOperationDepth)
            && (token instanceof LEFTBRACKET || token instanceof RIGHTBRACKET
                || token instanceof SEMICOLON || token instanceof COLON)) {

          if (1 < buildingStatementTokens.size()) {

            if (isJCTypeDefinition(buildingStatementTokens)) {
              nestLevel.push(Integer.valueOf(0));
            }
            final int nestDepth = nestLevel.peek()
                .intValue();

            final int fromLine = buildingStatementTokens.get(0).line;
            final int toLine = buildingStatementTokens.get(buildingStatementTokens.size() - 1).line;
            final String rText = makeText(buildingStatementTokens);
            final List<Token> nonTrivialTokens = config.isCOUNT_MODIFIER() ? buildingStatementTokens
                : removeJCTrivialTokens(buildingStatementTokens);
            // final List<Token> normalizedTokens = config.isNORMALIZATION()
            // ? normalizeJCTokens(nonTrivialTokens)
            // : nonTrivialTokens;
            final List<Token> normalizedTokens = normalizeJCTokens(nonTrivialTokens);
            final String nText = makeText(normalizedTokens);
            final byte[] hash = getMD5(nText);
            final Statement statement = new Statement(fromLine, toLine, nestDepth, 1 < nestDepth,
                buildingStatementTokens, rText, nText, hash);
            statements.add(statement);
            buildingStatementTokens = new ArrayList<Token>();

            if (isDebug) {
              System.out.println(statement.toString());
            }
          }

          else {
            buildingStatementTokens.clear();
          }
        }

        if ((0 == inParenDepth) && (token instanceof LEFTBRACKET)) {
          nestLevel.push(Integer.valueOf(nestLevel.peek()
              .intValue() + 1));
        }

        if ((0 < inTernaryOperationDepth) && (token instanceof COLON)) {
          inTernaryOperationDepth--;
        }

        if (token instanceof LEFTPAREN) {
          inParenDepth++;
          if ((1 < buildingStatementTokens.size()) && (buildingStatementTokens
              .get(buildingStatementTokens.size() - 2) instanceof ANNOTATION)) {
            inAnnotationDepth++;
            buildingStatementTokens.remove(buildingStatementTokens.size() - 1);
            final ANNOTATION annotation = new ANNOTATION(token.value);
            annotation.index = index++;
            annotation.line = token.line;
            buildingStatementTokens.add(annotation);
          }
        }

        previousToken = token;
      }
    }

    catch (final EmptyStackException e) {
      System.err.println("parsing error has happened.");
    }

    return statements;
  }

  public static List<Statement> getPYStatements(final List<Token> allTokens)
      throws EmptyStackException {

    final List<Statement> statements = new ArrayList<Statement>();
    List<Token> tokens = new ArrayList<Token>();

    final Stack<Integer> methodDefinitionDepth = new Stack<>();

    int nestLevel = 0;
    int index = 0;
    int inParenDepth = 0;
    int inBracketDepth = 0;
    int inSquareBracketDepth = 0;
    boolean interrupted = false;
    boolean isIndent = true;
    final boolean isDebug = CPAConfig.getInstance()
        .isDEBUG();

    try {
      for (final Token token : allTokens) {

        if ((token instanceof TAB) || (token instanceof WHITESPACE)) {
          if (isIndent && !interrupted) {
            nestLevel++;
          }
        } else {
          isIndent = false;
        }

        if (!(token instanceof TAB) && !(token instanceof WHITESPACE)
            && !(token instanceof LINEEND)) {
          token.index = index++;
        }

        if (!(token instanceof TAB) && !(token instanceof WHITESPACE) && !(token instanceof LINEEND)
            && !(token instanceof SEMICOLON) && !(token instanceof LINEINTERRUPTION)) {
          tokens.add(token);
        }

        if (token instanceof RIGHTPAREN) {
          inParenDepth--;
        }

        if (token instanceof LEFTPAREN) {
          inParenDepth++;
        }

        if (token instanceof RIGHTBRACKET) {
          inBracketDepth--;
        }

        if (token instanceof LEFTBRACKET) {
          inBracketDepth++;
        }

        if (token instanceof RIGHTSQUAREBRACKET) {
          inSquareBracketDepth--;
        }

        if (token instanceof LEFTSQUAREBRACKET) {
          inSquareBracketDepth++;
        }

        if (token instanceof LINEINTERRUPTION) {
          interrupted = true;
        } else if (token instanceof LINEEND) {
          // do nothing
        } else {
          interrupted = false;
        }

        // make a statement
        if (!interrupted && (0 == inParenDepth) && (0 == inBracketDepth)
            && (0 == inSquareBracketDepth)
            && ((token instanceof LINEEND) || (token instanceof SEMICOLON))) {
          if (!tokens.isEmpty()) {

            if (!methodDefinitionDepth.isEmpty() && (nestLevel <= methodDefinitionDepth.peek()
                .intValue())) {
              methodDefinitionDepth.pop();
            }

            if (isPYMethodDefinition(tokens)) {
              methodDefinitionDepth.push(Integer.valueOf(nestLevel));
            }

            if (!methodDefinitionDepth.isEmpty() && (nestLevel < methodDefinitionDepth.peek()
                .intValue())) {
              methodDefinitionDepth.pop();
            }

            final int fromLine = tokens.get(0).line;
            final int toLine = tokens.get(tokens.size() - 1).line;
            final boolean isTarget =
                (!methodDefinitionDepth.isEmpty() && (methodDefinitionDepth.peek()
                    .intValue() < nestLevel));
            final String rText = makeText(tokens);
            final String nText = makePYText(tokens);
            final byte[] hash = getMD5(nText);
            final Statement statement =
                new Statement(fromLine, toLine, nestLevel, isTarget, tokens, rText, nText, hash);
            statements.add(statement);
            tokens = new ArrayList<Token>();

            if (isDebug) {
              System.out.println(statement.toString());
            }
          }

          if (token instanceof LINEEND) {
            nestLevel = 0;
            isIndent = true;
          }
        }
      }
    }

    catch (final EmptyStackException e) {
      System.err.println("parsing error has happened.");
    }

    return statements;
  }

  private static List<Token> normalizeJCTokens(final List<Token> tokens) {

    final List<Token> normalizedTokens = new ArrayList<>();
    final Map<String, String> identifiers = new HashMap<>();
    final Map<String, String> types = new HashMap<>();

    for (int index = 0; index < tokens.size(); index++) {

      final Token token = tokens.get(index);

      if (token instanceof IDENTIFIER) {

        if (index < tokens.size() && tokens.get(index + 1) instanceof LEFTPAREN) {
          normalizedTokens.add(token);
        }

        else if (Character.isLowerCase(token.value.charAt(0))) {
          String normalizedValue = identifiers.get(token.value);
          if (null == normalizedValue) {
            normalizedValue = "$V" + identifiers.size();
            identifiers.put(token.value, normalizedValue);
          }
          final IDENTIFIER normalizedIdentifier = new IDENTIFIER(normalizedValue);
          normalizedIdentifier.index = token.index;
          normalizedIdentifier.line = token.line;
          normalizedTokens.add(normalizedIdentifier);
        }

        else if (Character.isUpperCase(token.value.charAt(0))) {
          // String normalizedValue = types.get(token.value);
          // if (null == normalizedValue) {
          // normalizedValue = "$T" + types.size();
          // types.put(token.value, normalizedValue);
          // }
          // builder.append(normalizedValue);
          normalizedTokens.add(token);
        }
      }

      else if (token instanceof CHARLITERAL) {
        // final CHARLITERAL literal = new CHARLITERAL("C");
        final CHARLITERAL literal = new CHARLITERAL("$L");
        literal.index = token.index;
        literal.line = token.line;
        normalizedTokens.add(literal);
      }

      else if (token instanceof NUMBERLITERAL) {
        // final NUMBERLITERAL literal = new NUMBERLITERAL("N");
        final NUMBERLITERAL literal = new NUMBERLITERAL("$L");
        literal.index = token.index;
        literal.line = token.line;
        normalizedTokens.add(literal);
      }

      else if (token instanceof STRINGLITERAL) {
        // final STRINGLITERAL literal = new STRINGLITERAL("S");
        final STRINGLITERAL literal = new STRINGLITERAL("$L");
        literal.index = token.index;
        literal.line = token.line;
        normalizedTokens.add(literal);
      }

      else if ((token instanceof TRUE) || (token instanceof FALSE)) {
        // final BOOLEANLITERAL literal = new BOOLEANLITERAL("B");
        final BOOLEANLITERAL literal = new BOOLEANLITERAL("$L");
        literal.index = token.index;
        literal.line = token.line;
        normalizedTokens.add(literal);
      }

      else {
        normalizedTokens.add(token);
      }
    }

    return normalizedTokens;
  }

  private static String makePYText(final List<Token> tokens) {

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
    builder.deleteCharAt(builder.length() - 1);
    return builder.toString();
  }

  private static String makeText(final List<Token> tokens) {
    final String[] array = tokens.stream()
        .map(token -> token.value)
        .collect(Collectors.toList())
        .toArray(new String[0]);
    final String text = String.join(" ", array);
    return text;
  }

  private static List<Token> removeJCTrivialTokens(final List<Token> tokens) {
    final List<Token> nonTrivialTokens = new ArrayList<>();
    for (final Token token : tokens) {

      if (token instanceof ABSTRACT || token instanceof FINAL || token instanceof PRIVATE
          || token instanceof PROTECTED || token instanceof PUBLIC || token instanceof STATIC
          || token instanceof STRICTFP || token instanceof TRANSIENT) {
        // not used for making hash
        continue;
      }

      else if (token instanceof ANNOTATION) {
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
  final public String rText;
  final public String nText;
  final public byte[] hash;

  public Statement(final int fromLine, final int toLine, final int nestLevel,
      final boolean isTarget, final List<Token> tokens, final String rText, final String nText,
      final byte[] hash) {
    this.fromLine = fromLine;
    this.toLine = toLine;
    this.tokens = tokens;
    this.nestLevel = nestLevel;
    this.isTarget = isTarget;
    this.rText = rText;
    this.nText = nText;
    this.hash = Arrays.copyOf(hash, hash.length);
  }

  @Override
  public int hashCode() {
    final BigInteger value = new BigInteger(1, this.hash);
    return value.toString(16)
        .hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (null == o) {
      return false;
    }
    if (!(o instanceof Statement)) {
      return false;
    }

    return Arrays.equals(this.hash, ((Statement) o).hash);
  }

  @Override
  public String toString() {
    return this.nText;
  }
}
