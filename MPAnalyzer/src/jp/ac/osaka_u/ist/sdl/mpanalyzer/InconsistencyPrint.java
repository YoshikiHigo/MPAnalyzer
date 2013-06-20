package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Inconsistency;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;

public class InconsistencyPrint {

	public static void main(String[] args) {

		try {

			final long startTime = System.nanoTime();

			final String INCONSISTENCIES = Config
					.getPATH_TO_INCONSISTENCIESFILE();
			final int PLACE = Config.getPlaceValue();

			System.out.println("outputing to " + INCONSISTENCIES + " ... ");

			final BufferedWriter writer = new BufferedWriter(new FileWriter(
					INCONSISTENCIES));

			final List<Inconsistency> inconsistencies = ReadOnlyDAO
					.getInstance().getInconsistency(PLACE);
			for (final Inconsistency inconsistency : inconsistencies) {

				writer.write(" ===== ");
				writer.write(inconsistency.filepath);
				writer.write(" ===== ");
				writer.newLine();

				writer.write("range: ");
				writer.write(Integer.toString(inconsistency.startLine));
				writer.write(" --- ");
				writer.write(Integer.toString(inconsistency.endLine));
				writer.newLine();

				writer.write("matched pattern ID: ");
				writer.write(Integer.toString(inconsistency.patternID));
				writer.newLine();

				writer.write("matched pattern: ");
				writer.write(inconsistency.pattern);
				writer.newLine();

				writer.write("---------- present code ----------");
				writer.newLine();
				writer.write(inconsistency.presentCode);
				writer.newLine();
				writer.write("---------- ------------ ----------");
				writer.newLine();

				writer.write("--------- suggested code ---------");
				writer.newLine();
				writer.write(inconsistency.suggestedCode);
				writer.newLine();
				writer.write("--------- -------------- ---------");
				writer.newLine();
			}
			writer.close();

			final long endTime = System.nanoTime();
			System.out.print("execution time: ");
			System.out.println(TimingUtility.getExecutionTime(startTime,
					endTime));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
