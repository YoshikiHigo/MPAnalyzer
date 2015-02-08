package yoshikihigo.cpanalyzer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import yoshikihigo.cpanalyzer.Config;

public class ChangePatternDAO {

	static public final String PATTERNS_SCHEMA = "id integer primary key autoincrement, beforeHash integer, afterHash integer, type integer, support integer, confidence real, nos integer";

	private Connection connector;

	public ChangePatternDAO() {

		try {

			Class.forName("org.sqlite.JDBC");
			final String database = Config.getInstance().getDATABASE();
			this.connector = DriverManager.getConnection("jdbc:sqlite:"
					+ database);
			final Statement statement = connector.createStatement();
			statement.executeUpdate("drop table if exists patterns");
			statement.executeUpdate("create table patterns (" + PATTERNS_SCHEMA
					+ ")");
			statement.close();

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void makeChangePatterns() {

		try {

			System.out.print("making change patterns ...");
			final List<Integer> hashs = new ArrayList<Integer>();
			{
				final Statement statement = this.connector.createStatement();
				final StringBuilder text = new StringBuilder();
				text.append("select beforeHash from changes ");
				text.append("group by beforeHash having count(beforeHash) <> 1");
				final ResultSet result = statement
						.executeQuery(text.toString());
				while (result.next()) {
					hashs.add(result.getInt(1));
				}
				statement.close();
			}

			{
				final StringBuilder text = new StringBuilder();
				text.append("insert into patterns (beforeHash, afterHash, type, support, confidence) ");
				text.append("select A.beforeHash, A.afterHash, A.type, A.times, ");
				text.append("CAST(A.times AS REAL)/(select count(*) from changes where beforeHash=?)");
				text.append("from (select beforeHash, afterHash, type, count(*) times ");
				text.append("from changes where beforeHash=? group by afterHash) A");
				final PreparedStatement statement = this.connector
						.prepareStatement(text.toString());

				int number = 1;
				for (final Integer beforeHash : hashs) {
					if (0 == number % 500) {
						System.out.print(number);
					} else if (0 == number % 100) {
						System.out.print(".");
					}
					if (0 == number % 5000) {
						System.out.println();
					}
					statement.setInt(1, beforeHash);
					statement.setInt(2, beforeHash);
					statement.executeUpdate();
					number++;
				}
				statement.close();
			}
			System.out.println(" done.");

			{
				final Statement statement = this.connector.createStatement();
				statement
						.executeUpdate("create index index_beforeHash_patterns on patterns(beforeHash)");
				statement
						.executeUpdate("create index index_afterHash_patterns on patterns(afterHash)");
				statement
						.executeUpdate("create index index_beforeHash_afterHash_patterns on patterns(beforeHash, afterHash)");
				statement.close();
			}

			System.out.print("calculating metrics ...");
			final List<int[]> hashpairs = new ArrayList<>();
			{
				final Statement statement = this.connector.createStatement();
				final ResultSet result = statement
						.executeQuery("select beforeHash, afterHash from patterns");
				while (result.next()) {
					final int[] hashpair = new int[2];
					hashpair[0] = result.getInt(1);
					hashpair[1] = result.getInt(2);
					hashpairs.add(hashpair);
				}
				statement.close();
			}

			{
				final StringBuilder text = new StringBuilder();
				text.append("update patterns set nos = (select count(distinct software) ");
				text.append("from changes C where C.beforeHash = ? and C.afterHash = ?) ");
				text.append("where beforeHash = ? and afterHash = ?");
				final PreparedStatement statement = this.connector
						.prepareStatement(text.toString());

				int number = 1;
				for (final int[] hashpair : hashpairs) {
					if (0 == number % 1000) {
						System.out.print(number);
					} else if (0 == number % 100) {
						System.out.print(".");
					}
					if (0 == number % 5000) {
						System.out.println();
					}
					statement.setInt(1, hashpair[0]);
					statement.setInt(2, hashpair[1]);
					statement.setInt(3, hashpair[0]);
					statement.setInt(4, hashpair[1]);
					statement.executeUpdate();
					number++;
				}
				statement.close();
			}
			System.out.println(" done.");

		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void close() {
		try {
			this.connector.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}