package yoshikihigo.cpanalyzer.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import yoshikihigo.cpanalyzer.data.Change.ChangeType;
import yoshikihigo.cpanalyzer.data.Change.DiffType;
import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.db.ReadOnlyDAO;
import yoshikihigo.cpanalyzer.gui.ObservedChangePatterns.CPLABEL;

public class ThresholdPanel extends JPanel implements Observer {

  public static final int TEXTFIELD_LENGTH = 5;

  public ThresholdPanel() {
    this.setBorder(new TitledBorder(new LineBorder(Color.black), "Configuration"));

    final JButton queryButton = new JButton("Quering DB");
    final JTextField supportMinField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField supportMaxField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField confidenceMinField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField confidenceMaxField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField nodMinField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField nodMaxField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField nofMinField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField nofMaxField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField lbmMinField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField lbmMaxField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField lamMinField = new JTextField(TEXTFIELD_LENGTH);
    final JTextField lamMaxField = new JTextField(TEXTFIELD_LENGTH);
    final JCheckBox type2CheckBox = new JCheckBox("TYPE2", true);
    final JCheckBox type3CheckBox = new JCheckBox("TYPE3", true);
    final JCheckBox replaceCheckBox = new JCheckBox("REPLACE", true);
    final JCheckBox addCheckBox = new JCheckBox("ADD", true);
    final JCheckBox deleteCheckBox = new JCheckBox("DELETE", true);

    supportMinField.setText("2");
    supportMaxField.setText("9999");
    confidenceMinField.setText("0");
    confidenceMaxField.setText("1");
    nodMinField.setText("0");
    nodMaxField.setText("9999");
    nofMinField.setText("1");
    nofMaxField.setText("9999");
    lbmMinField.setText("0");
    lbmMaxField.setText("9999");
    lamMinField.setText("0");
    lamMaxField.setText("9999");

    // this.setLayout(new FlowLayout(7, 5, 0));
    this.setLayout(new GridLayout(2, 5));

    {
      final JPanel supportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
      supportPanel.setBorder(new TitledBorder(new LineBorder(Color.black), "SUPPORT"));
      supportPanel.add(new JLabel("Min: "));
      supportPanel.add(supportMinField);
      supportPanel.add(new JPanel());
      supportPanel.add(new JLabel("Max: "));
      supportPanel.add(supportMaxField);
      this.add(supportPanel);
    }

    {
      final JPanel confidencePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
      confidencePanel.setBorder(new TitledBorder(new LineBorder(Color.black), "CONFIDENCE"));
      confidencePanel.add(new JLabel("Min: "));
      confidencePanel.add(confidenceMinField);
      confidencePanel.add(new JPanel());
      confidencePanel.add(new JLabel("Max: "));
      confidencePanel.add(confidenceMaxField);
      this.add(confidencePanel);
    }

    {
      final JPanel nodPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
      nodPanel.setBorder(new TitledBorder(new LineBorder(Color.black), "NOD"));
      nodPanel.add(new JLabel("Min: "));
      nodPanel.add(nodMinField);
      nodPanel.add(new JPanel());
      nodPanel.add(new JLabel("Max: "));
      nodPanel.add(nodMaxField);
      this.add(nodPanel);
    }

    {
      final JPanel nofPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
      nofPanel.setBorder(new TitledBorder(new LineBorder(Color.black), "NOF"));
      nofPanel.add(new JLabel("Min: "));
      nofPanel.add(nofMinField);
      nofPanel.add(new JPanel());
      nofPanel.add(new JLabel("Max: "));
      nofPanel.add(nofMaxField);
      this.add(nofPanel);
    }

    {
      final JPanel lbmPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
      lbmPanel
          .setBorder(new TitledBorder(new LineBorder(Color.black), "LBM (Length Before Change)"));
      lbmPanel.add(new JLabel("Min: "));
      lbmPanel.add(lbmMinField);
      lbmPanel.add(new JPanel());
      lbmPanel.add(new JLabel("Max: "));
      lbmPanel.add(lbmMaxField);
      this.add(lbmPanel);
    }

    {
      final JPanel lamPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
      lamPanel
          .setBorder(new TitledBorder(new LineBorder(Color.black), "LAM (Length After Change)"));
      lamPanel.add(new JLabel("Min: "));
      lamPanel.add(lamMinField);
      lamPanel.add(new JPanel());
      lamPanel.add(new JLabel("Max: "));
      lamPanel.add(lamMaxField);
      this.add(lamPanel);
    }

    {
      final JPanel changePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
      changePanel.setBorder(new TitledBorder(new LineBorder(Color.black), "REPLACE/ADD/DELETE"));
      changePanel.add(replaceCheckBox);
      changePanel.add(addCheckBox);
      changePanel.add(deleteCheckBox);
      this.add(changePanel);
    }

    {
      final JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
      typePanel.setBorder(new TitledBorder(new LineBorder(Color.black), "TYPE-2/TYPE-3"));
      typePanel.add(type2CheckBox);
      typePanel.add(type3CheckBox);
      this.add(typePanel);
    }

    this.add(queryButton);

    queryButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

        queryButton.setEnabled(false);

        int supportMin = 1;
        int supportMax = Integer.MAX_VALUE;
        try {
          final String textMin = supportMinField.getText();
          supportMin = Integer.parseInt(textMin);
          if (supportMin < 1) {
            throw new NumberFormatException();
          }
          final String textMax = supportMaxField.getText();
          supportMax = Integer.parseInt(textMax);
          if (supportMax < 1) {
            throw new NumberFormatException();
          }
        } catch (final NumberFormatException supportException) {
          JOptionPane.showMessageDialog(ThresholdPanel.this, "SUPPORT must be positive integer.",
              "", JOptionPane.ERROR_MESSAGE);
          return;
        }

        float confidenceMin = 0f;
        float confidenceMax = 1f;
        try {
          final String textMin = confidenceMinField.getText();
          confidenceMin = Float.parseFloat(textMin);
          if ((confidenceMin < 0f) || (1f < confidenceMin)) {
            throw new NumberFormatException();
          }
          final String textMax = confidenceMaxField.getText();
          confidenceMax = Float.parseFloat(textMax);
          if ((confidenceMax < 0f) || (1f < confidenceMax)) {
            throw new NumberFormatException();
          }

        } catch (final NumberFormatException confidenceException) {
          JOptionPane.showMessageDialog(ThresholdPanel.this,
              "CONFIDENCE must be float value between 0 and 1.", "", JOptionPane.ERROR_MESSAGE);
          return;
        }

        int nodMin = 0;
        int nodMax = Integer.MAX_VALUE;
        try {
          final String textMin = nodMinField.getText();
          nodMin = Integer.parseInt(textMin);
          if (nodMin < 0) {
            throw new NumberFormatException();
          }
          final String textMax = nodMaxField.getText();
          nodMax = Integer.parseInt(textMax);
          if (nodMax < 0) {
            throw new NumberFormatException();
          }
        } catch (final NumberFormatException nodException) {
          JOptionPane.showMessageDialog(ThresholdPanel.this, "NOD must be 0 or positive integer.",
              "", JOptionPane.ERROR_MESSAGE);
          return;
        }

        int nofMin = 0;
        int nofMax = Integer.MAX_VALUE;
        try {
          final String textMin = nofMinField.getText();
          nofMin = Integer.parseInt(textMin);
          if (nofMin < 1) {
            throw new NumberFormatException();
          }
          final String textMax = nofMaxField.getText();
          nofMax = Integer.parseInt(textMax);
          if (nofMax < 1) {
            throw new NumberFormatException();
          }
        } catch (final NumberFormatException nofException) {
          JOptionPane.showMessageDialog(ThresholdPanel.this, "NOF must be positive integer.", "",
              JOptionPane.ERROR_MESSAGE);
          return;
        }

        int lbmMin = 0;
        int lbmMax = Integer.MAX_VALUE;
        try {
          final String textMin = lbmMinField.getText();
          lbmMin = Integer.parseInt(textMin);
          if (lbmMin < 0) {
            throw new NumberFormatException();
          }
          final String textMax = lbmMaxField.getText();
          lbmMax = Integer.parseInt(textMax);
          if (lbmMax < 0) {
            throw new NumberFormatException();
          }
        } catch (final NumberFormatException nofException) {
          JOptionPane.showMessageDialog(ThresholdPanel.this, "LBM must be 0 or positive integer.",
              "", JOptionPane.ERROR_MESSAGE);
          return;
        }

        int lamMin = 0;
        int lamMax = Integer.MAX_VALUE;
        try {
          final String textMin = lamMinField.getText();
          lamMin = Integer.parseInt(textMin);
          if (lamMin < 0) {
            throw new NumberFormatException();
          }
          final String textMax = lamMaxField.getText();
          lamMax = Integer.parseInt(textMax);
          if (lamMax < 0) {
            throw new NumberFormatException();
          }
        } catch (final NumberFormatException nofException) {
          JOptionPane.showMessageDialog(ThresholdPanel.this, "LAM must be 0 or positive integer.",
              "", JOptionPane.ERROR_MESSAGE);
          return;
        }

        final boolean type2checked = type2CheckBox.isSelected();
        final boolean type3checked = type3CheckBox.isSelected();
        if (!type2checked && !type3checked) {
          JOptionPane.showMessageDialog(ThresholdPanel.this,
              "At least one check box must be checked.", "", JOptionPane.ERROR_MESSAGE);
          return;
        }

        final boolean changeChecked = replaceCheckBox.isSelected();
        final boolean addChecked = addCheckBox.isSelected();
        final boolean deleteChecked = deleteCheckBox.isSelected();
        if (!changeChecked && !addChecked && !deleteChecked) {
          JOptionPane.showMessageDialog(ThresholdPanel.this,
              "At least one check box must be checked.", "", JOptionPane.ERROR_MESSAGE);
          return;
        }

        try {
          final List<ChangePattern> patterns =
              ReadOnlyDAO.SINGLETON.getChangePatterns(supportMin, confidenceMin);
          for (final Iterator<ChangePattern> iterator = patterns.iterator(); iterator.hasNext();) {
            final ChangePattern pattern = iterator.next();
            if ((pattern.getNOD() < nodMin) || (pattern.getNOF() < nofMin)
                || (pattern.getLBM() < lbmMin) || (pattern.getLAM() < lamMin)) {
              iterator.remove();
              continue;
            }

            if ((pattern.getNOD() > nodMax) || (pattern.getNOF() > nofMax)
                || (pattern.getLBM() > lbmMax) || (pattern.getLAM() > lamMax)) {
              iterator.remove();
              continue;
            }

            if (!type2checked && pattern.diffType.getValue() == DiffType.TYPE2.getValue()) {
              iterator.remove();
              continue;
            }

            if (!type3checked && pattern.diffType.getValue() == DiffType.TYPE3.getValue()) {
              iterator.remove();
              continue;
            }

            if (!changeChecked && pattern.changeType.getValue() == ChangeType.REPLACE.getValue()) {
              iterator.remove();
              continue;
            }

            if (!addChecked && pattern.changeType.getValue() == ChangeType.ADD.getValue()) {
              iterator.remove();
              continue;
            }

            if (!deleteChecked && pattern.changeType.getValue() == ChangeType.DELETE.getValue()) {
              iterator.remove();
              continue;
            }
          }

          ObservedChangePatterns.getInstance(CPLABEL.ALL)
              .setAll(patterns, ThresholdPanel.this);
          ObservedChangePatterns.getInstance(CPLABEL.FILTERED)
              .setAll(patterns, ThresholdPanel.this);
          ObservedChangePatterns.getInstance(CPLABEL.SELECTED)
              .clear(ThresholdPanel.this);

        } catch (final Exception databaseException) {
          JOptionPane.showMessageDialog(ThresholdPanel.this, "Couldn't connect to database.", "",
              JOptionPane.ERROR_MESSAGE);
          return;
        }

        queryButton.setEnabled(true);
      }
    });
  }

  @Override
  public void update(Observable o, Object arg) {}
}
