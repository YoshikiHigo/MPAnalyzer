package yoshikihigo.cpanalyzer.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import yoshikihigo.cpanalyzer.StringUtility;
import yoshikihigo.cpanalyzer.Utility;
import yoshikihigo.cpanalyzer.lexer.token.Token;

public class Code implements Comparable<Code> {

  private final static AtomicInteger ID_GENERATOR = new AtomicInteger();

  public final List<Statement> statements;
  public final String software;
  public final int id;
  public final String rText;
  public final String nText;
  public final String position;
  public final byte[] hash;

  public Code(final String software, final List<Statement> statements) {
    this.software = software;
    this.id = ID_GENERATOR.getAndIncrement();
    this.statements = statements;
    this.rText = String.join(System.lineSeparator(), statements.stream()
        .map(statement -> statement.rText)
        .collect(Collectors.toList()));
    this.nText = String.join(System.lineSeparator(), statements.stream()
        .map(statement -> statement.nText)
        .collect(Collectors.toList()));
    this.position = !statements.isEmpty()
        ? statements.get(0).fromLine + " --- " + Utility.getLast(statements).toLine
        : "not exist.";
    this.hash = this.getMD5(this.nText);
  }

  public Code(final String software, final int id, final String rText, final String nText,
      final int startLine, final int endLine) {
    this.software = software;
    this.id = id;
    this.statements = StringUtility.splitToStatements(nText, startLine, endLine);
    this.rText = rText;
    this.nText = nText;
    this.position = startLine + " --- " + endLine;
    this.hash = this.getMD5(this.nText);
  }

  @Override
  public boolean equals(final Object o) {

    if (!(o instanceof Code)) {
      return false;
    }

    final Code target = (Code) o;
    if (this.statements.size() != target.statements.size()) {
      return false;
    }

    for (int i = 0; i < this.statements.size(); i++) {
      if (this.statements.get(i).hash != target.statements.get(i).hash) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.hash);
  }

  @Override
  public String toString() {
    return this.nText;
  }

  @Override
  public int compareTo(final Code o) {
    return this.nText.compareTo(o.nText);
  }

  public int getStartLine() {
    if (this.statements.isEmpty()) {
      return 0;
    } else {
      return this.statements.get(0).fromLine;
    }
  }

  public int getEndLine() {
    if (this.statements.isEmpty()) {
      return 0;
    } else {
      return this.statements.get(this.statements.size() - 1).toLine;
    }
  }

  public List<Token> getTokens() {
    final List<Token> tokens = new ArrayList<Token>();
    for (final Statement statement : this.statements) {
      tokens.addAll(statement.tokens);
    }
    return tokens;
  }

  private byte[] getMD5(final String text) {
    try {
      final MessageDigest md = MessageDigest.getInstance("MD5");
      final byte[] data = text.getBytes(StandardCharsets.UTF_8);
      md.update(data);
      final byte[] digest = md.digest();
      return digest;
    } catch (final NoSuchAlgorithmException e) {
      e.printStackTrace();
      return new byte[0];
    }
  }
}
