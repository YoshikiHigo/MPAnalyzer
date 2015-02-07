package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ChangePatternDAO;

public class ChangePatternMaker {

	public static void main(String[] args) {

		final long startTime = System.nanoTime();

		Config.initialize(args);
		final ChangePatternDAO dao = new ChangePatternDAO();
		dao.makeChangePatterns();
		dao.close();

		final long endTime = System.nanoTime();
		System.out.print("execution time: ");
		System.out.println(TimingUtility.getExecutionTime(startTime, endTime));
	}
}
