package yoshikihigo.cpanalyzer;

public class BugFixAllMaker {

  public static void main(final String[] args) {
    BugFixRevisionExtractor.main(args);
    BugFixChangeMaker.main(args);
    BugFixChangePatternMaker.main(args);
  }
}
