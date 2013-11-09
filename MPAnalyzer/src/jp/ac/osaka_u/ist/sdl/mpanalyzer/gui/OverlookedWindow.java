package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.StringUtility;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedCodeFragments.CFLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedFiles.FLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedRevisions.RLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.clpanel.CLPanel;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mpcode.MPCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ocode.OCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.olist.OList;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.progress.ProgressDialog;
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

	private ProgressDialog progressDialog;

	public OverlookedWindow() {
		super("Overlooked code Window - MPAnalyzer");

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(d.width - 5, d.height - 27));

		final JLabel placeLabel = new JLabel("Place");
		placeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final JTextField placeField = new JTextField();
		final JPanel placePanel = new JPanel(new GridLayout(1, 2));
		placePanel.setBorder(new LineBorder(Color.black));
		placePanel.add(placeLabel);
		placePanel.add(placeField);

		final JButton searchButton = new JButton("search");
		final JPanel configurationPanel = new JPanel(new GridLayout(2, 1));
		configurationPanel.add(searchButton);
		configurationPanel.add(placePanel);

		final RList rList = new RList();
		final JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(configurationPanel, BorderLayout.NORTH);
		leftPanel.add(rList.scrollPane, BorderLayout.CENTER);
		this.getContentPane().add(leftPanel, BorderLayout.WEST);

		final JSplitPane listPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		final OList oList = new OList();
		listPanel.setTopComponent(oList.scrollPane);
		final CLPanel clPanel = new CLPanel();
		ObservedModificationPatterns.getInstance(MPLABEL.OVERLOOKED)
				.addObserver(clPanel);
		listPanel.setBottomComponent(clPanel.scrollPane);

		final JSplitPane codePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		final OCode oCode = new OCode();
		ObservedCodeFragments.getInstance(CFLABEL.OVERLOOKED)
				.addObserver(oCode);
		ObservedFiles.getInstance(FLABEL.OVERLOOKED).addObserver(oCode);
		ObservedRevisions.getInstance(RLABEL.OVERLOOKED).addObserver(oCode);
		codePanel.setTopComponent(oCode.scrollPane);
		final MPCode mpCode = new MPCode(CODE.AFTER);
		ObservedModificationPatterns.getInstance(MPLABEL.OVERLOOKED)
				.addObserver(mpCode);
		codePanel.setBottomComponent(mpCode.scrollPane);

		final JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(listPanel, BorderLayout.WEST);
		centerPanel.add(codePanel, BorderLayout.CENTER);
		this.getContentPane().add(centerPanel, BorderLayout.CENTER);

		ObservedCodeFragments.getInstance(CFLABEL.OVERLOOKED).addObserver(this);
		ObservedFiles.getInstance(FLABEL.OVERLOOKED).addObserver(this);
		ObservedModificationPatterns.getInstance(MPLABEL.OVERLOOKED)
				.addObserver(this);
		ObservedRevisions.getInstance(RLABEL.OVERLOOKED).addObserver(this);

		listPanel.setDividerLocation(d.height - 400);
		codePanel.setDividerLocation(d.height - 300);

		this.setVisible(true);

		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				searchButton.setEnabled(false);

				ObservedCodeFragments.getInstance(CFLABEL.OVERLOOKED).clear(
						OverlookedWindow.this);
				ObservedFiles.getInstance(FLABEL.OVERLOOKED).clear(
						OverlookedWindow.this);
				ObservedModificationPatterns.getInstance(MPLABEL.OVERLOOKED)
						.clear(OverlookedWindow.this);
				ObservedRevisions.getInstance(RLABEL.OVERLOOKED).clear(
						OverlookedWindow.this);

				final int value;
				try {
					final String text = placeField.getText();
					if (text.isEmpty()) {
						value = Integer.MAX_VALUE;
					} else {
						value = Integer.parseInt(text);
					}
				} catch (final NumberFormatException exception) {

					final JDialog dialog = new JDialog(OverlookedWindow.this,
							"MPAnalyzer", true);
					final JLabel error = new JLabel(
							"PLACE must be emply or a positive integer!");
					error.setHorizontalAlignment(SwingConstants.CENTER);
					dialog.getContentPane().add(error);
					dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					dialog.setSize(300, 80);
					dialog.setLocation(100, 100);
					dialog.setVisible(true);

					searchButton.setEnabled(true);
					return;
				}

				final ProgressDialog progressDialog = new ProgressDialog(
						OverlookedWindow.this,
						"searching overlooked code fragments...");
				OverlookedWindow.this.progressDialog = progressDialog;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						progressDialog.setVisible(true);
					}
				});

				final SwingWorker<Object, Object> task = new SwingWorker<Object, Object>() {

					@Override
					protected Object doInBackground() throws Exception {
						final long revision = rList.getSelectedRevision();
						final SortedMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>> oCodefragments = OverlookedWindow.this
								.detectOverlookedCode(revision, value);
						oList.setModel(oCodefragments);
						oList.repaint();

						ObservedRevisions.getInstance(RLABEL.OVERLOOKED).set(
								revision, OverlookedWindow.this);
						return null;
					}

					@Override
					protected void done() {
						super.done();
						searchButton.setEnabled(true);
						progressDialog.dispose();
					}

				};
				task.execute();
			}
		});
	}

	private SortedMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>> detectOverlookedCode(
			final long revision, final int place) {
		final Map<String, List<Statement>> files = this.getFiles(revision);
		final SortedSet<ModificationPattern> MPs = ObservedModificationPatterns
				.getInstance(MPLABEL.FILTERED).get();
		final SortedMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>> oCodefragments = new TreeMap<ModificationPattern, SortedMap<String, SortedSet<CodeFragment>>>();
		for (final ModificationPattern mp : MPs) {
			final List<Statement> pattern = mp.getModifications().get(0).before.statements;
			final SortedMap<String, SortedSet<CodeFragment>> oCodefragmentForAMP = this
					.getOverlookedCode(files, pattern);

			final List<CodeFragment> cfForChecking = new ArrayList<CodeFragment>();
			for (final SortedSet<CodeFragment> cfs : oCodefragmentForAMP
					.values()) {
				cfForChecking.addAll(cfs);
			}
			if ((0 < cfForChecking.size()) && (cfForChecking.size() <= place)) {
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

			this.progressDialog.note.setText("preparing a file list ...");
			this.progressDialog.repaint();

			final SortedSet<String> paths = new TreeSet<String>();

			logClient.doList(url, SVNRevision.create(revision),
					SVNRevision.create(revision), true, SVNDepth.INFINITY,
					SVNDirEntry.DIRENT_ALL, new ISVNDirEntryHandler() {

						@Override
						public void handleDirEntry(final SVNDirEntry entry)
								throws SVNException {

							if (OverlookedWindow.this.progressDialog.canceled
									.isCanceled()) {
								return;
							}

							OverlookedWindow.this.progressDialog.progressBar
									.setMaximum(OverlookedWindow.this.progressDialog.progressBar
											.getMaximum() + 1);
							OverlookedWindow.this.progressDialog.progressBar
									.setValue(OverlookedWindow.this.progressDialog.progressBar
											.getValue() + 1);
							OverlookedWindow.this.progressDialog.repaint();

							if (entry.getKind() == SVNNodeKind.FILE) {
								final String path = entry.getRelativePath();

								if (LANGUAGE.equalsIgnoreCase("JAVA")
										&& path.startsWith(TARGET)
										&& StringUtility.isJavaFile(path)) {

									paths.add(path);

									OverlookedWindow.this.progressDialog.note
											.setText("preparing files ... "
													+ path);
									OverlookedWindow.this.progressDialog
											.repaint();

								} else if (LANGUAGE.equalsIgnoreCase("C")
										&& path.startsWith(TARGET)
										&& StringUtility.isCFile(path)) {

									paths.add(path);

									OverlookedWindow.this.progressDialog.note
											.setText("preparing files ... "
													+ path);
									OverlookedWindow.this.progressDialog
											.repaint();

								}
							}
						}
					});

			this.progressDialog.progressBar.setMaximum(paths.size());
			this.progressDialog.progressBar.setValue(0);

			final Map<String, List<Statement>> files = new HashMap<String, List<Statement>>();
			int progress = 1;
			for (final String path : paths) {

				if (this.progressDialog.canceled.isCanceled()) {
					return new HashMap<String, List<Statement>>();
				}

				this.progressDialog.progressBar.setValue(progress++);
				this.progressDialog.note.setText("detecting patterns ... "
						+ path);
				this.progressDialog.repaint();

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
