package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModifications.MLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mcode.MCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mlist.MList;

public class PatternWindow extends JFrame implements Observer {

	private final JTextArea logDisplay;

	public PatternWindow() {
		super("");

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(d.width - 5, d.height - 27));
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		ObservedModifications.getInstance(MLABEL.SELECTED).addObserver(this);

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

		this.logDisplay = new JTextArea();
		this.logDisplay.setEditable(false);
		this.logDisplay.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Log message"));
		this.getContentPane().add(this.logDisplay, BorderLayout.SOUTH);

		this.setVisible(true);

		codePane.setDividerLocation(codePane.getWidth() / 2);
	}

	@Override
	public void update(final Observable o, final Object arg) {
		if (o instanceof ObservedModifications) {
			final ObservedModifications modifications = (ObservedModifications) o;
			if (modifications.label.equals(MLABEL.SELECTED)) {
				if (modifications.isSet()) {
					final Modification m = modifications.get().first();
					this.logDisplay.setText(m.revision.message);
				} else {
					this.logDisplay.setText("");
				}
			}
		}
	}
}
