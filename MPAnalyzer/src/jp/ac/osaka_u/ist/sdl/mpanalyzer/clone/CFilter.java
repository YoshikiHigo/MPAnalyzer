package jp.ac.osaka_u.ist.sdl.mpanalyzer.clone;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;

public class CFilter extends CDetector {

	public static void main(final String[] args) {

		System.out.print("identifying source files ... ");
		final CFilter filter = new CFilter();
		final SortedSet<String> paths = filter.identifyFiles();
		System.out.println(": done.");
		
		System.out.print("identifying changed clones ... ");
		final ConcurrentMap<String, List<Statement>> contents = filter
				.getFileContent(paths);
		System.out.println(": done.");

		try {
			final ReadOnlyDAO readOnlyDAO = ReadOnlyDAO.getInstance();
			List<ModificationPattern> patterns = readOnlyDAO
					.getModificationPatterns(2, 1);
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
