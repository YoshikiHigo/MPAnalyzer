package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.rlist.RList;

import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class OverlookedWindow extends JFrame {

	static final private String PATH_TO_REPOSITORY = Config
			.getPATH_TO_REPOSITORY();
	static final private String TARGET = Config.getTARGET();
	static final private String LANGUAGE = Config.getLanguage();

	public OverlookedWindow() {
		super("Overlooked code Window - MPAnalyzer");

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(d.width - 5, d.height - 27));

		final RList rList = new RList();
		final JButton searchButton = new JButton("search");
		final JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(searchButton, BorderLayout.NORTH);
		leftPanel.add(rList.scrollPane, BorderLayout.CENTER);

		this.getContentPane().add(leftPanel, BorderLayout.WEST);

		this.setVisible(true);

		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final long revision = rList.getSelectedRevision();
				OverlookedWindow.this.detectOverlookedCode(revision);
			}
		});
	}

	private void detectOverlookedCode(final long revision) {
		final Map<String, List<Statement>> files = this.getFiles(revision);
	}

	private Map<String, List<Statement>> getFiles(final long revision) {

		try {
			final SVNURL url = SVNURL.fromFile(new File(PATH_TO_REPOSITORY));
			FSRepositoryFactory.setup();
			final SVNLogClient logClient = SVNClientManager.newInstance()
					.getLogClient();
			final SVNWCClient wcClient = SVNClientManager.newInstance()
					.getWCClient();

			// this.progressDialog.note.setText("preparing a file list ...");
			// this.progressDialog.repaint();

			final SortedSet<String> paths = new TreeSet<String>();

			logClient.doList(url, SVNRevision.create(revision),
					SVNRevision.create(revision), true, SVNDepth.INFINITY,
					SVNDirEntry.DIRENT_ALL, new ISVNDirEntryHandler() {

						@Override
						public void handleDirEntry(final SVNDirEntry entry)
								throws SVNException {

							// if (progressDialog.canceled.isCanceled()) {
							// return;
							// }

							// progressDialog.progressBar
							// .setMaximum(progressDialog.progressBar
							// .getMaximum() + 1);
							// progressDialog.progressBar
							// .setValue(progressDialog.progressBar
							// .getValue() + 1);
							// progressDialog.repaint();

							if (entry.getKind() == SVNNodeKind.FILE) {
								final String path = entry.getRelativePath();

								if (LANGUAGE.equalsIgnoreCase("JAVA")
										&& path.startsWith(TARGET)
										&& StringUtility.isJavaFile(path)) {

									paths.add(path);

									// progressDialog.note
									// .setText("preparing files ... "
									// + path);
									// progressDialog.repaint();

								} else if (LANGUAGE.equalsIgnoreCase("C")
										&& path.startsWith(TARGET)
										&& StringUtility.isCFile(path)) {

									paths.add(path);

									// progressDialog.note
									// .setText("preparing files ... "
									// + path);
									// progressDialog.repaint();

								}
							}
						}
					});

			// this.progressDialog.progressBar.setMaximum(files.size());
			// this.progressDialog.progressBar.setValue(0);

			final Map<String, List<Statement>> files = new HashMap<String, List<Statement>>();
			int progress = 1;
			for (final String path : paths) {

				// if (progressDialog.canceled.isCanceled()) {
				// return new ArrayList<FileData>();
				// }

				// this.progressDialog.progressBar.setValue(progress++);
				// this.progressDialog.note.setText("detecting patterns ... "
				// + path);
				// this.progressDialog.repaint();

				final SVNURL fileurl = SVNURL.fromFile(new File(
						PATH_TO_REPOSITORY
								+ System.getProperty("file.separator") + path));

				final StringBuilder text = new StringBuilder();
				wcClient.doGetFileContents(fileurl,
						SVNRevision.create(revision),
						SVNRevision.create(revision), false,
						new OutputStream() {
							@Override
							public void write(int b) throws IOException {
								text.append((char) b);
							}
						});

				final List<Statement> statements = StringUtility
						.splitToStatements(text.toString(), LANGUAGE);
				files.put(path, statements);
			}

			return files;

		} catch (final SVNException exception) {
			exception.printStackTrace();
		}

		return new HashMap<String, List<Statement>>();
	}
}
