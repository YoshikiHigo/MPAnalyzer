package yoshikihigo.cpanalyzer;

import yoshikihigo.cpanalyzer.db.ChangePatternDAO;

public class ChangePatternMaker {

  private final CPAConfig config;

  public static void main(String[] args) {
    final CPAConfig config = CPAConfig.initialize(args);
    final ChangePatternMaker maker = new ChangePatternMaker(config);
    maker.perform();
  }

  public ChangePatternMaker(final CPAConfig config) {
    this.config = config;
  }

  public void perform() {

    final long startTime = System.nanoTime();

    if (ChangePatternDAO.SINGLETON.initialize(config)) {
      ChangePatternDAO.SINGLETON.makeIndicesOnCODES();
      ChangePatternDAO.SINGLETON.makeIndicesOnCHANGES();
      ChangePatternDAO.SINGLETON.makeChangePatterns();
    }
    ChangePatternDAO.SINGLETON.close();

    final long endTime = System.nanoTime();

    if (!config.isQUIET()) {
      System.out.print("execution time: ");
      System.out.println(TimingUtility.getExecutionTime(startTime, endTime));
    }
  }
}
