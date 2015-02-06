package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;

public class MPCSVPrinter {

	public static void main(String[] args) {

		try {

			final long startTime = System.nanoTime();

			Config.initialize(args);
			final String CVSFILE = Config.getInstance().getCSV_FILE();

			System.out.println("outputing to " + CVSFILE + " ... ");

			final BufferedWriter writer = new BufferedWriter(new FileWriter(
					CVSFILE));

			writer.write("ID, SUPPORT, CONFIDENCE, EREVISION, SREVISION, EDATE, SDATE, NOD, NOR, NOF, LBM, LAM");
			writer.newLine();

			final List<ModificationPattern> patterns = ReadOnlyDAO
					.getInstance().getModificationPatterns(0, 0);
			for (final ModificationPattern pattern : patterns) {

				writer.write(Integer.toString(pattern.id));
				writer.write(", ");
				writer.write(Integer.toString(pattern.support));
				writer.write(", ");
				writer.write(Float.toString(pattern.confidence));
				writer.write(", ");

				final List<Modification> modifications = pattern
						.getModifications();
				Revision sRevision = modifications.get(0).revision;
				Revision eRevision = modifications.get(0).revision;
				for (final Modification modification : modifications) {
					if (sRevision.number > modification.revision.number) {
						sRevision = modification.revision;
					}
					if (eRevision.number < modification.revision.number) {
						eRevision = modification.revision;
					}
				}

				writer.write(Long.toString(eRevision.number));
				writer.write(", ");
				writer.write(Long.toString(sRevision.number));
				writer.write(", ");
				writer.write(eRevision.date);
				writer.write(", ");
				writer.write(sRevision.date);

				writer.write(", ");
				writer.write(Integer.toString(pattern.getNOD()));
				writer.write(", ");
				writer.write(Integer.toString(pattern.getNOR()));
				writer.write(", ");
				writer.write(Integer.toString(pattern.getNOF()));
				writer.write(", ");
				writer.write(Integer.toString(pattern.getLBM()));
				writer.write(", ");
				writer.write(Integer.toString(pattern.getLAM()));

				writer.newLine();
			}

			writer.flush();
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
