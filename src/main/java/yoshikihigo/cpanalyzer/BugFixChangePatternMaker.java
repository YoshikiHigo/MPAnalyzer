package yoshikihigo.cpanalyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BugFixChangePatternMaker {

  private final CPAConfig config;

  public BugFixChangePatternMaker(final CPAConfig config) {
    this.config = config;
  }

  public static void main(final String[] args) {
    final CPAConfig config = CPAConfig.initialize(args);
    BugFixChangePatternMaker maker = new BugFixChangePatternMaker(config);
    maker.perform();
  }

  private void perform() {
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
