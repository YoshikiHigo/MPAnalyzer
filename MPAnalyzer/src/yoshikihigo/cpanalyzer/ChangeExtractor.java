package yoshikihigo.cpanalyzer;

import java.io.File;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Revision;

public class ChangeExtractor {

	public static void main(String[] args) {

		CPAConfig.initialize(args);
		final int THREADS = CPAConfig.getInstance().getTHREAD();

		final long startTime = System.nanoTime();

		System.out.println("working on software \""
				+ CPAConfig.getInstance().getSOFTWARE() + "\"");
		System.out.print("identifing revisions to be checked ... ");
		final Revision[] revisions = getSVNRevisions();
		if (!CPAConfig.getInstance().isVERBOSE()) {
			System.out.println("done.");
		}

		if (0 == revisions.length) {
			System.out.println("no revision.");
			System.exit(0);
		}

		final AtomicInteger index = new AtomicInteger(1);
		final BlockingQueue<Change> queue = new ArrayBlockingQueue<Change>(
				100000, true);
		final ChangeWritingThread writingThread = new ChangeWritingThread(
				queue, revisions);
		writingThread.start();
		final ChangeExtractionThread[] extractingThreads = new ChangeExtractionThread[THREADS];
		for (int i = 0; i < extractingThreads.length; i++) {
			extractingThreads[i] = new ChangeExtractionThread(i, revisions,
					index, queue);
			extractingThreads[i].start();
		}

		try {
			for (final ChangeExtractionThread thread : extractingThreads) {
				thread.join();
			}
			writingThread.finish();
			writingThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}

		final long endTime = System.nanoTime();
		System.out.print("execution time: ");
		System.out.println(TimingUtility.getExecutionTime(startTime, endTime));
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

		try {

			final SVNURL url = SVNURL.fromFile(new File(repository));
			FSRepositoryFactory.setup();
			final SVNRepository svnRepository = FSRepositoryFactory.create(url);

			if (endRevision < 0) {
				endRevision = svnRepository.getLatestRevision();
			}

			svnRepository.log(null, startRevision, endRevision, true, true,
					new ISVNLogEntryHandler() {
						@Override
						public void handleLogEntry(SVNLogEntry logEntry)
								throws SVNException {
							for (final Object key : logEntry.getChangedPaths()
									.keySet()) {
								final String path = (String) key;
								final int number = (int) logEntry.getRevision();
								final String date = StringUtility
										.getDateString(logEntry.getDate());
								final String message = logEntry.getMessage();
								final Revision revision = new Revision(
										software, number, date, message);
								for (final LANGUAGE language : languages) {
									if (isVerbose && language.isTarget(path)) {
										System.err.println(Integer
												.toString(number)
												+ " is identified.");
									}
									revisions.add(revision);
									return;
								}
							}
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return revisions.toArray(new Revision[0]);
	}
}
