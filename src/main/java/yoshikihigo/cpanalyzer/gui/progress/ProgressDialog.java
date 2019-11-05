package yoshikihigo.cpanalyzer.gui.progress;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class ProgressDialog extends JDialog {

  final public JProgressBar progressBar;
  final public JTextField note;
  final public Canceled canceled;

  public ProgressDialog(final JFrame parent, final String title) {
    super(parent, title, true);

    this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    this.getContentPane()
        .setLayout(new BorderLayout());

    this.progressBar = new JProgressBar(0, 10000);
    this.progressBar.setStringPainted(true);
    this.progressBar.setString(null);

    this.canceled = new Canceled();

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(this.progressBar, BorderLayout.CENTER);

    this.getContentPane()
        .add(panel, BorderLayout.CENTER);

    this.note = new JTextField();
    this.note.setEditable(false);
    panel.add(this.note, BorderLayout.NORTH);

    final JButton cancelButton = new JButton("Cancel");
    this.getContentPane()
        .add(cancelButton, BorderLayout.SOUTH);
    cancelButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        canceled.setCanceled(true);
        ProgressDialog.this.dispose();
      }

    });

    this.setBounds(64, 64, 756, 128);
  }
}
