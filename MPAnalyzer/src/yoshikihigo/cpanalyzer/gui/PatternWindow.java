package yoshikihigo.cpanalyzer.gui;

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

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.gui.ObservedChanges.MLABEL;
import yoshikihigo.cpanalyzer.gui.mcode.MCode;
import yoshikihigo.cpanalyzer.gui.mlist.MList;

public class PatternWindow extends JFrame implements Observer {

	private final JTextArea logDisplay;

	public PatternWindow() {
		super("");

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(d.width - 5, d.height - 27));
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		ObservedChanges.getInstance(MLABEL.SELECTED).addObserver(this);

		final MList list = new MList();
		ObservedChanges.getInstance(MLABEL.SELECTED).addObserver(list);
		this.logDisplay = new JTextArea();
		this.logDisplay.setEditable(false);
		this.logDisplay.setLineWrap(true);
		this.logDisplay.setBorder(new TitledBorder(new LineBorder(Color.black),
				"Log message"));
		final JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		topPane.setLeftComponent(list.scrollPane);
		topPane.setRightComponent(this.logDisplay);

		final MCode beforeCode = new MCode(CODE.BEFORE);
		ObservedChanges.getInstance(MLABEL.SELECTED).addObserver(
				beforeCode);
		final MCode afterCode = new MCode(CODE.AFTER);
		ObservedChanges.getInstance(MLABEL.SELECTED).addObserver(
				afterCode);
		final JSplitPane codePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		codePane.add(beforeCode.scrollPane, JSplitPane.LEFT);
		codePane.add(afterCode.scrollPane, JSplitPane.RIGHT);

		final JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPane.setTopComponent(topPane);
		mainPane.setBottomComponent(codePane);
		this.getContentPane().add(mainPane, BorderLayout.CENTER);

		this.setVisible(true);

		topPane.setDividerLocation(MList.COLUMN_LENGTH_NUMBER
				+ MList.COLUMN_LENGTH_DATE + MList.COLUMN_LENGTH_PATH
				+ MList.COLUMN_LENGTH_BEFORE_POSITION
				+ MList.COLUMN_LENGTH_AFTER_POSITION);
		codePane.setDividerLocation(codePane.getWidth() / 2);
	}

	@Override
	public void update(final Observable o, final Object arg) {
		if (o instanceof ObservedChanges) {
			final ObservedChanges modifications = (ObservedChanges) o;
			if (modifications.label.equals(MLABEL.SELECTED)) {
				if (modifications.isSet()) {
					final Change m = modifications.get().first();
					this.logDisplay.setText(m.revision.message);
				} else {
					this.logDisplay.setText("");
				}
			}
		}
	}
}
