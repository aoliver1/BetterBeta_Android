package info.betterbeta.model;

import android.location.Location;
import android.provider.Contacts;

import com.google.android.maps.GeoPoint;

public abstract class Locatable {
	
	private String longitude;
	private String latitude;
	private float accuracy;
	private float bearing;
	Contacts contact;

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
	public float getAccuracy() {
		return this.accuracy;
	}
	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}
	public float getBearing() {
		return this.bearing;
	}
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	public Contacts getContact() {
		return this.contact;
	}
	public void setContact(Contacts contact) {
		this.contact = contact;
	}
	
	public static GeoPoint getGeoPointFromStrings(String longitude, String latitude){	
    	try{
        	return new GeoPoint(
                    (int) (Location.convert(latitude) * 1E6),
                    (int) (Location.convert(longitude) * 1E6));
        	}
        	catch(IllegalArgumentException e){	
        		
        	}
        	return new GeoPoint(
                    (int) (Location.convert(latitude.substring(0, latitude.indexOf("."))) * 1E6),
                    (int) (Location.convert(longitude.substring(0, latitude.indexOf("."))) * 1E6));
    }

}
