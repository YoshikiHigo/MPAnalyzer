package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.MPDAO;

public class MPMaker {

	public static void main(String[] args) {

		try {

			final long startTime = System.nanoTime();

			System.out.println("making modification patterns ...");
			final MPDAO dao = new MPDAO();
			dao.makeModificationPatterns();
			dao.close();

			// 実行時間を表示
			final long endTime = System.nanoTime();
			System.out.print("execution time: ");
			System.out.println(TimingUtility.getExecutionTime(startTime,
					endTime));

		} catch (final Exception e) {
			e.printStackTrace();
		}

	}
}
