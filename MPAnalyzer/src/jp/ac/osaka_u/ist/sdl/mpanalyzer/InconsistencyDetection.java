package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Inconsistency;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.SourceFile;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.InconsistencyDAO;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;

public class InconsistencyDetection {

	public static void main(String[] args) throws Exception {
		/*
		final long startTime = System.nanoTime();

		final int SUPPORT = Config.getSupportValue();
		final float CONFIDENCE = Config.getConfidenceValue();

		final String target = Config.getPATH_TO_TARGET();
		final SortedSet<String> files = getFiles(target);

		final List<ModificationPattern> patterns = ReadOnlyDAO.getInstance()
				.getModificationPatterns(SUPPORT, CONFIDENCE);

		final InconsistencyDAO dao = new InconsistencyDAO();

		for (final String file : files) {

			System.out.print("checking ");
			System.out.println(file);

			final SourceFile normalizedFile = new SourceFile(file);
			final LineNumberReader reader = new LineNumberReader(
					new FileReader(file));
			while (reader.ready()) {
				final String line = reader.readLine();
				normalizedFile.addLine(line);
			}
			reader.close();

			for (final ModificationPattern pattern : patterns) {

				final int id = pattern.id;
				final String beforeNormalized = pattern.getModifications().get(
						0).before.text;
				final String afterNormalized = pattern.getModifications()
						.get(0).after.text;
				final int support = pattern.support;
				final float confidence = pattern.confidence;

				final List<SortedSet<Integer>> lineNumbersList = normalizedFile
						.getMatchedLineNumbersList(beforeNormalized,
								afterNormalized);
				if (0 == lineNumbersList.size()) {
					continue;
				}

				final List<String> proposalCode = pattern.getModifications()
						.get(0).after.original;

				for (final SortedSet<Integer> lineNumbers : lineNumbersList) {

					final List<String> presentCode = new ArrayList<String>();
					for (final Integer lineNumber : lineNumbers) {
						final String line = normalizedFile.getLine(lineNumber);
						presentCode.add(StringUtility.addLineHeader(line,
								lineNumber.toString()));
					}

					final int startLine = lineNumbers.first();
					final int endLine = lineNumbers.last();
					final Inconsistency inconsistency = new Inconsistency(
							normalizedFile.filepath, startLine, endLine,
							beforeNormalized, id, presentCode, proposalCode,
							support, confidence);
					dao.addInconsistency(inconsistency);
				}
			}
		}

		dao.close();

		final long endTime = System.nanoTime();
		System.out.print("execution time: ");
		System.out.println(TimingUtility.getExecutionTime(startTime, endTime));
		*/
	}

	private static SortedSet<String> getFiles(final String target)
			throws IOException {

		final String LANGUAGE = Config.getLanguage();

		final SortedSet<String> files = new TreeSet<String>();
		final File targetFile = new File(target);
		if (targetFile.isFile()) {
			if (LANGUAGE.equalsIgnoreCase("JAVA")
					&& StringUtility.isJavaFile(target)) {
				files.add(target);
			} else if (LANGUAGE.equalsIgnoreCase("C")
					&& StringUtility.isCFile(target)) {
				files.add(target);
			}
		} else if (targetFile.isDirectory()) {
			for (final File child : targetFile.listFiles()) {
				files.addAll(getFiles(child.getPath()));
			}
		}

		return files;
	}
}
