package jp.ac.osaka_u.ist.sdl.mpanalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ModificationDAO;

public class ModificationWritingThread extends Thread {

	final private BlockingQueue<Modification> queue;
	private boolean finished;

	ModificationWritingThread(final BlockingQueue<Modification> queue) {
		this.queue = queue;
		this.finished = false;
	}

	@Override
	public void run() {

		try {

			final ModificationDAO dao = new ModificationDAO();

			final List<Modification> modifications = new ArrayList<Modification>();
			while (!this.finished) {
				if (0 < this.queue.size()) {
					this.queue.drainTo(modifications);
					dao.addModifications(modifications);
					modifications.clear();
				}
				// Thread.sleep(100);
			}

			this.queue.drainTo(modifications);
			dao.addModifications(modifications);
			dao.flush();

			dao.close();

		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void finish() {
		this.finished = true;
	}
}
