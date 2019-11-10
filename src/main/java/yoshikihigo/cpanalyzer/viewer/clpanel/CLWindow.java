package yoshikihigo.cpanalyzer.viewer.clpanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CLWindow extends JFrame {

  public CLWindow(final String text) {

    Dimension d = Toolkit.getDefaultToolkit()
        .getScreenSize();
    this.setSize(new Dimension(d.width / 2, d.height - 27));
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    final JTextArea textArea = new JTextArea();
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setText(text);

    final JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportView(textArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    this.getContentPane()
        .add(scrollPane, BorderLayout.CENTER);

    this.setVisible(true);

    textArea.addMouseListener(new MouseListener() {

      @Override
      public void mouseClicked(final MouseEvent e) {
        final int button = e.getButton();
        if (MouseEvent.BUTTON1 == button) {
          if (e.getClickCount() == 2) {
            CLWindow.this.dispose();
          }
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {}

      @Override
      public void mouseReleased(MouseEvent e) {}

      @Override
      public void mouseEntered(MouseEvent e) {}

      @Override
      public void mouseExited(MouseEvent e) {}
    });
  }
}
