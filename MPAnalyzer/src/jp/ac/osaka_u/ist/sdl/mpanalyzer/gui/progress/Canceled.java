package jp.ac.osaka_u.ist.sdl.mpanalyzer.gui.progress;

public class Canceled {

	private boolean canceled;

	Canceled() {
		this.canceled = false;
	}

	void setCanceled(final boolean canceled) {
		this.canceled = canceled;
	}

	public boolean isCanceled() {
		return this.canceled;
	}
}
