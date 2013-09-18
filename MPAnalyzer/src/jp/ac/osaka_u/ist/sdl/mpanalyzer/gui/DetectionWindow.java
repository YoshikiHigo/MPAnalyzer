package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedCodeFragments.CFLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedFiles.FLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedRevisions.RLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.dcode.DCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.dtree.DTree;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mcode.MCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.progress.ProgressDialog;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.rlist.RList;

public class DetectionWindow extends JFrame implements Observer {

	static final private String PATH_TO_REPOSITORY = Config
			.getPATH_TO_REPOSITORY();
	static final private String TARGET = Config.getTARGET();

	public DetectionWindow() {
		super("Detection Window - MPAnalyzer");

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(d.width - 5, d.height - 27));

		final RList rList = new RList();

		final JRadioButton beforeButton = new JRadioButton(
				"Before modification", false);
		final JRadioButton afterButton = new JRadioButton("After modification",
				true);
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

		final DTree dTree = new DTree();

		final DCode dCode = new DCode();
		ObservedCodeFragments.getInstance(CFLABEL.SELECTED).addObserver(dCode);
		ObservedFiles.getInstance(FLABEL.SELECTED).addObserver(dCode);
		ObservedRevisions.getInstance(RLABEL.SELECTED).addObserver(dCode);

		final JSplitPane bottomPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT);
		bottomPane.add(dTree.scrollPane, JSplitPane.LEFT);
		bottomPane.add(dCode.scrollPane, JSplitPane.RIGHT);

		final JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPane.add(topPane, JSplitPane.TOP);
		mainPane.add(bottomPane, JSplitPane.BOTTOM);

		this.getContentPane().add(mainPane);

		mainPane.setDividerLocation(d.height / 2);
		bottomPane.setDividerLocation(d.width / 2);

		ObservedCodeFragments.getInstance(CFLABEL.SELECTED).addObserver(this);
		ObservedFiles.getInstance(FLABEL.SELECTED).addObserver(this);
		ObservedRevisions.getInstance(RLABEL.SELECTED).addObserver(this);

		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {

				searchButton.setEnabled(false);

				final long revision = rList.getSelectedRevision();
				final CodeFragment codefragment = beforeButton.isSelected() ? pattern
						.getModifications().get(0).before : pattern
						.getModifications().get(0).after;

				ObservedRevisions.getInstance(RLABEL.SELECTED).set(revision,
						DetectionWindow.this);
				ObservedCodeFragments.getInstance(CFLABEL.SELECTED).set(
						codefragment, DetectionWindow.this);

				final ProgressDialog progressDialog = new ProgressDialog(
						DetectionWindow.this);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						progressDialog.setVisible(true);
					}
				});

				final SwingWorker<Object, Object> task = new SwingWorker<Object, Object>() {

					@Override
					protected Object doInBackground() throws Exception {
						dTree.setProgressDialog(progressDialog);
						dTree.update(revision, codefragment);
						return null;
					}

					@Override
					protected void done() {
						super.done();
						searchButton.setEnabled(true);
					}

				};
				task.execute();
			}
		});
	}

	@Override
	public void update(Observable o, Object arg) {
	}
}
