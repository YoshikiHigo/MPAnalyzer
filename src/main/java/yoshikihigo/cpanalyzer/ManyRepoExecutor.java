package yoshikihigo.cpanalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;

public class ManyRepoExecutor {

  public static void main(final String[] args) {

    final CPAConfig config = CPAConfig.initialize(args);
    final String db = config.getDATABASE();
    final Path reposFilePath = Paths.get(config.getGITREPOSITIES_FOR_MINING());

    List<String> lines = null;
    try {
      lines = Files.readAllLines(reposFilePath);
    } catch (final IOException e) {
      System.err.println("invalid file path: " + reposFilePath.toString());
      System.exit(1);
    }

    // 各行に対してChangeExtractorを実行
    for (int index = 0; index < lines.size(); index++) {
      final String line = lines.get(index);
      final StringTokenizer tokenizer = new StringTokenizer(line, " ,");
      final String localRepoDir = tokenizer.nextToken();
      final String cve = tokenizer.nextToken();
      final String commitHash = tokenizer.nextToken();

      final StringBuilder builder = new StringBuilder();
      builder.append("[")
          .append(index + 1)
          .append("/")
          .append(lines.size())
          .append("] extracting changes from ")
          .append(line)
          .append(" ...");
      System.out.println(builder.toString());

      final String[] newArgs = {"-db", db, "-i", "-gitrepo", localRepoDir, "-startcommit",
          commitHash, "-endcommit", commitHash, "-q", "-thd", "2"};
      ChangeExtractor.main(newArgs);
    }

    // patternの生成
    final String[] newArgs = {"-db", db, "-q"};
    ChangePatternMaker.main(newArgs);
  }
}
