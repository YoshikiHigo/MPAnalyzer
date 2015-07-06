package yoshikihigo.cpanalyzer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.data.Statement;

public class ChangeExtractionThread extends Thread {

	final public int id;
	final public Revision[] revisions;
	final private AtomicInteger index;
	final private BlockingQueue<Change> queue;

	public ChangeExtractionThread(final int id, final Revision[] revisions,
			final AtomicInteger index, BlockingQueue<Change> queue) {
		this.id = id;
		this.revisions = revisions;
		this.index = index;
		this.queue = queue;
	}

	@Override
	public void run() {

		try {

			final String repository = Config.getInstance()
					.getSVNREPOSITORY_FOR_MINING();
			final String language = Config.getInstance().getLANGUAGE();
			final String software = Config.getInstance().getSOFTWARE();
			final boolean onlyCondition = Config.getInstance()
					.isONLY_CONDITION();
			final boolean ignoreImport = Config.getInstance().isIGNORE_IMPORT();
			final boolean isVerbose = Config.getInstance().isVERBOSE();

			final SVNURL url = SVNURL.fromFile(new File(repository));
			FSRepositoryFactory.setup();
			final SVNDiffClient diffClient = SVNClientManager.newInstance()
					.getDiffClient();
			final SVNWCClient wcClient = SVNClientManager.newInstance()
					.getWCClient();

			REVISION: while (true) {

				final int targetIndex = this.index.getAndIncrement();
				if (this.revisions.length <= targetIndex) {
					break;
				}
				if (targetIndex < 1) {
					continue;
				}

				final Revision beforeRevision = this.revisions[targetIndex - 1];
				final Revision afterRevision = this.revisions[targetIndex];

				final StringBuilder progress = new StringBuilder();
				progress.append(this.id);
				progress.append(": checking revisions ");
				progress.append(beforeRevision.number);
				progress.append(" and ");
				progress.append(afterRevision.number);
				System.out.println(progress.toString());

				final List<String> changedFileList = new ArrayList<String>();
				try {
					diffClient.doDiffStatus(url,
							SVNRevision.create(beforeRevision.number), url,
							SVNRevision.create(afterRevision.number),
							SVNDepth.INFINITY, true,
							new ISVNDiffStatusHandler() {

								@Override
								public void handleDiffStatus(
										final SVNDiffStatus diffStatus) {

									final String path = diffStatus.getPath();
									final SVNStatusType type = diffStatus
											.getModificationType();

									if (!type
											.equals(SVNStatusType.STATUS_MODIFIED)) {
										return;
									}

									if (language.equalsIgnoreCase("JAVA")
											&& StringUtility.isJavaFile(path)) {
										changedFileList.add(path);
										if (isVerbose) {
											System.out.println(path);
										}
									} else if (language.equalsIgnoreCase("C")
											&& StringUtility.isCFile(path)) {
										changedFileList.add(path);
										if (isVerbose) {
											System.out.println(path);
										}

									}
								}
							});
				} catch (final SVNException e) {
					e.printStackTrace();
					continue REVISION;
				}

				FILE: for (final String path : changedFileList) {
					final SVNURL fileurl = SVNURL.fromFile(new File(repository
							+ System.getProperty("file.separator") + path));

					final StringBuilder beforeText = new StringBuilder();
					try {
						wcClient.doGetFileContents(fileurl,
								SVNRevision.create(beforeRevision.number),
								SVNRevision.create(beforeRevision.number),
								false, new OutputStream() {
									@Override
									public void write(int b) throws IOException {
										beforeText.append((char) b);
									}
								});
					} catch (SVNException e) {
						e.printStackTrace();
						continue FILE;
					}

					final StringBuilder afterText = new StringBuilder();
					try {
						wcClient.doGetFileContents(fileurl,
								SVNRevision.create(afterRevision.number),
								SVNRevision.create(afterRevision.number),
								false, new OutputStream() {
									@Override
									public void write(int b) throws IOException {
										afterText.append((char) b);
									}
								});
					} catch (SVNException e) {
						e.printStackTrace();
						continue FILE;
					}

					final List<Statement> beforeStatements = StringUtility
							.splitToStatements(beforeText.toString(), language);
					final List<Statement> afterStatements = StringUtility
							.splitToStatements(afterText.toString(), language);

					final List<Change> changes = LCS.getChanges(
							beforeStatements, afterStatements, software, path,
							afterRevision);

					if (onlyCondition) {
						for (final Change change : changes) {
							if (change.isCondition()) {
								this.queue.add(change);
							}
						}
					}

					else if (ignoreImport) {
						for (final Change change : changes) {
							if (!change.isImport()) {
								this.queue.add(change);
							}
						}
					}

					else {
						this.queue.addAll(changes);
						if (isVerbose) {
							System.out.println("Number of Changes: "
									+ changes.size());
						}
					}
				}
			}

		} catch (final SVNException e) {
			e.printStackTrace();
		}
	}
}
