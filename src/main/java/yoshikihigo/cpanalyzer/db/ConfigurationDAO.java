package yoshikihigo.cpanalyzer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import yoshikihigo.cpanalyzer.CPAConfig;

public class ConfigurationDAO {

  static public final ConfigurationDAO SINGLETON = new ConfigurationDAO();

  static private final String REPO_TYPE = "repotype";
  static private final String REPO_DIR = "repodir";
  static private final String CURRENT_DIR = "currentdir";
  static private final String BUG_FILE = "bugfile";
  static private final String DATE = "date";
  static private final String USER = "user";
  static public final String CONFIGURATION_SCHEMA = "name string primary key, value string";

  private ConfigurationDAO() {}

  synchronized public void initialize() {

    try {
      final Connection connection = getConnection();
      final Statement statement = connection.createStatement();

      statement.executeUpdate("drop table if exists configuration");

      final StringBuilder createText = new StringBuilder();
      createText.append("create table configuration (")
          .append(CONFIGURATION_SCHEMA)
          .append(")");
      statement.executeUpdate(createText.toString());

      statement.close();
      connection.close();
    }

    catch (final SQLException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  public void setRepoType(final String value) {
    setItem(REPO_TYPE, value);
  }

  public void setRepoDir(final String value) {
    setItem(REPO_DIR, value);
  }

  public void setBugFile(final String value) {
    setItem(BUG_FILE, value);
  }

  public void setDate(final String value) {
    setItem(DATE, value);
  }

  public void setCurrentDir(final String value) {
    setItem(CURRENT_DIR, value);
  }

  public void setUser(final String value) {
    setItem(USER, value);
  }

  synchronized private void setItem(final String name, final String value) {

    try {
      final Connection connection = getConnection();
      final Statement statement = connection.createStatement();

      final StringBuilder deleteText = new StringBuilder();
      deleteText.append("delete from configuration where name = \'")
          .append(name)
          .append("\'");
      System.out.println(deleteText.toString());
      statement.executeUpdate(deleteText.toString());

      final StringBuilder insertText = new StringBuilder();
      insertText.append("insert into configuration values (\'")
          .append(name)
          .append("\', \'")
          .append(value)
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

  public String getRepoType() {
    return getItem(REPO_TYPE);
  }

  public String getRepoDir() {
    return getItem(REPO_DIR);
  }

  public String getBugFile() {
    return getItem(BUG_FILE);
  }

  public String getDate() {
    return getItem(DATE);
  }

  public String getCurrentDir() {
    return getItem(CURRENT_DIR);
  }

  public String getUser() {
    return getItem(USER);
  }

  synchronized private String getItem(final String item) {

    String repoType = null;
    try {
      final Connection connection = getConnection();
      final Statement statement = connection.createStatement();
      final StringBuilder selectText = new StringBuilder();
      selectText.append("select value from configuration where name =\'")
          .append(item)
          .append("\'");
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

}
