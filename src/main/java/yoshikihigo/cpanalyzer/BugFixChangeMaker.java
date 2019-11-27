package yoshikihigo.cpanalyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BugFixChangeMaker {

  public static void main(final String[] args) {
    CPAConfig.initialize(args);
    BugFixChangeMaker main = new BugFixChangeMaker();
    main.make();
  }

  private void make() {
    final CPAConfig config = CPAConfig.getInstance();
    final String database = config.getDATABASE();

    try {
      Class.forName("org.sqlite.JDBC");
      final Connection connector = DriverManager.getConnection("jdbc:sqlite:" + database);

      final Statement statementA = connector.createStatement();
      statementA.executeUpdate(
          "update changes set bugfix = (select R.bugfix from revisions R where id = revision and R.repo = repo)");
      statementA.close();

    } catch (SQLException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
