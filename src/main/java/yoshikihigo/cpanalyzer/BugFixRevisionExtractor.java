package yoshikihigo.cpanalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import com.google.common.base.Splitter;

public class BugFixRevisionExtractor {

  private final CPAConfig config;

  public BugFixRevisionExtractor(final CPAConfig config) {
    this.config = config;
  }

  public static void main(final String[] args) {
    final CPAConfig config = CPAConfig.initialize(args);
    final BugFixRevisionExtractor extractor = new BugFixRevisionExtractor(config);
    extractor.perform();
  }

  private void perform() {
    final String database = config.getDATABASE();
    final Map<String, String> bugIDs = this.getBugIDs();

    try {
      Class.forName("org.sqlite.JDBC");
      final Connection connector = DriverManager.getConnection("jdbc:sqlite:" + database);

      final Statement selectStmt = connector.createStatement();
      final ResultSet selectResults =
          selectStmt.executeQuery("select repo, id, message from revisions");
      final PreparedStatement updateStmt =
          connector.prepareStatement("update revisions set bugfix = ? where repo = ? and id = ?");
      while (selectResults.next()) {
        final String repo = selectResults.getString(1);
        final String id = selectResults.getString(2);
        String message = selectResults.getString(3);
        if (message.contains("git-svn-id")) {
          message = message.substring(0, message.indexOf("git-svn-id"));
        }

        int bugfix = 0;
        final StringBuilder urls = new StringBuilder();
        for (final Entry<String, String> entry : bugIDs.entrySet()) {
          if (message.contains("Merged revisions")) {
            continue;
          }
          final String bugId = entry.getKey();
          if (/* message.contains("CAMEL-")&&!(message.contains("/branches/")) */message
              .endsWith(bugId) || message.contains(bugId + " ") || message.contains(bugId + "\t")
              || message.contains(bugId + '\r') || message.contains(bugId + '\n')
              || message.contains(bugId + ":") || message.contains(bugId + ";")
              || message.contains(bugId + ".")) {
            bugfix++;
            final String url = entry.getValue();
            urls.append(url);
            urls.append(System.lineSeparator());
          }
        }

        updateStmt.setInt(1, bugfix);
        updateStmt.setString(2, repo);
        updateStmt.setString(3, id);
        updateStmt.addBatch();
      }
      selectStmt.close();
      updateStmt.executeBatch();
      updateStmt.close();

    } catch (SQLException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private Map<String, String> getBugIDs() {
    final Path bugFilePath = config.getBUG();
    Map<String, String> map = Collections.emptyMap();

    try {
      map = Files.readAllLines(bugFilePath)
          .stream()
          .collect(
              Collectors.toMap(BugFixRevisionExtractor::getID, BugFixRevisionExtractor::getURL));
    } catch (final IOException e) {
      e.printStackTrace();
      System.exit(0);
    }

    return map;
  }

  static private String getID(final String line) {
    return Splitter.on(',')
        .omitEmptyStrings()
        .trimResults()
        .splitToList(line)
        .get(0);
  }

  static private String getURL(final String line) {
    return Splitter.on(',')
        .omitEmptyStrings()
        .trimResults()
        .splitToList(line)
        .get(1);
  }
}
