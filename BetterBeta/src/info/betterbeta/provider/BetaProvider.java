package info.betterbeta.provider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class BetaProvider extends ContentProvider {

    public static final String AUTHORITY = "info.betterbeta";
    
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	// Create the constants used to differentiate between the different URI requests.
	private static final int PROBLEMS = 1;
	private static final int PROBLEM_ID = 2;
	private static final int AREAS = 3;
	private static final int AREA_ID = 4;
	private static final int MEDIA = 5;
	private static final int MEDIA_ID = 6;
	private static final int PROBLEM_COUNT = 7;
	private static final int AREA_COUNT = 8;
	private static final int MEDIA_COUNT = 9;

	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	// Allocate the UriMatcher object, where a URI ending in 'problems' will
	// correspond to a request for all problems, and 'problems’ with a
	// trailing '/[rowID]' will represent a single problem row.
	static {
		uriMatcher.addURI(AUTHORITY, "problems", PROBLEMS);
		uriMatcher.addURI(AUTHORITY, "problems/count", PROBLEM_COUNT);
		uriMatcher.addURI(AUTHORITY, "problems/#", PROBLEM_ID);
		uriMatcher.addURI(AUTHORITY, "areas", AREAS);
		uriMatcher.addURI(AUTHORITY, "areas/count", AREA_COUNT);
		uriMatcher.addURI(AUTHORITY, "areas/#", AREA_ID);
		uriMatcher.addURI(AUTHORITY, "media", MEDIA);
		uriMatcher.addURI(AUTHORITY, "media/count", MEDIA_COUNT);
		uriMatcher.addURI(AUTHORITY, "media/#", MEDIA_ID);
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case PROBLEMS:
			return "vnd.android.cursor.dir/vnd.betterbeta.problem";
		case PROBLEM_ID:
			return "vnd.android.cursor.item/vnd.betterbeta.problem";
		case AREAS:
			return "vnd.android.cursor.dir/vnd.betterbeta.area";
		case AREA_ID:
			return "vnd.android.cursor.item/vnd.betterbeta.area";
		case MEDIA:
			return "vnd.android.cursor.dir/vnd.betterbeta.media";
		case MEDIA_ID:
			return "vnd.android.cursor.item/vnd.betterbeta.media";
		case PROBLEM_COUNT:
			return "vnd.android.cursor.item/vnd.betterbeta.problem";
		case AREA_COUNT:
			return "vnd.android.cursor.item/vnd.betterbeta.area";
		case MEDIA_COUNT:
			return "vnd.android.cursor.item/vnd.betterbeta.media";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		problemDatabaseHelper dbHelper = new problemDatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		problemDB = dbHelper.getWritableDatabase();
		if(problemDB.getVersion() < DATABASE_VERSION)
			dbHelper.onUpgrade(problemDB, problemDB.getVersion(), DATABASE_VERSION);
	//	dbHelper.onUpgrade(problemDB, 1, DATABASE_VERSION);
	//	dbHelper.dumpDb(problemDB);
		return (problemDB == null) ? false : true;
	}
	
	@Override
	public Uri insert(Uri _uri, ContentValues _initialValues) {
		// Insert the new row, will return the row number if successful.
		long rowID = 0;
		
		switch (uriMatcher.match(_uri)) {
		case PROBLEMS:
			rowID = problemDB.insert(PROBLEM_TABLE, "problems", _initialValues);
			break;
		case AREAS:
			rowID = problemDB.insert(AREA_TABLE, "areas", _initialValues);
			break;
		case MEDIA:
			rowID = problemDB.insert(MEDIA_TABLE, "media", _initialValues);
			break;
		default:
			rowID = -1;
		}
			
		// Return a URI to the newly inserted row on success.
		if (rowID > 0) {
			Uri uri = ContentUris.withAppendedId(_uri, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		else if (rowID == 0)
			throw new SQLException("URI match failed for : " + _uri);
		
		throw new SQLException("Failed to insert row into " + _uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		int count;
		String segment;
		
		switch (uriMatcher.match(uri)) {
		
		case PROBLEMS:
			count = problemDB.update(PROBLEM_TABLE, values, where, whereArgs);
			break;

		case PROBLEM_ID:
			segment = uri.getPathSegments().get(1);
			count = problemDB.update(PROBLEM_TABLE, values, KEY_PROBLEM_ID + "=" + segment
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		case AREAS:
			count = problemDB.update(AREA_TABLE, values, where, whereArgs);
			break;

		case AREA_ID:
			segment = uri.getPathSegments().get(1);
			count = problemDB.update(AREA_TABLE, values, KEY_AREA_ID + "=" + segment
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		case MEDIA:
			count = problemDB.update(MEDIA_TABLE, values, where, whereArgs);
			break;

		case MEDIA_ID:
			segment = uri.getPathSegments().get(1);
			count = problemDB.update(MEDIA_TABLE, values, KEY_MEDIA_ID + "=" + segment
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;
		String segment;
		switch (uriMatcher.match(uri)) {
		case PROBLEMS:
			count = problemDB.delete(PROBLEM_TABLE, where, whereArgs);
			break;

		case PROBLEM_ID:
			segment = uri.getPathSegments().get(1);
			count = problemDB.delete(PROBLEM_TABLE, KEY_PROBLEM_ID + "=" + segment
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		case AREAS:
			count = problemDB.delete(AREA_TABLE, where, whereArgs);
			break;

		case AREA_ID:
			segment = uri.getPathSegments().get(1);
			count = problemDB.delete(PROBLEM_TABLE, KEY_PROBLEM_ID + "=" + segment
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		case MEDIA:
			count = problemDB.delete(MEDIA_TABLE, where, whereArgs);
			break;

		case MEDIA_ID:
			segment = uri.getPathSegments().get(1);
			count = problemDB.delete(MEDIA_TABLE, KEY_MEDIA_ID + "=" + segment
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy = sort;
		Cursor c;
		// If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) {
		case PROBLEM_ID:
			qb.appendWhere(KEY_PROBLEM_ID + "=" + uri.getPathSegments().get(1));
			qb.setTables(PROBLEM_TABLE);
			break;	
		case PROBLEMS:
			qb.setTables(PROBLEM_TABLE);
			if (TextUtils.isEmpty(sort)) 
				orderBy = KEY_PROBLEM_NAME;
			break;
		case AREA_ID:
			qb.appendWhere(KEY_AREA_ID + "=" + uri.getPathSegments().get(1));
			qb.setTables(AREA_TABLE);
			break;
		case AREAS:
			qb.setTables(AREA_TABLE);
			if (TextUtils.isEmpty(sort)) 
				orderBy = KEY_AREA_NAME;
			break;
		case MEDIA_ID:
			qb.appendWhere(KEY_MEDIA_ID + "=" + uri.getPathSegments().get(1));
			qb.setTables(MEDIA_TABLE);
			break;
		case MEDIA:
			qb.setTables(MEDIA_TABLE);
			if (TextUtils.isEmpty(sort)) 
				orderBy = KEY_MEDIA_TYPE;
			break;
		case PROBLEM_COUNT:
			if(selection != null && selection.length() > 0)
				return problemDB.rawQuery("SELECT count(*) FROM " + PROBLEM_TABLE + " where " + selection, selectionArgs);
			else
				return problemDB.rawQuery("SELECT count(*) FROM " + PROBLEM_TABLE, null);
		case AREA_COUNT:
			if(selection != null && selection.length() > 0)
				return problemDB.rawQuery("SELECT count(*) FROM " + AREA_TABLE + " where " + selection, selectionArgs);
			else
				return problemDB.rawQuery("SELECT count(*) FROM " + AREA_TABLE, null);
		case MEDIA_COUNT:
			if(selection != null && selection.length() > 0)
				return problemDB.rawQuery("SELECT count(*) FROM " + MEDIA_TABLE + " where " + selection, selectionArgs);
			else
				return problemDB.rawQuery("SELECT count(*) FROM " + MEDIA_TABLE, null);
		default:
			break;
		}

		c = qb.query(problemDB, projection, selection, selectionArgs, null, null, orderBy);
		
		// Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
		c.setNotificationUri(getContext().getContentResolver(), uri);

		// Return a cursor to the query result.
		return c;
	}

	/** The underlying database */
	private SQLiteDatabase problemDB;

	private static final String TAG = "BetaProvider";
	private static final String DATABASE_NAME = "betterbeta.db";
	private static final int DATABASE_VERSION = 2;
	private static final String PROBLEM_TABLE = "problems";
	private static final String AREA_TABLE = "areas";
	private static final String MEDIA_TABLE = "media";

	//Problem Column Names
	public static final String KEY_PROBLEM_ID = "_id";
	public static final String KEY_PROBLEM_NAME = "name";
	public static final String KEY_PROBLEM_DATE_ADDED = "date_added";
	public static final String KEY_PROBLEM_DATE_MODIFIED = "date_modified";
	public static final String KEY_PROBLEM_DETAILS = "details";
	public static final String KEY_PROBLEM_LATITUDE = "latitude";
	public static final String KEY_PROBLEM_LONGITUDE = "longitude";
	public static final String KEY_PROBLEM_RATING = "rating";
	public static final String KEY_PROBLEM_AREA = "area_id";
	public static final String KEY_PROBLEM_MASTER = "master_id";
	public static final String KEY_PROBLEM_ID_TYPE = "id_type";
	public static final String KEY_PROBLEM_STATE = "state";
	public static final String KEY_PROBLEM_PERMISSION = "permission";

	//Problem Column indexes

	public static final int ID_PROBLEM_COLUMN = 0;
	public static final int NAME_PROBLEM_COLUMN = 1;
	public static final int DATE_ADDED_PROBLEM_COLUMN = 2;
	public static final int DATE_MODIFIED_PROBLEM_COLUMN = 3;
	public static final int DETAILS_PROBLEM_COLUMN = 4;
	public static final int LONGITUDE_PROBLEM_COLUMN = 5;
	public static final int LATITUDE_PROBLEM_COLUMN = 6;
	public static final int RATING_PROBLEM_COLUMN = 7;
	public static final int AREA_PROBLEM_COLUMN = 8;
	public static final int MASTER_PROBLEM_COLUMN = 9;
	public static final int ID_TYPE_PROBLEM_COLUMN = 10;
	public static final int STATE_PROBLEM_COLUMN = 11;
	public static final int PERMISSION_PROBLEM_COLUMN = 12;

	//Area Column Names (some, like _id, name, longitude, latitude will be same

	public static final String KEY_AREA_ID = "_id";
	public static final String KEY_AREA_NAME = "name";
	public static final String KEY_AREA_DATE_ADDED = "date_added";
	public static final String KEY_AREA_DATE_MODIFIED = "date_modified";
	public static final String KEY_AREA_DETAILS = "details";
	public static final String KEY_AREA_LATITUDE = "latitude";
	public static final String KEY_AREA_LONGITUDE = "longitude";
	public static final String KEY_AREA_PARENT = "parent_id";
	public static final String KEY_AREA_MASTER = "master_id";
	public static final String KEY_AREA_ID_TYPE = "id_type";
	public static final String KEY_AREA_STATE = "state";
	public static final String KEY_AREA_PERMISSION = "permission";
	
	//Area Column indexes

	public static final int ID_AREA_COLUMN = 0;
	public static final int NAME_AREA_COLUMN = 1;
	public static final int DATE_ADDED_AREA_COLUMN = 2;
	public static final int DATE_MODIFIED_AREA_COLUMN = 3;
	public static final int DETAILS_AREA_COLUMN = 4;
	public static final int LONGITUDE_AREA_COLUMN = 5;
	public static final int LATITUDE_AREA_COLUMN = 6;
	public static final int PARENT_AREA_COLUMN = 7;
	public static final int MASTER_AREA_COLUMN = 8;
	public static final int ID_TYPE_AREA_COLUMN = 9;
	public static final int STATE_AREA_COLUMN = 10;
	public static final int PERMISSION_AREA_COLUMN = 11;
	
	//Media Column Names
	
	public static final String KEY_MEDIA_ID = "_id";
	public static final String KEY_MEDIA_NAME = "name";
	public static final String KEY_MEDIA_DETAILS = "details";
	public static final String KEY_MEDIA_TYPE = "type";
	public static final String KEY_MEDIA_PATH = "path";
	public static final String KEY_MEDIA_AREA = "area_id";
	public static final String KEY_MEDIA_PROBLEM = "problem_id";
	public static final String KEY_MEDIA_DATE_ADDED = "date_added";
	public static final String KEY_MEDIA_DATE_MODIFIED = "date_modified";
	public static final String KEY_MEDIA_LATITUDE = "latitude";
	public static final String KEY_MEDIA_LONGITUDE = "longitude";
	public static final String KEY_MEDIA_MASTER = "master_id";
	public static final String KEY_MEDIA_ID_TYPE = "id_type";
	public static final String KEY_MEDIA_STATE = "state";
	public static final String KEY_MEDIA_PERMISSION = "permission";
	
	//Media Column Indexes

	public static final int ID_MEDIA_COLUMN = 0;
	public static final int AREA_MEDIA_COLUMN = 1;
	public static final int PROBLEM_MEDIA_COLUMN = 2;
	public static final int DETAILS_MEDIA_COLUMN = 3;
	public static final int TYPE_MEDIA_COLUMN = 4;
	public static final int PATH_MEDIA_COLUMN = 5;
	public static final int DATE_ADDED_MEDIA_COLUMN = 6;
	public static final int DATE_MODIFIED_MEDIA_COLUMN = 7;
	public static final int LONGITUDE_MEDIA_COLUMN = 8;
	public static final int LATITUDE_MEDIA_COLUMN = 9;
	public static final int MASTER_MEDIA_COLUMN = 10;
	public static final int ID_TYPE_MEDIA_COLUMN = 11;
	public static final int STATE_MEDIA_COLUMN = 12;
	public static final int PERMISSION_MEDIA_COLUMN = 13;
	
	// Helper class for opening, creating, and managing database version control
	private static class problemDatabaseHelper extends SQLiteOpenHelper {
		private static final String PROBLEMS_CREATE = "create table " + PROBLEM_TABLE + 
								   " (" + KEY_PROBLEM_ID + " integer primary key autoincrement, " 
										+ KEY_PROBLEM_NAME + " TEXT, " 
										+ KEY_PROBLEM_DATE_ADDED + " DATE, "
										+ KEY_PROBLEM_DATE_MODIFIED + " DATE, "
										+ KEY_PROBLEM_DETAILS + " TEXT, " 
										+ KEY_PROBLEM_LATITUDE + " TEXT, " 
										+ KEY_PROBLEM_LONGITUDE + " TEXT, " 
										+ KEY_PROBLEM_RATING + " FLOAT, "
										+ KEY_PROBLEM_AREA + " LONG, "
										+ KEY_PROBLEM_MASTER + " LONG, "
										+ KEY_PROBLEM_ID_TYPE + " INTEGER, "
										+ KEY_PROBLEM_STATE + " INTEGER, "
										+ KEY_PROBLEM_PERMISSION + " INTEGER);";

		private static final String AREAS_CREATE = "create table " + AREA_TABLE + 
								   " (" + KEY_AREA_ID + " integer primary key autoincrement, " 
										+ KEY_AREA_NAME + " TEXT, " 
										+ KEY_AREA_DATE_ADDED + " DATE, "
										+ KEY_AREA_DATE_MODIFIED + " DATE, "
										+ KEY_AREA_DETAILS + " TEXT, " 
										+ KEY_AREA_LATITUDE + " TEXT, " 
										+ KEY_AREA_LONGITUDE + " TEXT, " 
										+ KEY_AREA_PARENT + " LONG, " 
										+ KEY_AREA_MASTER + " LONG, " 
										+ KEY_AREA_ID_TYPE + " INTEGER, " 
										+ KEY_AREA_STATE + " INTEGER, " 
										+ KEY_AREA_PERMISSION + " INTEGER);";

		private static final String MEDIA_CREATE = "create table " + MEDIA_TABLE + 
								   " (" + KEY_MEDIA_ID + " integer primary key autoincrement, " 
										+ KEY_MEDIA_NAME + " TEXT, " 
										+ KEY_MEDIA_DETAILS + " TEXT, " 
										+ KEY_MEDIA_TYPE + " INTEGER, " 
										+ KEY_MEDIA_PATH + " TEXT, "
										+ KEY_MEDIA_AREA + " LONG, " 
										+ KEY_MEDIA_PROBLEM + " LONG, "
										+ KEY_MEDIA_DATE_ADDED + " DATE, "
										+ KEY_MEDIA_DATE_MODIFIED + " DATE, "
										+ KEY_MEDIA_LATITUDE + " TEXT, " 
										+ KEY_MEDIA_LONGITUDE + " TEXT, " 
										+ KEY_MEDIA_MASTER + " LONG, " 
										+ KEY_MEDIA_ID_TYPE + " INTEGER, " 
										+ KEY_MEDIA_STATE + " INTEGER, " 
										+ KEY_MEDIA_PERMISSION + " INTEGER);";
		
		
		/** Helper class for managing the betterbeta database */
		public problemDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(PROBLEMS_CREATE);
			db.execSQL(AREAS_CREATE);
			db.execSQL(MEDIA_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS " + PROBLEM_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + AREA_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + MEDIA_TABLE);
			onCreate(db);
		}
		
		public void dumpDb(SQLiteDatabase db){
			
			Log.d("BetaProvider", "dumping DB");
			String newLine = System.getProperty("line.separator");
			String queryString;
			try {
			    File root = Environment.getExternalStorageDirectory();
			    if (root.canWrite()){
			    	File  betterBetaDirectory = new File(root, "betterbeta/database");
			    	betterBetaDirectory.mkdirs();
			        File database = new File(betterBetaDirectory, "database.sql");
			        FileWriter dbWriter = new FileWriter(database);
			        BufferedWriter out = new BufferedWriter(dbWriter);
			        out.write(PROBLEMS_CREATE + newLine);
			        out.write(AREAS_CREATE + newLine);
			        out.write(MEDIA_CREATE + newLine);
			       
			        Cursor c = db.query(PROBLEM_TABLE, null, null, null, null, null, null);     
			        while (c.moveToNext()){
			        	queryString = "INSERT INTO " + PROBLEM_TABLE + " (" +
											KEY_PROBLEM_ID + "," + 
											KEY_PROBLEM_NAME + "," + 
											KEY_PROBLEM_DATE_ADDED + "," + 
											KEY_PROBLEM_DATE_MODIFIED + "," + 
											KEY_PROBLEM_DETAILS + "," + 
											KEY_PROBLEM_LATITUDE + "," + 
											KEY_PROBLEM_LONGITUDE + "," + 
											KEY_PROBLEM_RATING + "," + 
											KEY_PROBLEM_AREA + "," + 
											KEY_PROBLEM_MASTER + "," + 
											KEY_PROBLEM_ID_TYPE + "," + 
											KEY_PROBLEM_STATE + "," + 
											KEY_PROBLEM_PERMISSION +
			        						") VALUES (" +
											c.getLong(0)+ ","+
											"\"" + c.getString(1)+ "\"" + ","+
											"\"" + c.getString(2)+ "\"" + ","+
											"\"" + c.getString(3)+ "\"" + ","+
											"\"" + c.getString(4)+ "\"" + ","+
											"\"" + c.getString(5)+ "\"" + ","+
											"\"" + c.getString(6)+ "\"" + ","+
											c.getFloat(7)+ ","+
											c.getLong(8)+ ", " +
											c.getLong(9)+ ","+
											c.getInt(10)+ ","+
											c.getInt(11)+ ","+
											c.getInt(12)+ 
			        						");" + newLine ;
			        	out.write(queryString);

			        }
					c.close();
					
					 c = db.query(AREA_TABLE, null, null, null, null, null, null);     
				        while (c.moveToNext()){
				        	queryString = "INSERT INTO " + AREA_TABLE + " (" +
												KEY_AREA_ID + "," + 
												KEY_AREA_NAME + "," + 
												KEY_AREA_DATE_ADDED + "," + 
												KEY_AREA_DATE_MODIFIED + "," + 
												KEY_AREA_DETAILS + "," + 
												KEY_AREA_LATITUDE + "," + 
												KEY_AREA_LONGITUDE + "," + 
												KEY_AREA_PARENT  + "," + 
												KEY_AREA_MASTER + "," + 
												KEY_AREA_ID_TYPE + "," + 
												KEY_AREA_STATE+ "," + 
												KEY_AREA_PERMISSION +
				        						") VALUES (" +
												c.getLong(0)+ ","+
												"\"" + c.getString(1)+"\"" +  ","+
												"\"" + c.getString(2)+ "\"" + ","+
												"\"" + c.getString(3)+ "\"" + ","+
												"\"" + c.getString(4)+ "\"" + ","+
												"\"" + c.getString(5)+ "\"" + ","+
												"\"" + c.getString(6)+ "\"" + ","+
												c.getLong(7)+ ", " +
												c.getLong(8)+ ","+
												c.getInt(9)+ ","+
												c.getInt(10)+ ","+
												c.getInt(11)+ 
				        						");" + newLine ;
				        	out.write(queryString);
				        }
						c.close();
						
						c = db.query(MEDIA_TABLE, null, null, null, null, null, null);     
					        while (c.moveToNext()){
					        	queryString = "INSERT INTO " + MEDIA_TABLE + " (" +
					        						KEY_MEDIA_ID + "," + 
					        						KEY_MEDIA_NAME + "," + 
					        						KEY_MEDIA_DETAILS + "," + 
					        						KEY_MEDIA_TYPE + "," + 
					        						KEY_MEDIA_PATH + "," + 
					        						KEY_MEDIA_AREA + "," + 
					        						KEY_MEDIA_PROBLEM + "," + 
					        						KEY_MEDIA_DATE_ADDED + "," + 
					        						KEY_MEDIA_DATE_MODIFIED + "," + 
					        						KEY_MEDIA_LATITUDE + "," + 
					        						KEY_MEDIA_LONGITUDE + "," + 
					        						KEY_MEDIA_MASTER  + "," + 
					        						KEY_MEDIA_ID_TYPE  + "," + 
					        						KEY_MEDIA_STATE  + "," + 
					        						KEY_MEDIA_PERMISSION  +
					        						") VALUES (" +
													c.getLong(0)+ ","+
													"\"" + c.getString(1)+ "\"" + ","+
													"\"" + c.getString(2)+ "\"" + ","+
													c.getInt(3)+ ","+
													"\"" + c.getString(4)+ "\"" + ","+
													c.getLong(5)+ ","+
													c.getLong(6)+ ","+
													"\"" + c.getString(7)+ "\"" + ","+
													"\"" + c.getString(8)+ "\"" + ","+
													"\"" + c.getString(9)+ "\"" + ","+
													"\"" + c.getString(10)+ "\"" + ","+
													c.getLong(11)+ ","+
													c.getInt(12)+ ","+
													c.getInt(13)+ ","+
													c.getInt(14)+ 
					        						");" + newLine;
					        	out.write(queryString);
					        }
							c.close();
			        out.close();
			    }
			} catch (IOException e) {
			    Log.e(TAG, "Could not write file " + e.getMessage());
			}
					
		}
	}
	
	public static final String MYSQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String SQLITE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	public static SimpleDateFormat mysqlDateFormater = new SimpleDateFormat(MYSQL_DATE_FORMAT); 
	public static SimpleDateFormat sqliteDateFormater = new SimpleDateFormat(SQLITE_DATE_FORMAT); 
}
