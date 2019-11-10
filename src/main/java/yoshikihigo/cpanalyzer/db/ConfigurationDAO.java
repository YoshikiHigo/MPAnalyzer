package yoshikihigo.cpanalyzer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import yoshikihigo.cpanalyzer.CPAConfig;

public class ConfigurationDAO {

  static public final ConfigurationDAO SINGLETON = new ConfigurationDAO();

  static public final String CONFIGURATION_SCHEMA = "repotype string, " + //
      "repodir string, " + //
      "curentdir string, " + //
      "date string, " + //
      "user string";

  private ConfigurationDAO() {}

  synchronized public void set(final String repoType, final String repoDir, final String currentDir,
      final String date, final String user) {

    try {
      final Connection connection = getConnection();
      final Statement statement = connection.createStatement();

      statement.executeUpdate("drop table if exists configuration");

      final StringBuilder createText = new StringBuilder();
      createText.append("create table configuration (")
          .append(CONFIGURATION_SCHEMA)
          .append(")");
      statement.executeUpdate(createText.toString());

      final StringBuilder insertText = new StringBuilder();
      insertText.append("insert into configuration values (\'")
          .append(repoType)
          .append("\', \'")
          .append(repoDir)
          .append("\', \'")
          .append(currentDir)
          .append("\', \'")
          .append(date)
          .append("\', \'")
          .append(user)
          .append("\')");
      System.out.println(insertText.toString());
      statement.executeUpdate(insertText.toString());

      statement.close();
      connection.close();
    }

    catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  synchronized private Connection getConnection() {

    Connection connection = null;
    try {
      Class.forName("org.sqlite.JDBC");
      final CPAConfig config = CPAConfig.getInstance();
      final String database = config.getDATABASE();
      connection = DriverManager.getConnection("jdbc:sqlite:" + database);
    } catch (final ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
    return connection;
  }

  public String getRepoType() {
    return getItem("repotype");
  }

  public String getRepoDir() {
    return getItem("repodir");
  }

  public String getDate() {
    return getItem("date");
  }

  public String getCurrentDir() {
    return getItem("currentdir");
  }

  public String getUser() {
    return getItem("user");
  }

  synchronized private String getItem(final String item) {

    String repoType = null;
    try {
      final Connection connection = getConnection();
      final Statement statement = connection.createStatement();
      final StringBuilder selectText = new StringBuilder();
      selectText.append("select ")
          .append(item)
          .append(" from configuration");
      final ResultSet results = statement.executeQuery(selectText.toString());
      if (results.next()) {
        repoType = results.getString(1);
      }

      statement.close();
      connection.close();

    } catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
    return repoType;
  }
}
