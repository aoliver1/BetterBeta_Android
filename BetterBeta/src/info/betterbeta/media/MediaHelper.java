package info.betterbeta.media;

import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Media;
import info.betterbeta.model.Problem;
import info.betterbeta.model.State;
import info.betterbeta.provider.BetaProvider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

public class MediaHelper {

	public static boolean addNewMedia(Media media, ContentResolver cr) {

		String w = BetaProvider.KEY_MEDIA_NAME + " = ? AND " + BetaProvider.KEY_AREA_ID + " = ?";

		Cursor c = cr.query(Media.CONTENT_URI, null, w, new String[] { media.getName(),
				String.valueOf(media.getArea()) }, null);

		int dbCount;
		if (c != null)
			dbCount = c.getCount();
		else
			dbCount = 0;

		c.close();
		
		if (dbCount == 0) {

			ContentValues values = new ContentValues();

			if (media.getMasterId() > 0){ // coming from web server, put ID and CLEAN state
				values.put(BetaProvider.KEY_MEDIA_STATE, State.CLEAN);
			}
			else{  // coming from user, mark as NEW for future sync
				values.put(BetaProvider.KEY_MEDIA_STATE, State.NEW);
			}	
			values.put(BetaProvider.KEY_MEDIA_ID_TYPE, media.getIdType());
			values.put(BetaProvider.KEY_MEDIA_NAME, media.getName());
			if(null != media.getDateAdded())
				values.put(BetaProvider.KEY_MEDIA_DATE_ADDED, BetaProvider.sqliteDateFormater.format(media.getDateAdded()));
			else
				values.put(BetaProvider.KEY_MEDIA_DATE_ADDED, BetaProvider.sqliteDateFormater.format(Calendar.getInstance().getTime()));
			if(null != media.getDateModified())
				values.put(BetaProvider.KEY_MEDIA_DATE_MODIFIED,  BetaProvider.sqliteDateFormater.format(media.getDateModified()));
			else
				values.put(BetaProvider.KEY_MEDIA_DATE_MODIFIED, BetaProvider.sqliteDateFormater.format(Calendar.getInstance().getTime()));
			values.put(BetaProvider.KEY_MEDIA_MASTER, media.getMasterId());	
			values.put(BetaProvider.KEY_MEDIA_DETAILS, media.getDetails());
			values.put(BetaProvider.KEY_MEDIA_LONGITUDE, media.getLongitude());
			values.put(BetaProvider.KEY_MEDIA_LATITUDE, media.getLatitude());
			values.put(BetaProvider.KEY_MEDIA_AREA, media.getArea());
			values.put(BetaProvider.KEY_MEDIA_PROBLEM, media.getProblem());
			values.put(BetaProvider.KEY_MEDIA_TYPE, media.getType());
			values.put(BetaProvider.KEY_MEDIA_PATH, media.getPath());
			values.put(BetaProvider.KEY_MEDIA_PERMISSION, media.getPermission());

			// values.put(BetaProvider.KEY_MEDIA_RATING, media.getRating());

			cr.insert(Media.CONTENT_URI, values);
			c.close();
		}
		return true;
	}

	
	public static boolean updateMedia(Media media, ContentResolver contentResolver){
		
		ContentValues values = new ContentValues();
		values.put(BetaProvider.KEY_MEDIA_NAME, media.getName());
		values.put(BetaProvider.KEY_MEDIA_DETAILS, media.getDetails());
		values.put(BetaProvider.KEY_MEDIA_LONGITUDE, media.getLongitude());
		values.put(BetaProvider.KEY_MEDIA_LATITUDE, media.getLatitude());
		values.put(BetaProvider.KEY_MEDIA_AREA, media.getArea());
		values.put(BetaProvider.KEY_MEDIA_PROBLEM, media.getProblem());
		values.put(BetaProvider.KEY_MEDIA_MASTER, media.getMasterId());
		values.put(BetaProvider.KEY_MEDIA_STATE, media.getState());
		values.put(BetaProvider.KEY_MEDIA_ID_TYPE, media.getIdType());
		values.put(BetaProvider.KEY_MEDIA_TYPE, media.getType());
		values.put(BetaProvider.KEY_MEDIA_DATE_MODIFIED, BetaProvider.sqliteDateFormater.format(Calendar.getInstance().getTime()));
		values.put(BetaProvider.KEY_MEDIA_PERMISSION, media.getPermission());
		
		String where = BetaProvider.KEY_MEDIA_ID + " = ?";
		contentResolver.update(Media.CONTENT_URI, values, where, new String[]{String.valueOf(media.getId())});
		
		return true;
	}
	
	public static ArrayList<Media> getNewAndDirtyMedias(ContentResolver contentResolver) {

		Cursor c = contentResolver.query(Media.CONTENT_URI, 
						new String[]{BetaProvider.KEY_MEDIA_ID},
						BetaProvider.KEY_MEDIA_STATE + " = ? or " + 
						BetaProvider.KEY_MEDIA_STATE + " = ?",
						new String[]{String.valueOf(State.NEW), 
										String.valueOf(State.DIRTY)}, null);
		
		ArrayList<Media> medias = new ArrayList<Media>(c.getColumnCount());
		Media media;
		while (c.moveToNext()){
			media = MediaHelper.inflate(c.getLong(0), contentResolver, IdType.LOCAL);
			medias.add(media);
		}
		c.close();
		return medias;
	}

	public static Media inflate(long mediaId, ContentResolver cr, int idType){
		Media media = null;	
		String whereClause;
		if(idType == IdType.LOCAL)
			whereClause = BetaProvider.KEY_MEDIA_ID + "= ?";
		else
			whereClause = BetaProvider.KEY_MEDIA_MASTER + "= ?";
			
		Cursor c = cr.query(Media.CONTENT_URI,  new String[] { 		
									BetaProvider.KEY_MEDIA_ID,
									BetaProvider.KEY_MEDIA_NAME,
									BetaProvider.KEY_MEDIA_DATE_ADDED,
									BetaProvider.KEY_MEDIA_DATE_MODIFIED,
									BetaProvider.KEY_MEDIA_DETAILS,
									BetaProvider.KEY_MEDIA_LONGITUDE, 
									BetaProvider.KEY_MEDIA_LATITUDE , 
									BetaProvider.KEY_MEDIA_AREA , 
									BetaProvider.KEY_MEDIA_PROBLEM , 
									BetaProvider.KEY_MEDIA_MASTER , 
									BetaProvider.KEY_MEDIA_ID_TYPE , 
									BetaProvider.KEY_MEDIA_STATE  , 
									BetaProvider.KEY_MEDIA_TYPE   , 
									BetaProvider.KEY_MEDIA_PATH   , 
									BetaProvider.KEY_MEDIA_PERMISSION }, 
									whereClause, 
									new String[] {String.valueOf(mediaId)}, 
									null);

		if(c.getCount()==1){
			media = new Media();
			c.moveToFirst();
			media.setId(mediaId);
			media.setName(c.getString(1));
			try {
				media.setDateAdded(BetaProvider.sqliteDateFormater.parse(c.getString(2)));
				media.setDateModified(BetaProvider.sqliteDateFormater.parse(c.getString(3)));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			media.setDetails(c.getString(4));
			media.setLongitude(c.getString(5));
			media.setLatitude(c.getString(6));		
			media.setArea(c.getLong(7));		
			media.setProblem(c.getLong(8));		
			media.setMasterId(c.getLong(9));		
			media.setIdType(c.getInt(10));			
			media.setState(c.getInt(11));		
			media.setType(c.getInt(12));		
			media.setPath(c.getString(13));		
			media.setPermission(c.getInt(14));		
		}
		c.close();
		return media;
	}

	
	public static Media inflateLocation(long mediaId, ContentResolver cr){
		Media media = null;	
		String whereClause = BetaProvider.KEY_MEDIA_ID + "= ?";
		Cursor c = cr.query(Media.CONTENT_URI,  new String[] { 		
								BetaProvider.KEY_MEDIA_LONGITUDE, 
								BetaProvider.KEY_MEDIA_LATITUDE }, 
								whereClause, 
								new String[] {String.valueOf(mediaId)}, 
								null);

		if(c.getCount()==1){
			media = new Media();
			c.moveToFirst();
			media.setId(mediaId);
			media.setLongitude(c.getString(0));
			media.setLatitude(c.getString(1));
		}
		
		c.close();
		return media;
	}

	public static Media inflateName(long mediaId, ContentResolver cr){
		Media media = null;	
		String whereClause = BetaProvider.KEY_MEDIA_ID + "= ?";
		Cursor c = cr.query(Media.CONTENT_URI,  new String[] { 		
								BetaProvider.KEY_MEDIA_NAME}, 
								whereClause, 
								new String[] {String.valueOf(mediaId)}, 
								null);

		if(c.getCount()==1){
			media = new Media();
			c.moveToFirst();
			media.setId(mediaId);
			media.setName(c.getString(0));
		}
		
		c.close();
		return media;
	}
	

	public static Media inflatePermission(long mediaId, ContentResolver cr, int idType){
		Media media = null;
		String whereClause;
		
		if(idType == IdType.LOCAL)
			whereClause = BetaProvider.KEY_MEDIA_ID + "= ?";
		else
			whereClause = BetaProvider.KEY_MEDIA_MASTER + "= ?";
	
		Cursor c = cr.query(Media.CONTENT_URI,  new String[] { 		
															BetaProvider.KEY_MEDIA_PERMISSION}, 
															whereClause, 
															new String[] {String.valueOf(mediaId)}, 
															null);

		if(c.getCount()==1){
			media = new Media();
			c.moveToFirst();
			media.setId(mediaId);
			media.setPermission(c.getInt(0));
		}
		c.close();
		return media;
	}

	public static void setMasterFromLocalArea(Media media, ContentResolver contentResolver) {

		if (media.getIdType() == IdType.LOCAL){
			Cursor c = contentResolver.query(Area.CONTENT_URI,
									new String[]{BetaProvider.KEY_AREA_MASTER},
									BetaProvider.KEY_AREA_ID + " = ?", 
									new String[]{String.valueOf(media.getArea())},
									null);
			if(c.moveToNext()){
				if (c.getLong(0) > 0){
					media.setArea(c.getLong(0));
					media.setIdType(IdType.MASTER);
				}
			}
			c.close();
		}
		
	}

	public static void setMasterFromLocalProblem(Media media, ContentResolver contentResolver) {

		if (media.getIdType() == IdType.LOCAL){
			Cursor c = contentResolver.query(Problem.CONTENT_URI,
									new String[]{BetaProvider.KEY_PROBLEM_MASTER},
									BetaProvider.KEY_PROBLEM_ID + " = ?", 
									new String[]{String.valueOf(media.getProblem())},
									null);
			if(c.moveToNext()){
				if (c.getLong(0) > 0){
					media.setProblem(c.getLong(0));
					media.setIdType(IdType.MASTER);
				}
			}
			c.close();
		}
		
	}
}
