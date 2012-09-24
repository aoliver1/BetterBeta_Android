package info.betterbeta.maps;

import info.betterbeta.R;
import info.betterbeta.area.AreaDetail;
import info.betterbeta.model.Area;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class AreaItemizedOverlay extends ItemizedOverlay<AreaOverlayItem> {

	Context context;
	private ArrayList<AreaOverlayItem> areas = new ArrayList<AreaOverlayItem>();
	public AreaItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	}

	@Override
	protected AreaOverlayItem createItem(int i) {
		return areas.get(i);
	}

	@Override
	public int size() {
		return areas.size();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
	}
	
	@Override
	protected boolean onTap(int index) {

		final AreaOverlayItem areaOverlay = getItem(index);
		
		@SuppressWarnings("unused")
		AlertDialog areYouSure = new AlertDialog.Builder(context)
	       .setTitle(areaOverlay.getTitle())
	        .setPositiveButton(R.string.view, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	
	    			context.startActivity(new Intent(context, AreaDetail.class).putExtra("area_id", areaOverlay.getProblemId()));
	            }
	        })
	        .setNegativeButton(R.string.hide, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                
	            }
	        })
	        .show();
		return true;
	}
	public void addArea(Area area) {
		
		try{
		GeoPoint areaLocation = new GeoPoint(
                (int) (Location.convert(area.getLatitude()) * 1E6),
                (int) (Location.convert(area.getLongitude()) * 1E6));
		
		areas.add(new AreaOverlayItem(areaLocation, area.getName(), area.getDetails(), area.getId()));
	    populate();
		}
		catch (Exception e){
			Log.v("AreaOverlay", "bad data:" + area.getName());
		}
	}

}
