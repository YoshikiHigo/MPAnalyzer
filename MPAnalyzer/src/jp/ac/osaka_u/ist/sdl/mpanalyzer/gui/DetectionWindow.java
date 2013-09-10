package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mcode.MCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.rlist.RList;

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

public class DetectionWindow extends JFrame {

	static final private String PATH_TO_REPOSITORY = Config
			.getPATH_TO_REPOSITORY();
	static final private String TARGET = Config.getTARGET();

	public DetectionWindow() {
		super("Detection Window - MPAnalyzer");

		final RList rList = new RList();

		final JRadioButton beforeButton = new JRadioButton(
				"Before modification", false);
		final JRadioButton afterButton = new JRadioButton("After modification",
				false);
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(beforeButton);
		buttonGroup.add(afterButton);
		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridLayout(1, 2));
		buttonPane.add(beforeButton);
		buttonPane.add(afterButton);

		final JPanel mCodePane = new JPanel();
		mCodePane.setLayout(new GridLayout(1, 2));
		final MCode beforeCode = new MCode(CODE.BEFORE);
		final MCode afterCode = new MCode(CODE.AFTER);
		mCodePane.add(beforeCode.scrollPane);
		mCodePane.add(afterCode.scrollPane);
		final ModificationPattern pattern = ObservedModificationPatterns
				.getInstance(MPLABEL.SELECTED).get().first();
		beforeCode.setText(pattern.getModifications().get(0).before.text);
		afterCode.setText(pattern.getModifications().get(0).after.text);

		final JPanel topMainPanl = new JPanel();
		topMainPanl.setLayout(new BorderLayout());
		topMainPanl.add(buttonPane, BorderLayout.NORTH);
		topMainPanl.add(mCodePane, BorderLayout.CENTER);

		final JButton searchButton = new JButton("Search");

		final JPanel topPane = new JPanel();
		topPane.setLayout(new BorderLayout());
		topPane.add(rList.scrollPane, BorderLayout.WEST);
		topPane.add(topMainPanl, BorderLayout.CENTER);
		topPane.add(searchButton, BorderLayout.EAST);

		final JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainPane.add(topPane, JSplitPane.TOP);

		this.getContentPane().add(mainPane);

		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {

				final Map<String, String> pathContentMap = new HashMap<String, String>();

				try {
					final SVNURL url = SVNURL.fromFile(new File(
							PATH_TO_REPOSITORY));
					FSRepositoryFactory.setup();
					final SVNWCClient wcClient = SVNClientManager.newInstance()
							.getWCClient();
					final SVNDiffClient diffClient = SVNClientManager
							.newInstance().getDiffClient();

					final List<String> files = new ArrayList<String>();

					// 変更されたJavaファイルのリストを得る
					diffClient.doDiffStatus(url, SVNRevision.create(0), url,
							SVNRevision.create(rList.getSelectedRevision()),
							SVNDepth.INFINITY, true,
							new ISVNDiffStatusHandler() {

								@Override
								public void handleDiffStatus(
										final SVNDiffStatus diffStatus) {
									final String path = diffStatus.getPath();
									final SVNStatusType type = diffStatus
											.getModificationType();
									if (path.endsWith(".java")
											&& path.startsWith(TARGET)
											&& type.equals(SVNStatusType.STATUS_MODIFIED)) {
										files.add(path);
									}
								}
							});

					for (final String path : files) {
						final SVNURL fileurl = SVNURL.fromFile(new File(
								PATH_TO_REPOSITORY
										+ System.getProperty("file.separator")
										+ path));

						final StringBuilder text = new StringBuilder();
						wcClient.doGetFileContents(
								fileurl,
								SVNRevision.create(rList.getSelectedRevision()),
								SVNRevision.create(rList.getSelectedRevision()),
								false, new OutputStream() {
									@Override
									public void write(int b) throws IOException {
										text.append((char) b);
									}
								});

						pathContentMap.put(path, text.toString());
					}

				} catch (final SVNException exception) {
					exception.printStackTrace();
				}
			}

		});
	}
}
