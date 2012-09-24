package info.betterbeta.maps;

import info.betterbeta.R;
import info.betterbeta.model.Problem;
import info.betterbeta.problem.ProblemDetail;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class ProblemItemizedOverlay extends ItemizedOverlay<ProblemOverlayItem> {

	Context context;
	private ArrayList<ProblemOverlayItem> problems = new ArrayList<ProblemOverlayItem>();
	public ProblemItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	}

	@Override
	protected ProblemOverlayItem createItem(int i) {
		return problems.get(i);
	}

	@Override
	public int size() {
		return problems.size();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
	}
	
	@Override
	protected boolean onTap(int index) {

		final ProblemOverlayItem problemOverlay = getItem(index);
		
		@SuppressWarnings("unused")
		AlertDialog areYouSure = new AlertDialog.Builder(context)
	       .setTitle(problemOverlay.getTitle())
	        .setPositiveButton(R.string.view, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	
	    			context.startActivity(new Intent(context, ProblemDetail.class).putExtra("problem_id", problemOverlay.getProblemId()));
	            }
	        })
	        .setNegativeButton(R.string.hide, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                
	            }
	        })
	        .show();
		return true;
	}
	public void addProblem(Problem problem) {
		GeoPoint problemLocation = new GeoPoint(
                (int) (Location.convert(problem.getLatitude()) * 1E6),
                (int) (Location.convert(problem.getLongitude()) * 1E6));
		
		problems.add(new ProblemOverlayItem(problemLocation, problem.getName(), problem.getDetails(), problem.getId()));
	    populate();
	}
	
	
	

}
