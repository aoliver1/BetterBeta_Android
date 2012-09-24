package info.betterbeta.area;

import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.State;
import info.betterbeta.provider.BetaProvider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

public class AreaHelper {
	
	public static boolean addNewArea(Area area, ContentResolver cr) {

		String w = BetaProvider.KEY_AREA_NAME + " = ?";

		Cursor c = cr.query(Area.CONTENT_URI, null, w, new String[]{area.getName()}, null);

		int dbCount;
		if (c != null)
			dbCount = c.getCount();
		else
			dbCount = 0;

		c.close();
		if (dbCount == 0) {

			ContentValues values = new ContentValues();

			if (area.getMasterId() > 0){  // coming from web server, put ID and CLEAN state
				values.put(BetaProvider.KEY_AREA_STATE, State.CLEAN);
				values.put(BetaProvider.KEY_AREA_ID_TYPE, IdType.MASTER);
			}
			else{ // coming from user, mark as NEW for future sync AND leave MASTER as 0/default
				values.put(BetaProvider.KEY_AREA_STATE, State.NEW);
				values.put(BetaProvider.KEY_AREA_ID_TYPE, IdType.LOCAL);
			}
			values.put(BetaProvider.KEY_AREA_NAME, area.getName());
			if(null != area.getDateAdded())
				values.put(BetaProvider.KEY_AREA_DATE_ADDED, BetaProvider.sqliteDateFormater.format(area.getDateAdded()));
			else
				values.put(BetaProvider.KEY_AREA_DATE_ADDED, BetaProvider.sqliteDateFormater.format(Calendar.getInstance().getTime()));
			if(null != area.getDateModified())
				values.put(BetaProvider.KEY_AREA_DATE_MODIFIED, BetaProvider.sqliteDateFormater.format(area.getDateModified()));
			else
				values.put(BetaProvider.KEY_AREA_DATE_MODIFIED, BetaProvider.sqliteDateFormater.format(Calendar.getInstance().getTime()));

			values.put(BetaProvider.KEY_AREA_DETAILS, area.getDetails());
			values.put(BetaProvider.KEY_AREA_LATITUDE, area.getLatitude());
			values.put(BetaProvider.KEY_AREA_LONGITUDE, area.getLongitude());
			values.put(BetaProvider.KEY_AREA_PARENT, area.getParent());
			values.put(BetaProvider.KEY_AREA_MASTER, area.getMasterId());
			values.put(BetaProvider.KEY_AREA_PERMISSION, area.getPermission());

			cr.insert(Area.CONTENT_URI, values);
		}
		return true;
	}
	

	
	public static boolean updateArea(Area area, ContentResolver contentResolver){
		
		ContentValues values = new ContentValues();
		
		values.put(BetaProvider.KEY_AREA_NAME, area.getName());
		values.put(BetaProvider.KEY_AREA_DETAILS, area.getDetails());
		values.put(BetaProvider.KEY_AREA_LONGITUDE, area.getLongitude());
		values.put(BetaProvider.KEY_AREA_LATITUDE, area.getLatitude());
		values.put(BetaProvider.KEY_AREA_PARENT, area.getParent());
		values.put(BetaProvider.KEY_AREA_STATE, area.getState());
		values.put(BetaProvider.KEY_AREA_MASTER, area.getMasterId());
		values.put(BetaProvider.KEY_AREA_ID_TYPE, area.getIdType());
		values.put(BetaProvider.KEY_AREA_DATE_MODIFIED, BetaProvider.sqliteDateFormater.format(Calendar.getInstance().getTime()));
		values.put(BetaProvider.KEY_AREA_PERMISSION, area.getPermission());
		
		String where = BetaProvider.KEY_AREA_ID + " = ?";
		
		contentResolver.update(Area.CONTENT_URI, values, where, new String[]{String.valueOf(area.getId())});

		return true;
	}
	
	
	public static ArrayList<Area> getNewAndDirtyAreas(ContentResolver contentResolver) {

		Cursor c = contentResolver.query(Area.CONTENT_URI, 
						new String[]{BetaProvider.KEY_AREA_ID},
						BetaProvider.KEY_AREA_STATE + " = ? or " + 
						BetaProvider.KEY_AREA_STATE + " = ?",
						new String[]{String.valueOf(State.NEW), 
										String.valueOf(State.DIRTY)}, null);
		
		ArrayList<Area> areas = new ArrayList<Area>(c.getColumnCount());
		Area area;
		while (c.moveToNext()){
			area = AreaHelper.inflate(c.getLong(0), contentResolver, IdType.LOCAL);
			areas.add(area);
		}
		c.close();
		return areas;
	}
	
	public static Area inflate(long areaId, ContentResolver cr, int idType){
		Area area = null;	
		String whereClause;
		if(idType == IdType.LOCAL)
			whereClause = BetaProvider.KEY_AREA_ID + "= ?";
		else
			whereClause = BetaProvider.KEY_AREA_MASTER + "= ?";
		Cursor c = cr.query(Area.CONTENT_URI,  new String[] { 		
									BetaProvider.KEY_AREA_ID,
									BetaProvider.KEY_AREA_NAME,
									BetaProvider.KEY_AREA_DATE_ADDED,
									BetaProvider.KEY_AREA_DATE_MODIFIED,
									BetaProvider.KEY_AREA_DETAILS,
									BetaProvider.KEY_AREA_PARENT,
									BetaProvider.KEY_AREA_LONGITUDE, 
									BetaProvider.KEY_AREA_LATITUDE , 
									BetaProvider.KEY_AREA_MASTER , 
									BetaProvider.KEY_AREA_ID_TYPE, 
									BetaProvider.KEY_AREA_STATE, 
									BetaProvider.KEY_AREA_PERMISSION }, 
									whereClause, 
									new String[] {String.valueOf(areaId)}, 
									null);
		if(c.getCount()==1){
			area = new Area();
			c.moveToFirst();
			area.setId(areaId);
			area.setName(c.getString(1));
			try {
				area.setDateAdded(BetaProvider.sqliteDateFormater.parse(c.getString(2)));
				area.setDateModified(BetaProvider.sqliteDateFormater.parse(c.getString(3)));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			area.setDetails(c.getString(4));
			area.setParent(c.getLong(5));
			area.setLongitude(c.getString(6));
			area.setLatitude(c.getString(7));
			area.setMasterId(c.getLong(8));
			area.setIdType(c.getInt(9));
			area.setState(c.getInt(10));
			area.setPermission(c.getInt(11));
		}
		c.close();
		
		return area;
	}
	
	public static Area inflateLocation(long areaId, ContentResolver cr, int idType){
		Area area = null;	
		String whereClause = BetaProvider.KEY_AREA_ID + "= ?";
		Cursor c = cr.query(Area.CONTENT_URI,  new String[] { 		
															BetaProvider.KEY_AREA_LONGITUDE, 
															BetaProvider.KEY_AREA_LATITUDE }, 
															whereClause, 
															new String[] {String.valueOf(areaId)}, 
															null);

		if(c.getCount()==1){
			area = new Area();
			c.moveToFirst();
			area.setId(areaId);
			area.setLongitude(c.getString(0));
			area.setLatitude(c.getString(1));
		}
		c.close();
		return area;
	}

	public static Area inflatePermission(long areaId, ContentResolver cr, int idType){
		Area area = null;
		String whereClause;
		
		if(idType == IdType.LOCAL)
			whereClause = BetaProvider.KEY_AREA_ID + "= ?";
		else
			whereClause = BetaProvider.KEY_AREA_MASTER + "= ?";
	
		Cursor c = cr.query(Area.CONTENT_URI,  new String[] { 		
															BetaProvider.KEY_AREA_PERMISSION}, 
															whereClause, 
															new String[] {String.valueOf(areaId)}, 
															null);

		if(c.getCount()==1){
			area = new Area();
			c.moveToFirst();
			area.setId(areaId);
			area.setPermission(c.getInt(0));
		}
		c.close();
		return area;
	}

	
	public static void setMasterFromLocalParentArea(Area area, ContentResolver contentResolver) {

		if (area.getIdType() == IdType.LOCAL){
			Cursor c = contentResolver.query(Area.CONTENT_URI,
					new String[]{BetaProvider.KEY_AREA_MASTER},
					BetaProvider.KEY_AREA_ID + " = ?", 
					new String[]{String.valueOf(area.getParent())},
					null);
			if(c.moveToNext()){
				if (c.getLong(0) > 0){
					area.setParent(c.getLong(0));
					area.setIdType(IdType.MASTER);
				}
			}
			c.close();
		}
		
	}
}
