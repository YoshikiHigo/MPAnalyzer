package yoshikihigo.cpanalyzer.data;

public class Inconsistency {

  public final String filepath;
  public final String pattern;
  public final int startLine;
  public final int endLine;
  public final int patternID;
  public final String presentCode;
  public final String suggestedCode;
  public final int support;
  public final float confidence;

  public Inconsistency(final String filepath, final int startLine, final int endLine,
      final String pattern, final int patternID, final String presentCode,
      final String suggestedCode, final int support, final float confidence) {
    this.filepath = filepath;
    this.pattern = pattern;
    this.startLine = startLine;
    this.endLine = endLine;
    this.patternID = patternID;
    this.presentCode = presentCode;
    this.suggestedCode = suggestedCode;
    this.support = support;
    this.confidence = confidence;
  }
}
