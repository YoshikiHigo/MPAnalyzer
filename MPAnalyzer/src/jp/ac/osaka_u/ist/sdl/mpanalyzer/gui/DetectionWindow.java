package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.dtree.DTree;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mcode.MCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.rlist.RList;

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

		final DTree dTree = new DTree();

		final JSplitPane bottomPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT);
		bottomPane.add(dTree, JSplitPane.LEFT);

		final JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPane.add(topPane, JSplitPane.TOP);
		mainPane.add(bottomPane, JSplitPane.BOTTOM);

		this.getContentPane().add(mainPane);

		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {

				final long revision = rList.getSelectedRevision();
				final CodeFragment codeFragment = beforeButton.isSelected() ? pattern
						.getModifications().get(0).before : pattern
						.getModifications().get(0).after;
				dTree.update(revision, codeFragment);
			}
		});
	}
}
