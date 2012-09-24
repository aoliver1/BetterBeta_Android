package info.betterbeta.model;

import info.betterbeta.provider.BetaProvider;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;

public class Problem extends Locatable{
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + BetaProvider.AUTHORITY + "/problems");
	public static final Uri CONTENT_URI_COUNT = Uri.parse("content://" + BetaProvider.AUTHORITY + "/problems/count");

	private long id;
	private long masterId;

	private long area;
	private int idType;

	private String name;
	private Date dateAdded;
	private Date dateModified;
	private String details;
	private double rating;
	private int state;

	private int permission;
	
	public int getPermission() {
		return this.permission;
	}

	public void setPermission(int permission) {
		this.permission = permission;
	}

	public long getMasterId() {
		return this.masterId;
	}


	public void setMasterId(long masterId) {
		this.masterId = masterId;
	}


	public int getState() {
		return this.state;
	}


	public void setState(int state) {
		this.state = state;
	}


	public long getId() {
		return this.id;
	}


	public void setId(long id) {
		this.id = id;
	}

	public long getArea() {
		return this.area;
	}

	public void setArea(long area) {
		this.area = area;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public Date getDateModified() {
		return this.dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}
	public String getName() {
		return name;
	}

	public Date getDateAdded() {
		return dateAdded;
	}

	public String getDetails() {
		return details;
	}

	public double getRating() {
		return rating;
	}
	public Problem() {
		// TODO Auto-generated constructor stub
	}

	public int getIdType() {
		return this.idType;
	}

	public void setIdType(int idType) {
		this.idType = idType;
	}

	@Override
	public String toString() {

		SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
		String dateString = sdf.format(dateAdded);
		// return "(" + dateAddedString + ") " + details;
		return dateString + ": " + name + " -  " + details;
	}
	
}
