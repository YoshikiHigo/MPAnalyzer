package yoshikihigo.cpanalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import yoshikihigo.cpanalyzer.data.Change;
import yoshikihigo.cpanalyzer.data.Revision;
import yoshikihigo.cpanalyzer.db.ChangeDAO;

public class ChangeWritingThread extends Thread {

	final private BlockingQueue<Change> queue;
	final Revision[] revisions;
	private boolean finished;

	ChangeWritingThread(final BlockingQueue<Change> queue,
			final Revision[] revisions) {
		this.queue = queue;
		this.revisions = revisions;
		this.finished = false;
	}

	@Override
	public void run() {

		try {
			final ChangeDAO dao = new ChangeDAO();
			dao.addRevisions(this.revisions);

			final List<Change> changes = new ArrayList<Change>();
			while (!this.finished) {
				if (0 < this.queue.size()) {
					this.queue.drainTo(changes);
					dao.addChanges(changes);
					changes.clear();
				}
			}

			this.queue.drainTo(changes);
			dao.addChanges(changes);
			dao.flush();
			dao.close();
		}

		catch (final Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void finish() {
		this.finished = true;
	}
}
