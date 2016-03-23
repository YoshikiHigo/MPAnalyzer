package yoshikihigo.cpanalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;

import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.db.ChangeDAO;

public class ChangeExtractor {

	public static void main(String[] args) {

		CPAConfig.initialize(args);
		final String db = CPAConfig.getInstance().getDATABASE();
		final File dbFile = new File(db);
		if (dbFile.exists()) {
			System.out.println(db + " already exists in your file system.");
			final boolean isForce = CPAConfig.getInstance().isFORCE();
			if (isForce) {
				if (!dbFile.delete()) {
					if (!CPAConfig.getInstance().isQUIET()) {
						System.err.println("The file cannot be removed.");
					}
					System.exit(0);
				} else {
					if (!CPAConfig.getInstance().isQUIET()) {
						System.out.println("The file has been removed.");
					}
				}
			} else {
				System.exit(0);
			}
		}

		final int THREADS = CPAConfig.getInstance().getTHREAD();

		final long startTime = System.nanoTime();

		if (!CPAConfig.getInstance().isQUIET()) {
			System.out.println("working on software \""
					+ CPAConfig.getInstance().getSOFTWARE() + "\"");
			System.out.print("identifing revisions to be checked ... ");
		}
		if (CPAConfig.getInstance().isVERBOSE()) {
			System.out.println();
		}
		final Revision[] revisions = getSVNRevisions();
		ChangeDAO.SINGLETON.initialize();
		ChangeDAO.SINGLETON.addRevisions(revisions);
		if (!CPAConfig.getInstance().isVERBOSE()) {
			System.out.println("done.");
		}

		if ((0 == revisions.length) && !CPAConfig.getInstance().isQUIET()) {
			System.out.println("no revision.");
			System.exit(0);
		}

		if (!CPAConfig.getInstance().isQUIET()) {
			System.out.print("extracting code changes ... ");
		}
		if (CPAConfig.getInstance().isVERBOSE()) {
			System.out.println();
		}

		final ExecutorService threadPool = Executors
				.newFixedThreadPool(THREADS);
		final List<Future<?>> futures = new ArrayList<>();

		for (int index = 1; index < revisions.length; index++) {
			final Revision beforeRevision = revisions[index - 1];
			final Revision afterRevision = revisions[index];
			final Future<?> future = threadPool
					.submit(new ChangeExtractionThread(beforeRevision,
							afterRevision));
			futures.add(future);
		}

		try {
			for (final Future<?> future : futures) {
				future.get();
			}
			ChangeDAO.SINGLETON.flush();
			ChangeDAO.SINGLETON.close();
		} catch (final ExecutionException | InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			threadPool.shutdown();
		}

		if (!CPAConfig.getInstance().isVERBOSE()
				&& !CPAConfig.getInstance().isQUIET()) {
			System.out.println("done.");
		}

		final long endTime = System.nanoTime();
		if (!CPAConfig.getInstance().isQUIET()) {
			System.out.print("execution time: ");
			System.out.println(TimingUtility.getExecutionTime(startTime,
					endTime));
		}
	}

	private static Revision[] getSVNRevisions() {

		final String repository = CPAConfig.getInstance()
				.getSVNREPOSITORY_FOR_MINING();
		final Set<LANGUAGE> languages = CPAConfig.getInstance().getLANGUAGE();
		final String software = CPAConfig.getInstance().getSOFTWARE();
		final boolean isVerbose = CPAConfig.getInstance().isVERBOSE();

		long startRevision = CPAConfig.getInstance()
				.getSTART_REVISION_FOR_MINING();
		long endRevision = CPAConfig.getInstance().getEND_REVISION_FOR_MINING();

		if (startRevision < 0) {
			startRevision = 0l;
		}

		final SortedSet<Revision> revisions = new TreeSet<>();

		SVNURL url;
		SVNRepository svnRepository;
		try {
			FSRepositoryFactory.setup();
			url = SVNURL.fromFile(new File(repository));
			svnRepository = FSRepositoryFactory.create(url);
		} catch (final SVNException | NullPointerException e) {
			e.printStackTrace();
			return revisions.toArray(new Revision[0]);
		}

		try {

			if (endRevision < 0) {
				endRevision = svnRepository.getLatestRevision();
			}

			svnRepository.log(
					null,
					startRevision,
					endRevision,
					true,
					true,
					entry -> {
						final int number = (int) entry.getRevision();
						final String date = StringUtility.getDateString(entry
								.getDate());
						final String message = entry.getMessage();
						final String author = entry.getAuthor();
						final Revision revision = new Revision(software,
								number, date, message, author);
						for (final String path : entry.getChangedPaths()
								.keySet()) {
							for (final LANGUAGE language : languages) {
								if (isVerbose && language.isTarget(path)) {
									System.out.println(Integer.toString(number)
											+ " has been identified.");
								}
								revisions.add(revision);
								return;
							}
						}
					});

		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return revisions.toArray(new Revision[0]);
	}
}
