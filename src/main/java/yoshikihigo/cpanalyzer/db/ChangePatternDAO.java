package yoshikihigo.cpanalyzer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import yoshikihigo.cpanalyzer.CPAConfig;

public class ChangePatternDAO {

  static public ChangePatternDAO SINGLETON = new ChangePatternDAO();

  static public final String PATTERNS_SCHEMA = "id integer primary key autoincrement, " + //
      "beforeHash blob, " + //
      "afterHash blob, " + //
      "changetype integer, " + //
      "difftype integer, " + //
      "support integer, " + //
      "confidence real, " + //
      "authors integer, " + //
      "files integer, " + //
      "nos integer, " + //
      "firstdate string, " + //
      "lastdate string, " + //
      "bugfix int";

  private Connection connector;
  private CPAConfig config;

  private ChangePatternDAO() {}

  synchronized public boolean initialize(final CPAConfig config) {

    this.config = config;

    try {

      Class.forName("org.sqlite.JDBC");
      final String database = config.getDATABASE();
      this.connector = DriverManager.getConnection("jdbc:sqlite:" + database);
      final Statement statement = this.connector.createStatement();

      {
        final boolean force = config.isFORCE();
        final ResultSet result = statement.executeQuery(
            "select count(*) from sqlite_master where type='table' and name='patterns'");
        if (result.next()) {
          final int value = result.getInt(1);
          if (!force && (0 < value)) {
            System.out.println("table \"patterns\" already exists.");
            statement.close();
            return false;
          }
        } else {
          System.out.println("database is invalid.");
          statement.close();
          System.exit(0);
        }
      }

      statement.executeUpdate("drop index if exists index_beforeHash_patterns");
      statement.executeUpdate("drop index if exists index_afterHash_patterns");
      statement.executeUpdate("drop index if exists index_beforeHash_afterHash_patterns");
      statement.executeUpdate("drop index if exists index_changetype_patterns");
      statement.executeUpdate("drop index if exists index_difftype_patterns");
      statement.executeUpdate("drop index if exists index_support_patterns");
      statement.executeUpdate("drop index if exists index_confidence_patterns");
      statement.executeUpdate("drop index if exists index_nos_patterns");

      statement.executeUpdate("drop table if exists patterns");
      statement.executeUpdate("create table patterns (" + PATTERNS_SCHEMA + ")");

      statement.executeUpdate("drop index if exists index_id_codes");
      statement.executeUpdate("drop index if exists index_hash_codes");
      statement.executeUpdate("drop index if exists index_nText_codes");
      statement.executeUpdate("drop index if exists index_beforeHash_changes");
      statement.executeUpdate("drop index if exists index_afterHash_changes");
      statement.executeUpdate("drop index if exists index_beforeHash_afterHash_changes");
      statement.close();
    }

    catch (final ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }

    return true;
  }

  synchronized public void makeIndicesOnCODES() {

    try {
      final Statement statement = this.connector.createStatement();
      statement.executeUpdate("create index index_id_codes on codes(id)");
      statement.executeUpdate("create index index_hash_codes on codes(hash)");
      statement.executeUpdate("create index index_nText_codes on codes(nText)");
      statement.close();
    }

    catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  synchronized public void makeIndicesOnCHANGES() {

    try {
      final Statement statement = this.connector.createStatement();
      statement.executeUpdate("create index index_beforeHash_changes on changes(beforeHash)");
      statement.executeUpdate("create index index_afterHash_changes on changes(afterHash)");
      statement.executeUpdate(
          "create index index_beforeHash_afterHash_changes on changes(beforeHash, afterHash)");
      statement.close();
    }

    catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  synchronized public void makeChangePatterns() {

    try {

      if (!config.isQUIET()) {
        System.out.print("making change patterns ...");
      }
      final List<byte[]> hashs = new ArrayList<>();
      {
        final boolean isAll = config.isALL();
        final Statement statement = this.connector.createStatement();
        final StringBuilder text = new StringBuilder();
        if (isAll) {
          text.append("select distinct beforeHash from changes ");
        } else {
          text.append("select beforeHash from changes ");
          text.append("group by beforeHash having count(beforeHash) <> 1");
        }
        final ResultSet result = statement.executeQuery(text.toString());
        while (result.next()) {
          hashs.add(result.getBytes(1));
        }
        statement.close();
      }

      {
        final StringBuilder text = new StringBuilder();
        text.append(
            "insert into patterns (beforeHash, afterHash, changetype, difftype, support, confidence) ");
        text.append("select A.beforeHash, A.afterHash, A.changetype, A.difftype, A.times, ");
        text.append("CAST(A.times AS REAL)/(select count(*) from changes where beforeHash=?)");
        text.append("from (select beforeHash, afterHash, changetype, difftype, count(*) times ");
        text.append("from changes where beforeHash=? group by afterHash) A");
        final PreparedStatement statement = this.connector.prepareStatement(text.toString());

        int number = 1;
        for (final byte[] beforeHash : hashs) {
          if (!config.isQUIET()) {
            if (0 == number % 500) {
              System.out.print(number);
            } else if (0 == number % 100) {
              System.out.print(".");
            }
            if (0 == number % 5000) {
              System.out.println();
            }
          }
          statement.setBytes(1, beforeHash);
          statement.setBytes(2, beforeHash);
          statement.executeUpdate();
          number++;
        }
        statement.close();
      }

      {
        final Statement statement = this.connector.createStatement();
        statement.executeUpdate("create index index_beforeHash_patterns on patterns(beforeHash)");
        statement.executeUpdate("create index index_afterHash_patterns on patterns(afterHash)");
        statement.executeUpdate(
            "create index index_beforeHash_afterHash_patterns on patterns(beforeHash, afterHash)");
        statement.executeUpdate("create index index_changetype_patterns on patterns(changetype)");
        statement.executeUpdate("create index index_difftype_patterns on patterns(difftype)");
        statement.close();
      }
      if (!config.isQUIET()) {
        System.out.println(" done.");
      }

      if (!config.isQUIET()) {
        System.out.print("calculating metrics ...");
      }
      final List<byte[][]> hashpairs = new ArrayList<>();
      {
        final Statement statement = this.connector.createStatement();
        final ResultSet result =
            statement.executeQuery("select beforeHash, afterHash from patterns");
        while (result.next()) {
          final byte[][] hashpair = new byte[2][];
          hashpair[0] = result.getBytes(1);
          hashpair[1] = result.getBytes(2);
          hashpairs.add(hashpair);
        }
        statement.close();
      }

      {
        final String text = "update patterns "
            + "set authors = (select count(distinct author) from changes C1 where C1.beforeHash = ? and C1.afterHash = ?), "
            + "files = (select count(distinct filepath) from changes C2 where C2.beforeHash = ? and C2.afterHash = ?), "
            + "nos = (select count(distinct repo) from changes C3 where C3.beforeHash = ? and C3.afterHash = ?), "
            + "firstdate = (select date from changes C4 where C4.beforeHash = ? and C4.afterHash = ? order by date asc limit 1), "
            + "lastdate = (select date from changes C5 where C5.beforeHash = ? and C5.afterHash = ? order by date desc limit 1), "
            + "bugfix = (select sum(C6.bugfix) from changes C6 where C6.beforeHash = ? and C6.afterHash = ?) "
            + "where beforeHash = ? and afterHash = ?";
        final PreparedStatement statement = this.connector.prepareStatement(text);

        int number = 1;
        for (final byte[][] hashpair : hashpairs) {
          if (!config.isQUIET()) {
            if (0 == number % 1000) {
              System.out.print(number);
            } else if (0 == number % 100) {
              System.out.print(".");
            }
            if (0 == number % 5000) {
              System.out.println();
            }
          }
          statement.setBytes(1, hashpair[0]);
          statement.setBytes(2, hashpair[1]);
          statement.setBytes(3, hashpair[0]);
          statement.setBytes(4, hashpair[1]);
          statement.setBytes(5, hashpair[0]);
          statement.setBytes(6, hashpair[1]);
          statement.setBytes(7, hashpair[0]);
          statement.setBytes(8, hashpair[1]);
          statement.setBytes(9, hashpair[0]);
          statement.setBytes(10, hashpair[1]);
          statement.setBytes(11, hashpair[0]);
          statement.setBytes(12, hashpair[1]);
          statement.setBytes(13, hashpair[0]);
          statement.setBytes(14, hashpair[1]);
          statement.executeUpdate();
          number++;
        }
        statement.close();
      }

      {
        final Statement statement = this.connector.createStatement();
        statement.executeUpdate("create index index_support_patterns on patterns(support)");
        statement.executeUpdate("create index index_confidence_patterns on patterns(confidence)");
        statement.executeUpdate("create index index_authors_patterns on patterns(authors)");
        statement.executeUpdate("create index index_files_patterns on patterns(files)");
        statement.executeUpdate("create index index_nos_patterns on patterns(nos)");
        statement.close();
      }
      if (!config.isQUIET()) {
        System.out.println(" done.");
      }
    }

    catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
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
