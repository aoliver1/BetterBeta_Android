package info.betterbeta.maps;

import info.betterbeta.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class CrosshairsOverlay extends Overlay {
	private Bitmap crosshairsIcon;

	//private LinearLayout layout;
	private String longitude;
	private String latitude;
	
	CrosshairsOverlay(View view, String longitude, String latitude) {
	//	this.layout = layout;
		this.longitude = longitude;
		this.latitude = latitude;
		crosshairsIcon = BitmapFactory.decodeResource(view.getResources(), R.drawable.crosshairs);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		
		Point screenCoords = new Point();
		GeoPoint crosshairLocation = new GeoPoint(0,0);
		
		try{
	    	crosshairLocation = new GeoPoint(
	                (int) (Location.convert(this.latitude) * 1E6),
	                (int) (Location.convert(this.longitude) * 1E6));
		}
		catch(IllegalArgumentException e){	
			super.draw(canvas, mapView, shadow);
			return;
		}
		mapView.getProjection().toPixels(crosshairLocation, screenCoords);
		
		canvas.drawBitmap(crosshairsIcon, screenCoords.x - crosshairsIcon.getWidth()/2, screenCoords.y - crosshairsIcon.getHeight()/2,null);
    	
		super.draw(canvas, mapView, shadow);
	}

	public String getLongitude() {
		return this.longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return this.latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	
}
