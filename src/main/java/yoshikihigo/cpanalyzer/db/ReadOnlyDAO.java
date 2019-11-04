package yoshikihigo.cpanalyzer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Change.ChangeType;
import yoshikihigo.cpanalyzer.data.Change.DiffType;
import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.data.Code;
import yoshikihigo.cpanalyzer.data.Revision;

public class ReadOnlyDAO {

  static public final ReadOnlyDAO SINGLETON = new ReadOnlyDAO();

  private Connection connector;

  private ReadOnlyDAO() {}

  synchronized public void initialize() {

    try {
      Class.forName("org.sqlite.JDBC");
      final String database = CPAConfig.getInstance()
          .getDATABASE();
      this.connector = DriverManager.getConnection("jdbc:sqlite:" + database);
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  synchronized public List<Change> getChanges(final byte[] beforeHash, final byte[] afterHash) {

    final List<Change> changes = new ArrayList<Change>();

    try {
      final StringBuilder text = new StringBuilder();
      text.append("select T.software, T.id, T.filepath, T.author, T.beforeHash, T.beforeRText, ");
      text.append(
          "T.beforeNText, T.beforeStart, T.beforeEnd, T.afterHash, T.afterRText, T.afterNText, ");
      text.append(
          "T.afterStart, T.afterEnd, T.revision, T.changetype, T.difftype, T.date, T.message from ");
      text.append(
          "(select M.software software, M.id id, M.filepath filepath, M.author author, M.beforeHash beforeHash, ");
      text.append("(select C2.rtext from codes C2 where C2.id = M.beforeID) beforeRText, ");
      text.append("(select C3.ntext from codes C3 where C3.id = M.beforeID) beforeNText, ");
      text.append("(select C4.start from codes C4 where C4.id = M.beforeID) beforeStart, ");
      text.append("(select C5.end from codes C5 where C5.id = M.beforeID) beforeEnd, ");
      text.append("M.afterHash afterHash, ");
      text.append("(select C6.rText from codes C6 where C6.id = M.afterID) afterRText, ");
      text.append("(select C7.nText from codes C7 where C7.id = M.afterID) afterNText, ");
      text.append("(select C8.start from codes C8 where C8.id = M.afterID) afterStart, ");
      text.append("(select C9.end from codes C9 where C9.id = M.afterID) afterEnd, ");
      text.append("M.revision revision, ");
      text.append("M.changetype changetype, ");
      text.append("M.difftype difftype, ");
      text.append("(select R1.date from revisions R1 where R1.id = M.revision) date, ");
      text.append("(select R2.message from revisions R2 where R2.id = M.revision) message ");
      text.append("from changes M) T where T.beforeHash=? and T.afterHash=?");
      final PreparedStatement statement = this.connector.prepareStatement(text.toString());

      statement.setBytes(1, beforeHash);
      statement.setBytes(2, afterHash);
      final ResultSet result = statement.executeQuery();

      while (result.next()) {
        final String software = result.getString(1);
        final int changeID = result.getInt(2);
        final String filepath = result.getString(3);
        final String author = result.getString(4);
        final int beforeID = result.getInt(5);
        final String beforeRText = result.getString(6);
        final String beforeNText = result.getString(7);
        final int beforeStart = result.getInt(8);
        final int beforeEnd = result.getInt(9);
        final int afterID = result.getInt(10);
        final String afterRText = result.getString(11);
        final String afterNText = result.getString(12);
        final int afterStart = result.getInt(13);
        final int afterEnd = result.getInt(14);
        final String revisionID = result.getString(15);
        final ChangeType changeType = ChangeType.getType(result.getInt(16));
        final DiffType diffType = DiffType.getType(result.getInt(17));
        final String date = result.getString(18);
        final String message = result.getString(19);

        final Code beforeCode =
            new Code(software, beforeID, beforeRText, beforeNText, beforeStart, beforeEnd);
        final Code afterCode =
            new Code(software, afterID, afterRText, afterNText, afterStart, afterEnd);
        final Revision revision = new Revision(software, revisionID, date, message, author);
        final Change change = new Change(software, changeID, filepath, beforeCode, afterCode,
            revision, changeType, diffType);
        changes.add(change);
      }
      statement.close();
    }

    catch (SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }

    return changes;
  }

  synchronized public List<ChangePattern> getChangePatterns() {
    return this.getChangePatterns(1, 0.0f);
  }

  synchronized public List<ChangePattern> getChangePatterns(final int supportThreshold,
      final float confidenceThreshold) {

    final List<ChangePattern> patterns = new ArrayList<>();

    try {
      final StringBuilder text = new StringBuilder();
      text.append(
          "select id, beforeHash, afterHash, changetype, difftype, support, confidence, authors, files, nos");
      text.append(" from patterns where ? <= support and ? <= confidence");
      final PreparedStatement statement = this.connector.prepareStatement(text.toString());

      statement.setInt(1, supportThreshold);
      statement.setFloat(2, confidenceThreshold);
      final ResultSet result = statement.executeQuery();

      while (result.next()) {
        final int id = result.getInt(1);
        final byte[] beforeHash = result.getBytes(2);
        final byte[] afterHash = result.getBytes(3);
        final ChangeType changeType = ChangeType.getType(result.getInt(4));
        final DiffType diffType = DiffType.getType(result.getInt(5));
        final int support = result.getInt(6);
        final float confidence = result.getFloat(7);
        final int authors = result.getInt(8);
        final int files = result.getInt(9);
        final int nos = result.getInt(10);
        final ChangePattern pattern = new ChangePattern(id, support, confidence, authors, files,
            nos, beforeHash, afterHash, changeType, diffType);
        patterns.add(pattern);
      }

      statement.close();
    }

    catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }

    return patterns;
  }

  synchronized public SortedSet<Revision> getRevisions() throws Exception {

    final Statement revisionStatement = this.connector.createStatement();
    final ResultSet result =
        revisionStatement.executeQuery("select software, id, date, message, author from revision");

    final SortedSet<Revision> revisions = new TreeSet<Revision>();
    while (result.next()) {
      final String software = result.getString(1);
      final String id = result.getString(2);
      final String date = result.getString(3);
      final String message = result.getString(4);
      final String author = result.getString(5);
      final Revision revision = new Revision(software, id, date, message, author);
      revisions.add(revision);
    }

    return revisions;
  }

  synchronized public List<Code> getCode(final byte[] hash) {

    final List<Code> codes = new ArrayList<>();
    final String text = "select software, id, rText, nText, start, end from codes where hash = ?";

    try {
      final PreparedStatement statement = this.connector.prepareStatement(text);
      statement.setBytes(1, hash);
      final ResultSet result = statement.executeQuery();
      while (result.next()) {
        final String software = result.getString(1);
        final int id = result.getInt(2);
        final String rText = result.getString(3);
        final String nText = result.getString(4);
        final int start = result.getInt(5);
        final int end = result.getInt(6);
        final Code code = new Code(software, id, rText, nText, start, end);
        codes.add(code);
      }

      result.close();
      statement.close();
    }

    catch (final SQLException e) {
      e.printStackTrace();
    }

    return codes;
  }

  synchronized public void close() {
    try {
      this.connector.close();
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
}
