package info.betterbeta.model;

public class State {

	public static final int CLEAN = 0;
	public static final int NEW = 1;
	public static final int DIRTY = 2;
	
	private int state;

	public int getState() {
		return this.state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
