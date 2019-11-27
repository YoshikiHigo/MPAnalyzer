package yoshikihigo.cpanalyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BugFixChangePatternMaker {

  public static void main(final String[] args) {
    CPAConfig.initialize(args);
    BugFixChangePatternMaker main = new BugFixChangePatternMaker();
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
          "update patterns set bugfix = (select sum(C.bugfix) from changes C where C.beforeHash = patterns.beforeHash and C.afterHash = patterns.afterHash)");
      statementA.close();

    } catch (SQLException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
