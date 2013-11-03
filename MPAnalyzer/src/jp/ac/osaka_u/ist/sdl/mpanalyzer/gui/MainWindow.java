package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.ObservedModificationPatterns.MPLABEL;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.clpanel.CLPanel;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.graph.PCGraph;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mpcode.MPCode;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.mplist.MPList;

public class MainWindow extends JFrame {

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainWindow();
			}
		});
	}

	public MainWindow() {
		super("");

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(d.width - 5, d.height - 27));
		this.addWindowListener(new MainWindowListener());

		final ThresholdPanel threshold = new ThresholdPanel();
		ObservedModificationPatterns.getInstance(MPLABEL.ALL).addObserver(
				threshold);
		ObservedModificationPatterns.getInstance(MPLABEL.FILTERED).addObserver(
				threshold);
		ObservedModificationPatterns.getInstance(MPLABEL.SELECTED).addObserver(
				threshold);
		this.getContentPane().add(threshold, BorderLayout.NORTH);

		final CLPanel clPanel = new CLPanel();
		ObservedModificationPatterns.getInstance(MPLABEL.SELECTED).addObserver(
				clPanel);
		this.getContentPane().add(clPanel.scrollPane, BorderLayout.SOUTH);

		final JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		final PCGraph graph = new PCGraph();
		ObservedModificationPatterns.getInstance(MPLABEL.ALL)
				.addObserver(graph);
		ObservedModificationPatterns.getInstance(MPLABEL.FILTERED).addObserver(
				graph);
		ObservedModificationPatterns.getInstance(MPLABEL.SELECTED).addObserver(
				graph);
		topPane.add(graph, JSplitPane.LEFT);

		final MPList list = new MPList();
		ObservedModificationPatterns.getInstance(MPLABEL.ALL).addObserver(list);
		ObservedModificationPatterns.getInstance(MPLABEL.FILTERED).addObserver(
				list);
		ObservedModificationPatterns.getInstance(MPLABEL.SELECTED).addObserver(
				list);
		topPane.add(list.scrollPane, JSplitPane.RIGHT);

		final MPCode beforeCode = new MPCode(CODE.BEFORE);
		ObservedModificationPatterns.getInstance(MPLABEL.ALL).addObserver(
				beforeCode);
		ObservedModificationPatterns.getInstance(MPLABEL.FILTERED).addObserver(
				beforeCode);
		ObservedModificationPatterns.getInstance(MPLABEL.SELECTED).addObserver(
				beforeCode);

		final MPCode afterCode = new MPCode(CODE.AFTER);
		ObservedModificationPatterns.getInstance(MPLABEL.ALL).addObserver(
				afterCode);
		ObservedModificationPatterns.getInstance(MPLABEL.FILTERED).addObserver(
				afterCode);
		ObservedModificationPatterns.getInstance(MPLABEL.SELECTED).addObserver(
				afterCode);

		final JSplitPane bottomPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT);
		bottomPane.add(beforeCode.scrollPane, JTabbedPane.LEFT);
		bottomPane.add(afterCode.scrollPane, JTabbedPane.RIGHT);

		final JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPane.add(topPane, JTabbedPane.TOP);
		mainPane.add(bottomPane, JTabbedPane.BOTTOM);

		this.getContentPane().add(mainPane, BorderLayout.CENTER);

		topPane.setDividerLocation(d.width / 2);
		bottomPane.setDividerLocation(d.width / 2);
		mainPane.setDividerLocation(d.height / 2);

		this.setVisible(true);
	}

	class MainWindowListener implements WindowListener {

		public void windowActivated(WindowEvent e) {
		}

		public void windowClosed(WindowEvent e) {
		}

		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}

		public void windowDeactivated(WindowEvent e) {
		}

		public void windowDeiconified(WindowEvent e) {
		}

		public void windowIconified(WindowEvent e) {
		}

		public void windowOpened(WindowEvent e) {
		}
	}
}
