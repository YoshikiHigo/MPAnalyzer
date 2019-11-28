package yoshikihigo.cpanalyzer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Revision;

public class ChangeDAO {

  static public ChangeDAO SINGLETON = new ChangeDAO();

  static public final String REVISIONS_SCHEMA = "repo string, " + //
      "id string, " + //
      "date string, " + //
      "message string, " + //
      "author string, " + //
      "bugfix int, " + //
      "primary key(repo, id)";
  static public final String CODES_SCHEMA = "repo string, " + //
      "id integer, " + //
      "rText string, " + //
      "nText string, " + //
      "hash blob, " + //
      "start int, " + //
      "end int, " + //
      "primary key(repo, id)";
  static public final String CHANGES_SCHEMA = "repo string, " //
      + "id integer, " + //
      "filepath string, " + //
      "author string, " + //
      "beforeID integer, " + //
      "beforeHash blob, " + //
      "afterID integer, " + //
      "afterHash blob, " + //
      "revision string, " + //
      "date string, " + //
      "changetype int, " + //
      "difftype int, " + //
      "bugfix int, " + //
      "primary key(repo, id)";

  private Connection connector;
  private PreparedStatement codePS;
  private PreparedStatement changePS;
  private int numberOfCodePS;
  private int numberOfChangePS;
  private CPAConfig config;

  private ChangeDAO() {}

  synchronized public void initialize(final CPAConfig config) {

    this.config = config;

    try {
      Class.forName("org.sqlite.JDBC");
      final String database = config.getDATABASE();
      this.connector = DriverManager.getConnection("jdbc:sqlite:" + database);

      final Statement statement = this.connector.createStatement();
      statement.executeUpdate("create table if not exists revisions (" + REVISIONS_SCHEMA + ")");
      statement.executeUpdate("create table if not exists codes (" + CODES_SCHEMA + ")");
      statement.executeUpdate("create table if not exists changes (" + CHANGES_SCHEMA + ")");
      statement.close();

      this.codePS =
          this.connector.prepareStatement("insert into codes values (?, ?, ?, ?, ?, ?, ?)");
      this.changePS = this.connector
          .prepareStatement("insert into changes values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

      this.numberOfCodePS = 0;
      this.numberOfChangePS = 0;
      this.connector.setAutoCommit(false);
    }

    catch (final ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  synchronized public void addRevisions(final Revision[] revisions) {

    try {
      final PreparedStatement statement =
          this.connector.prepareStatement("insert into revisions values (?, ?, ?, ?, ?, ?)");
      for (final Revision revision : revisions) {
        statement.setString(1, revision.repo);
        statement.setString(2, revision.id);
        statement.setString(3, revision.date);
        statement.setString(4, revision.message);
        statement.setString(5, revision.author);
        statement.setInt(6, revision.bugfix ? 1 : 0);
        statement.addBatch();
      }
      statement.executeBatch();
      this.connector.commit();
      statement.close();
    }

    catch (final SQLException e) {
      System.err.print(e.getMessage());
      // System.exit(0);
    }
  }

  synchronized public void addChange(final Change change) {

    try {
      this.codePS.setString(1, change.before.repo);
      this.codePS.setInt(2, change.before.id);
      this.codePS.setString(3, change.before.rText);
      this.codePS.setString(4, change.before.nText);
      this.codePS.setBytes(5, change.before.hash);
      final int beforeStart =
          change.before.statements.isEmpty() ? 0 : change.before.statements.get(0).fromLine;
      final int beforeEnd = change.before.statements.isEmpty() ? 0
          : change.before.statements.get(change.before.statements.size() - 1).toLine;
      this.codePS.setInt(6, beforeStart);
      this.codePS.setInt(7, beforeEnd);
      this.codePS.addBatch();
      this.numberOfCodePS++;

      this.codePS.setString(1, change.after.repo);
      this.codePS.setInt(2, change.after.id);
      this.codePS.setString(3, change.after.rText);
      this.codePS.setString(4, change.after.nText);
      this.codePS.setBytes(5, change.after.hash);
      final int afterStart =
          change.after.statements.isEmpty() ? 0 : change.after.statements.get(0).fromLine;
      final int afterEnd = change.after.statements.isEmpty() ? 0
          : change.after.statements.get(change.after.statements.size() - 1).toLine;
      this.codePS.setInt(6, afterStart);
      this.codePS.setInt(7, afterEnd);
      this.codePS.addBatch();
      this.numberOfCodePS++;

      this.changePS.setString(1, change.repo);
      this.changePS.setInt(2, change.id);
      this.changePS.setString(3, change.filepath);
      this.changePS.setString(4, change.revision.author);
      this.changePS.setInt(5, change.before.id);
      this.changePS.setBytes(6, change.before.hash);
      this.changePS.setInt(7, change.after.id);
      this.changePS.setBytes(8, change.after.hash);
      this.changePS.setString(9, change.revision.id);
      this.changePS.setString(10, change.revision.date);
      this.changePS.setInt(11, change.changeType.getValue());
      this.changePS.setInt(12, change.diffType.getValue());
      this.changePS.setInt(13, change.revision.bugfix ? 1 : 0);
      this.changePS.addBatch();
      this.numberOfChangePS++;

      if (10000 < this.numberOfCodePS) {
        if (config.isVERBOSE()) {
          System.out.println("writing \'codes\' table ...");
        }
        this.codePS.executeBatch();
        this.connector.commit();
        this.numberOfCodePS = 0;
      }

      if (10000 < this.numberOfChangePS) {
        if (config.isVERBOSE()) {
          System.out.println("writing \'changes\' table ...");
        }
        this.changePS.executeBatch();
        this.connector.commit();
        this.numberOfChangePS = 0;
      }
    }

    catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  synchronized public void addChanges(Collection<Change> changes) {
    changes.stream()
        .forEach(change -> this.addChange(change));
  }

  synchronized public void flush() {
    try {
      if (0 < this.numberOfCodePS) {
        if (config.isVERBOSE()) {
          System.out.println("writing \'codes\' table ...");
        }
        this.codePS.executeBatch();
        this.connector.commit();
        this.numberOfCodePS = 0;
      }
      if (0 < this.numberOfChangePS) {
        if (config.isVERBOSE()) {
          System.out.println("writing \'changes\' table ...");
        }
        this.changePS.executeBatch();
        this.connector.commit();
        this.numberOfChangePS = 0;
      }
    } catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  synchronized public void close() {
    try {
      this.codePS.close();
      this.changePS.close();
      this.connector.close();
    } catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
}
