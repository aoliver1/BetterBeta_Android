package info.betterbeta.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class ProblemOverlayItem extends OverlayItem {

	long problemId;
	public ProblemOverlayItem(GeoPoint point, String title, String snippet, long id) {
		super(point, title, snippet);
		this.problemId = id;
		// TODO Auto-generated constructor stub
	}
	public long getProblemId() {
		return this.problemId;
	}
	public void setProblemId(long problemId) {
		this.problemId = problemId;
	}
	
	

}
