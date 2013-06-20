package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;

public class ModificationPatternPrint {

	public static void main(String[] args) {

		try {

			final long startTime = System.nanoTime();

			final String PATTERNFILE = Config.getPATH_TO_PATTERNSFILE();
			final int SUPPORT = Config.getSupportValue();
			final float CONFIDENCE = Config.getConfidenceValue();

			System.out.println("outputing to " + PATTERNFILE + " ... ");

			final BufferedWriter writer = new BufferedWriter(new FileWriter(
					PATTERNFILE));

			final List<ModificationPattern> patterns = ReadOnlyDAO
					.getInstance().getModificationPatterns(SUPPORT, CONFIDENCE);
			for (final ModificationPattern pattern : patterns) {

				writer.write("===== ID: ");
				writer.write(Integer.toString(pattern.id));
				writer.write(", support: ");
				writer.write(Integer.toString(pattern.support));
				writer.write(", confidence: ");
				writer.write(Float.toString(pattern.confidence));
				writer.write(" =====");
				writer.newLine();

				final List<Modification> modifications = pattern
						.getModifications();
				for (final Modification modification : modifications) {
					writer.write(Long.toString(modification.revision.number));
					writer.write(", ");
					writer.write(modification.revision.date);
					writer.write(", ");
					writer.write(modification.filepath);
					writer.newLine();
				}

				final String beforeText = pattern.getModifications().get(0).before.text;
				final String afterText = pattern.getModifications().get(0).after.text;

				writer.write(beforeText);
				writer.write(" ----- ");
				writer.newLine();
				writer.write(afterText);

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
