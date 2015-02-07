package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Change;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;

public class ChangeExtractor {

	public static void main(String[] args) {

		Config.initialize(args);
		final int THREADS = Config.getInstance().getTHREAD();

		final long startTime = System.nanoTime();

		final Revision[] revisions = getRevisions().toArray(new Revision[0]);

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

	private static List<Revision> getRevisions() {

		final String repository = Config.getInstance()
				.getREPOSITORY_FOR_MINING();
		final String language = Config.getInstance().getLANGUAGE();
		final String software = Config.getInstance().getSOFTWARE();

		long startRevision = Config.getInstance()
				.getSTART_REVISION_FOR_MINING();
		long endRevision = Config.getInstance().getEND_REVISION_FOR_MINING();

		if (startRevision < 0) {
			startRevision = 0l;
		}

		final List<Revision> revisions = new ArrayList<Revision>();

		try {

			final SVNURL url = SVNURL.fromFile(new File(repository));
			FSRepositoryFactory.setup();
			final SVNRepository svnRepository = FSRepositoryFactory.create(url);

			if (endRevision < 0) {
				endRevision = svnRepository.getLatestRevision();
			}

			svnRepository.log(null, startRevision, endRevision, true, true,
					new ISVNLogEntryHandler() {
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
								if (language.equalsIgnoreCase("JAVA")
										&& StringUtility.isJavaFile(path)) {
									System.out.print(Integer.toString(number));
									System.out.println(" is beging checked.");
									revisions.add(revision);
									break;
								} else if (language.equalsIgnoreCase("C")
										&& StringUtility.isCFile(path)) {
									System.out.print(Integer.toString(number));
									System.out.println(" is being checked.");
									revisions.add(revision);
									break;
								}
							}
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return revisions;
	}
}
