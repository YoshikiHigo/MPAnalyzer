package yoshikihigo.cpanalyzer.gui;

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

import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.data.Code;
import yoshikihigo.cpanalyzer.gui.ObservedCodeFragments.CFLABEL;
import yoshikihigo.cpanalyzer.gui.ObservedFiles.FLABEL;
import yoshikihigo.cpanalyzer.gui.ObservedChangePatterns.MPLABEL;
import yoshikihigo.cpanalyzer.gui.ObservedRevisions.RLABEL;
import yoshikihigo.cpanalyzer.gui.dcode.DCode;
import yoshikihigo.cpanalyzer.gui.dtree.DTree;
import yoshikihigo.cpanalyzer.gui.mcode.MCode;
import yoshikihigo.cpanalyzer.gui.progress.ProgressDialog;
import yoshikihigo.cpanalyzer.gui.rlist.RList;

public class DetectionWindow extends JFrame implements Observer {

	public DetectionWindow() {
		super("Detection Window - MPAnalyzer");

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(d.width - 5, d.height - 27));

		final JPanel topLeftPanel = new JPanel(new BorderLayout());
		final JButton searchButton = new JButton("Search");
		topLeftPanel.add(searchButton, BorderLayout.NORTH);
		final RList rList = new RList();
		topLeftPanel.add(rList.scrollPane, BorderLayout.CENTER);

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
		final ChangePattern pattern = ObservedChangePatterns
				.getInstance(MPLABEL.SELECTED).get().first();
		beforeCode.setText(pattern.getChanges().get(0).before.text);
		afterCode.setText(pattern.getChanges().get(0).after.text);

		final JPanel topMainPanl = new JPanel();
		topMainPanl.setLayout(new BorderLayout());
		topMainPanl.add(buttonPane, BorderLayout.NORTH);
		topMainPanl.add(mCodePane, BorderLayout.CENTER);

		final JPanel topPane = new JPanel();
		topPane.setLayout(new BorderLayout());
		topPane.add(topLeftPanel, BorderLayout.WEST);
		topPane.add(topMainPanl, BorderLayout.CENTER);

		final DTree dTree = new DTree();
		final DCode dCode = new DCode();
		ObservedCodeFragments.getInstance(CFLABEL.DETECTION).addObserver(dCode);
		ObservedFiles.getInstance(FLABEL.SELECTED).addObserver(dCode);
		ObservedRevisions.getInstance(RLABEL.DETECTION).addObserver(dCode);

		final JSplitPane bottomPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT);
		bottomPane.add(dTree.scrollPane, JSplitPane.LEFT);
		bottomPane.add(dCode.scrollPane, JSplitPane.RIGHT);

		final JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPane.add(topPane, JSplitPane.TOP);
		mainPane.add(bottomPane, JSplitPane.BOTTOM);

		this.getContentPane().add(mainPane);

		ObservedCodeFragments.getInstance(CFLABEL.DETECTION).addObserver(this);
		ObservedFiles.getInstance(FLABEL.SELECTED).addObserver(this);
		ObservedRevisions.getInstance(RLABEL.DETECTION).addObserver(this);

		this.setVisible(true);

		mainPane.setDividerLocation(d.height / 3);
		bottomPane.setDividerLocation(d.width / 3);

		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {

				searchButton.setEnabled(false);

				final long revision = rList.getSelectedRevision();
				final Code codefragment = beforeButton.isSelected() ? pattern
						.getChanges().get(0).before : pattern
						.getChanges().get(0).after;

				ObservedRevisions.getInstance(RLABEL.DETECTION).set(revision,
						DetectionWindow.this);
				ObservedCodeFragments.getInstance(CFLABEL.DETECTION).set(
						codefragment, DetectionWindow.this);

				final ProgressDialog progressDialog = new ProgressDialog(
						DetectionWindow.this, "searching patterns...");
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
