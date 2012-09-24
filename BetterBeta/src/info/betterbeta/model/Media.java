package info.betterbeta.model;

import info.betterbeta.provider.BetaProvider;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;

public class Media extends Locatable{

	public static final int TYPE_URL_PIC = 1;
	public static final int TYPE_URI_PIC = 2;
	public static final int TYPE_URL_YOUTUBE = 3;
	public static final int TYPE_TXT = 4;
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + BetaProvider.AUTHORITY + "/media");
	public static final Uri CONTENT_URI_COUNT = Uri.parse("content://" + BetaProvider.AUTHORITY + "/media/count");

	private long id;
	private long masterId;
	private String name;
	private Date dateAdded;
	private Date dateModified;
	private String details;
	private String path;
	private int type; // Media.TYPE_XXX aka local pic or url of pic or url of youtube video, etc
	
	private long problem; 
	private long area; 
	private int idType;  // LOCAL or MASTER
	
	private int state;
	private int permission;
	
	public int getPermission() {
		return this.permission;
	}

	public void setPermission(int permission) {
		this.permission = permission;
	}


	public int getState() {
		return this.state;
	}

	public void setState(int state) {
		this.state = state;
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

	public void setDetails(String details) {
		this.details = details;
	}

	public String getName() {
		return name;
	}

	public Date getDateAdded() {
		return dateAdded;
	}

	public Date getDateModified() {
		return this.dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	public String getDetails() {
		return details;
	}

	public int getType() {
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getProblem() {
		return this.problem;
	}

	public void setProblem(long problem) {
		this.problem = problem;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	public Media() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {

		SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
		String dateString = sdf.format(dateAdded);
		return dateString + ": " + name + " - " + details;
	}

}
