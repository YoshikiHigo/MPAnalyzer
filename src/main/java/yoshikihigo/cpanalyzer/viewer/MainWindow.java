package yoshikihigo.cpanalyzer.viewer;

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

import yoshikihigo.cpanalyzer.CPAConfig;
import yoshikihigo.cpanalyzer.db.ReadOnlyDAO;
import yoshikihigo.cpanalyzer.viewer.ObservedChangePatterns.CPLABEL;
import yoshikihigo.cpanalyzer.viewer.clpanel.CLPanel;
import yoshikihigo.cpanalyzer.viewer.cpcode.CPCode;
import yoshikihigo.cpanalyzer.viewer.cplist.CPList;
import yoshikihigo.cpanalyzer.viewer.graph.PCGraph;

public class MainWindow extends JFrame {

  public static void main(String[] args) {

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final Exception e) {
    }

    CPAConfig.initialize(args);
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        new MainWindow();
      }
    });
  }

  public MainWindow() {
    super("");

    ReadOnlyDAO.SINGLETON.initialize();

    final Dimension d = Toolkit.getDefaultToolkit()
        .getScreenSize();
    this.setSize(new Dimension(d.width - 5, d.height - 27));
    this.addWindowListener(new MainWindowListener());

    final ThresholdPanel threshold = new ThresholdPanel();
    ObservedChangePatterns.getInstance(CPLABEL.ALL)
        .addObserver(threshold);
    ObservedChangePatterns.getInstance(CPLABEL.FILTERED)
        .addObserver(threshold);
    ObservedChangePatterns.getInstance(CPLABEL.SELECTED)
        .addObserver(threshold);
    this.getContentPane()
        .add(threshold, BorderLayout.NORTH);

    final JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    final PCGraph graph = new PCGraph();
    ObservedChangePatterns.getInstance(CPLABEL.ALL)
        .addObserver(graph);
    ObservedChangePatterns.getInstance(CPLABEL.FILTERED)
        .addObserver(graph);
    ObservedChangePatterns.getInstance(CPLABEL.SELECTED)
        .addObserver(graph);
    topPane.add(graph, JSplitPane.LEFT);

    final JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    final CPList list = new CPList();
    ObservedChangePatterns.getInstance(CPLABEL.ALL)
        .addObserver(list);
    ObservedChangePatterns.getInstance(CPLABEL.FILTERED)
        .addObserver(list);
    ObservedChangePatterns.getInstance(CPLABEL.SELECTED)
        .addObserver(list);
    rightPane.setTopComponent(list.scrollPane);
    final CLPanel clPanel = new CLPanel();
    ObservedChangePatterns.getInstance(CPLABEL.SELECTED)
        .addObserver(clPanel);
    rightPane.setBottomComponent(clPanel.scrollPane);
    topPane.add(rightPane, JSplitPane.RIGHT);

    final CPCode beforeCode = new CPCode(CODE.BEFORE);
    ObservedChangePatterns.getInstance(CPLABEL.ALL)
        .addObserver(beforeCode);
    ObservedChangePatterns.getInstance(CPLABEL.FILTERED)
        .addObserver(beforeCode);
    ObservedChangePatterns.getInstance(CPLABEL.SELECTED)
        .addObserver(beforeCode);

    final CPCode afterCode = new CPCode(CODE.AFTER);
    ObservedChangePatterns.getInstance(CPLABEL.ALL)
        .addObserver(afterCode);
    ObservedChangePatterns.getInstance(CPLABEL.FILTERED)
        .addObserver(afterCode);
    ObservedChangePatterns.getInstance(CPLABEL.SELECTED)
        .addObserver(afterCode);

    final JSplitPane bottomPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    bottomPane.add(beforeCode.scrollPane, JTabbedPane.LEFT);
    bottomPane.add(afterCode.scrollPane, JTabbedPane.RIGHT);

    final JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    mainPane.add(topPane, JTabbedPane.TOP);
    mainPane.add(bottomPane, JTabbedPane.BOTTOM);

    this.getContentPane()
        .add(mainPane, BorderLayout.CENTER);

    rightPane.setDividerLocation(d.height - 550);
    topPane.setDividerLocation(d.width / 2);
    bottomPane.setDividerLocation(d.width / 2);
    mainPane.setDividerLocation(d.height - 350);

    this.setVisible(true);
  }

  static class MainWindowListener implements WindowListener {

    public void windowActivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {
      System.exit(0);
    }

    public void windowDeactivated(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}
  }
}
