package yoshikihigo.cpanalyzer;

import yoshikihigo.cpanalyzer.db.ChangePatternDAO;

public class ChangePatternMaker {

	public static void main(String[] args) {

		final long startTime = System.nanoTime();

		CPAConfig.initialize(args);
		final ChangePatternDAO dao = ChangePatternDAO.SINGLETON;
		if (dao.initialize()) {
			dao.makeIndicesOnCODES();
			dao.makeIndicesOnCHANGES();
			dao.makeChangePatterns();
		}
		dao.close();

		final long endTime = System.nanoTime();

		if (!CPAConfig.getInstance().isQUIET()) {
			System.out.print("execution time: ");
			System.out.println(TimingUtility.getExecutionTime(startTime,
					endTime));
		}
	}
}
