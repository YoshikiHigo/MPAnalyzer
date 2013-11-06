package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedCodeFragments.CFLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedFiles.FLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedRevisions.RLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mpcode.MPCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ocode.OCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.olist.OList;
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

public class OverlookedWindow extends JFrame implements Observer {

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

		final OList oList = new OList();
		final JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(oList.scrollPane, BorderLayout.WEST);
		final OCode oCode = new OCode();
		ObservedCodeFragments.getInstance(CFLABEL.OVERLOOKED)
				.addObserver(oCode);
		ObservedFiles.getInstance(FLABEL.OVERLOOKED).addObserver(oCode);
		ObservedRevisions.getInstance(RLABEL.OVERLOOKED).addObserver(oCode);
		final JSplitPane codePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		codePanel.setTopComponent(oCode.scrollPane);
		final MPCode mpCode = new MPCode(CODE.AFTER);
		ObservedModificationPatterns.getInstance(MPLABEL.OVERLOOKED)
				.addObserver(mpCode);
		codePanel.setBottomComponent(mpCode.scrollPane);
		centerPanel.add(codePanel, BorderLayout.CENTER);

		this.getContentPane().add(centerPanel, BorderLayout.CENTER);

		ObservedCodeFragments.getInstance(CFLABEL.OVERLOOKED).addObserver(this);
		ObservedFiles.getInstance(FLABEL.OVERLOOKED).addObserver(this);
		ObservedModificationPatterns.getInstance(MPLABEL.OVERLOOKED)
				.addObserver(this);
		ObservedRevisions.getInstance(RLABEL.OVERLOOKED).addObserver(this);

		codePanel.setDividerLocation(d.height - 300);
		this.setVisible(true);

		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final long revision = rList.getSelectedRevision();
				final SortedMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>> oCodefragments = OverlookedWindow.this
						.detectOverlookedCode(revision);
				oList.setModel(oCodefragments);
				oList.repaint();

				ObservedCodeFragments.getInstance(CFLABEL.OVERLOOKED).clear(
						OverlookedWindow.this);
				ObservedFiles.getInstance(FLABEL.OVERLOOKED).clear(
						OverlookedWindow.this);
				ObservedRevisions.getInstance(RLABEL.OVERLOOKED).clear(
						OverlookedWindow.this);

				ObservedRevisions.getInstance(RLABEL.OVERLOOKED).set(revision,
						OverlookedWindow.this);
			}
		});
	}

	private SortedMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>> detectOverlookedCode(
			final long revision) {
		final Map<String, List<Statement>> files = this.getFiles(revision);
		final SortedSet<ModificationPattern> MPs = ObservedModificationPatterns
				.getInstance(MPLABEL.FILTERED).get();
		final SortedMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>> oCodefragments = new TreeMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>>();
		for (final ModificationPattern mp : MPs) {
			final List<Statement> pattern = mp.getModifications().get(0).before.statements;
			final SortedMap<String, SortedSet<CodeFragment>> oCodefragmentForAMP = this
					.getOverlookedCode(files, pattern);
			if (0 < oCodefragmentForAMP.size()) {
				oCodefragments.put(mp, oCodefragmentForAMP);
			}
		}
		return oCodefragments;
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

	private SortedMap<String, SortedSet<CodeFragment>> getOverlookedCode(
			final Map<String, List<Statement>> files,
			final List<Statement> pattern) {

		final SortedMap<String, SortedSet<CodeFragment>> oCodefragments = new TreeMap<String, SortedSet<CodeFragment>>();

		for (final Entry<String, List<Statement>> entry : files.entrySet()) {
			final String path = entry.getKey();
			final List<Statement> statements = entry.getValue();

			final SortedSet<CodeFragment> oCodefragmentsInAFile = this
					.getOverookedCode(statements, pattern);
			if (0 < oCodefragmentsInAFile.size()) {
				oCodefragments.put(path, oCodefragmentsInAFile);
			}
		}

		return oCodefragments;
	}

	private SortedSet<CodeFragment> getOverookedCode(
			final List<Statement> statements, final List<Statement> pattern) {

		int pIndex = 0;
		final SortedSet<CodeFragment> oCodefragments = new TreeSet<CodeFragment>();
		List<Statement> correspondence = new ArrayList<Statement>();
		for (int index = 0; index < statements.size(); index++) {

			if (statements.get(index).hash == pattern.get(pIndex).hash) {
				pIndex++;
				correspondence.add(statements.get(index));
				if (pIndex == pattern.size()) {
					final CodeFragment codefragment = new CodeFragment(
							correspondence);
					oCodefragments.add(codefragment);
					correspondence = new ArrayList<Statement>();
					pIndex = 0;
				}
			}

			else {
				pIndex = 0;
			}
		}

		return oCodefragments;
	}

	@Override
	public void update(final Observable o, final Object arg) {
	}
}
