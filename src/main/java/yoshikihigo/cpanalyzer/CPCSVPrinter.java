package yoshikihigo.cpanalyzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.ChangePattern;
import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.db.ReadOnlyDAO;

public class CPCSVPrinter {

  public static void main(String[] args) {

    try {

      final long startTime = System.nanoTime();

      CPAConfig.initialize(args);
      final String CVSFILE = CPAConfig.getInstance()
          .getCSV_FILE();

      System.out.println("outputing to " + CVSFILE + " ... ");

      final BufferedWriter writer = new BufferedWriter(new FileWriter(CVSFILE));

      writer
          .write("ID, SUPPORT, CONFIDENCE, EREVISION, SREVISION, EDATE, SDATE, NOD, NOF, LBM, LAM");
      writer.newLine();

      final List<ChangePattern> patterns = ReadOnlyDAO.SINGLETON.getChangePatterns(0, 0);
      for (final ChangePattern pattern : patterns) {

        writer.write(Integer.toString(pattern.id));
        writer.write(", ");
        writer.write(Integer.toString(pattern.support));
        writer.write(", ");
        writer.write(Float.toString(pattern.confidence));
        writer.write(", ");

        final List<Change> changes = pattern.getChanges();
        Revision sRevision = changes.get(0).revision;
        Revision eRevision = changes.get(0).revision;
        for (final Change change : changes) {
          if (change.revision.compareTo(sRevision) < 0) {
            sRevision = change.revision;
          }
          if (change.revision.compareTo(eRevision) > 0) {
            eRevision = change.revision;
          }
        }

        writer.write(eRevision.id);
        writer.write(", ");
        writer.write(sRevision.id);
        writer.write(", ");
        writer.write(eRevision.date);
        writer.write(", ");
        writer.write(sRevision.date);

        writer.write(", ");
        writer.write(Integer.toString(pattern.getNOD()));
        writer.write(", ");
        writer.write(Integer.toString(pattern.getNOF()));
        writer.write(", ");
        writer.write(Integer.toString(pattern.getLBM()));
        writer.write(", ");
        writer.write(Integer.toString(pattern.getLAM()));

        writer.newLine();
      }

      writer.flush();
      writer.close();

      final long endTime = System.nanoTime();
      System.out.print("execution time: ");
      System.out.println(TimingUtility.getExecutionTime(startTime, endTime));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
