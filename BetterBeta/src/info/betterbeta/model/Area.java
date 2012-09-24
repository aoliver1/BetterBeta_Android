package info.betterbeta.model;

import info.betterbeta.provider.BetaProvider;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;

public class Area extends Locatable{

	public static final Uri CONTENT_URI = Uri.parse("content://" + BetaProvider.AUTHORITY + "/areas");
	public static final Uri CONTENT_URI_COUNT = Uri.parse("content://" + BetaProvider.AUTHORITY + "/areas/count");

	public static final int NONE_ID = 1;
	private long id;
	private String name;
	private Date dateAdded;
	private Date dateModified;
	private String details;
	private long parent;
	private int idType;
	private int permission;

	private int state;
	private long masterId;
	
	public int getPermission() {
		return this.permission;
	}

	public void setPermission(int permission) {
		this.permission = permission;
	}

	public int getIdType() {
		return this.idType;
	}

	public void setIdType(int idType) {
		this.idType = idType;
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

	public Area() {
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public void setDetails(String details) {
		this.details = details;
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

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setParent(long parent) {
		this.parent = parent;
	}
	
	public long getParent() {
		return this.parent;
	}

	public Date getDateModified() {
		return this.dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	@Override
	public String toString() {

		SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
		String dateString = sdf.format(dateAdded);
		return dateString + ": " + name + " - " + details;
	}
	


}
