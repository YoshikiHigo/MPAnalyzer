package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModifications.MLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mcode.MCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mlist.MList;

public class PatternWindow extends JFrame {

	public PatternWindow() {
		super("");

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(d.width - 5, d.height - 27));
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final MList list = new MList();
		ObservedModifications.getInstance(MLABEL.SELECTED).addObserver(list);

		final MCode beforeCode = new MCode(CODE.BEFORE);
		ObservedModifications.getInstance(MLABEL.SELECTED).addObserver(
				beforeCode);

		final MCode afterCode = new MCode(CODE.AFTER);
		ObservedModifications.getInstance(MLABEL.SELECTED).addObserver(
				afterCode);

		final JSplitPane codePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		codePane.add(beforeCode.scrollPane, JSplitPane.LEFT);
		codePane.add(afterCode.scrollPane, JSplitPane.RIGHT);

		final JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainPane.add(list.scrollPane, JSplitPane.LEFT);
		mainPane.add(codePane, JSplitPane.RIGHT);

		this.getContentPane().add(mainPane, BorderLayout.CENTER);
	}
}
