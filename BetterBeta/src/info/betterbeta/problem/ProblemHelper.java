package info.betterbeta.problem;

import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Problem;
import info.betterbeta.model.State;
import info.betterbeta.provider.BetaProvider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

public class ProblemHelper {

	public static boolean addNewProblem(Problem problem, ContentResolver cr) {

		String w = BetaProvider.KEY_PROBLEM_NAME + " = ? and " + BetaProvider.KEY_AREA_ID + " = ?";

		Cursor c = cr.query(Problem.CONTENT_URI, null, w, new String[] { problem.getName(),
				String.valueOf(problem.getArea()) }, null);

		int dbCount;
		if (c != null)
			dbCount = c.getCount();
		else
			dbCount = 0;

		c.close();
		
		if (dbCount == 0) {

			ContentValues values = new ContentValues();

			if (problem.getMasterId() > 0){ // coming from web server, put ID and CLEAN state
				values.put(BetaProvider.KEY_PROBLEM_STATE, State.CLEAN);
			}
			else{  // coming from user, mark as NEW for future sync
				values.put(BetaProvider.KEY_PROBLEM_STATE, State.NEW);
			}	
			values.put(BetaProvider.KEY_PROBLEM_ID_TYPE, problem.getIdType());
			values.put(BetaProvider.KEY_PROBLEM_NAME, problem.getName());
			if(null != problem.getDateAdded())
				values.put(BetaProvider.KEY_PROBLEM_DATE_ADDED, BetaProvider.sqliteDateFormater.format(problem.getDateAdded()));
			else
				values.put(BetaProvider.KEY_PROBLEM_DATE_ADDED, BetaProvider.sqliteDateFormater.format(Calendar.getInstance().getTime()));
			if(null != problem.getDateModified())
				values.put(BetaProvider.KEY_PROBLEM_DATE_MODIFIED, BetaProvider.sqliteDateFormater.format(problem.getDateModified()));
			else
				values.put(BetaProvider.KEY_PROBLEM_DATE_MODIFIED, BetaProvider.sqliteDateFormater.format(Calendar.getInstance().getTime()));
			values.put(BetaProvider.KEY_PROBLEM_MASTER, problem.getMasterId());	
			values.put(BetaProvider.KEY_PROBLEM_DETAILS, problem.getDetails());
			values.put(BetaProvider.KEY_PROBLEM_LONGITUDE, problem.getLongitude());
			values.put(BetaProvider.KEY_PROBLEM_LATITUDE, problem.getLatitude());
			values.put(BetaProvider.KEY_PROBLEM_AREA, problem.getArea());
			values.put(BetaProvider.KEY_PROBLEM_PERMISSION, problem.getPermission());

			// values.put(BetaProvider.KEY_PROBLEM_RATING, problem.getRating());

			cr.insert(Problem.CONTENT_URI, values);
			c.close();
		}
		return true;
	}

	
	public static boolean updateProblem(Problem problem, ContentResolver contentResolver){
		
		ContentValues values = new ContentValues();
		values.put(BetaProvider.KEY_PROBLEM_NAME, problem.getName());
		values.put(BetaProvider.KEY_PROBLEM_DETAILS, problem.getDetails());
		values.put(BetaProvider.KEY_PROBLEM_DATE_MODIFIED, BetaProvider.sqliteDateFormater.format(Calendar.getInstance().getTime()));
		values.put(BetaProvider.KEY_PROBLEM_LONGITUDE, problem.getLongitude());
		values.put(BetaProvider.KEY_PROBLEM_LATITUDE, problem.getLatitude());
		values.put(BetaProvider.KEY_PROBLEM_AREA, problem.getArea());
		values.put(BetaProvider.KEY_PROBLEM_MASTER, problem.getMasterId());
		values.put(BetaProvider.KEY_PROBLEM_STATE, problem.getState());
		values.put(BetaProvider.KEY_PROBLEM_ID_TYPE, problem.getIdType());
		values.put(BetaProvider.KEY_PROBLEM_PERMISSION, problem.getPermission());
		
		String where = BetaProvider.KEY_PROBLEM_ID + " = ?";
		contentResolver.update(Problem.CONTENT_URI, values, where, new String[]{String.valueOf(problem.getId())});
		
		return true;
	}
	
	public static ArrayList<Problem> getNewAndDirtyProblems(ContentResolver contentResolver) {

		Cursor c = contentResolver.query(Problem.CONTENT_URI, 
						new String[]{BetaProvider.KEY_PROBLEM_ID},
						BetaProvider.KEY_PROBLEM_STATE + " = ? or " + 
						BetaProvider.KEY_PROBLEM_STATE + " = ?",
						new String[]{String.valueOf(State.NEW), 
										String.valueOf(State.DIRTY)}, null);
		
		ArrayList<Problem> problems = new ArrayList<Problem>(c.getColumnCount());
		Problem problem;
		while (c.moveToNext()){
			problem = ProblemHelper.inflate(c.getLong(0), contentResolver, IdType.LOCAL);
			problems.add(problem);
		}
		c.close();
		return problems;
	}

	public static Problem inflate(long problemId, ContentResolver cr, int idType){
		Problem problem = null;	
		String whereClause;
		if(idType == IdType.LOCAL)
			whereClause = BetaProvider.KEY_PROBLEM_ID + "= ?";
		else
			whereClause = BetaProvider.KEY_PROBLEM_MASTER + "= ?";
			
		Cursor c = cr.query(Problem.CONTENT_URI,  new String[] { 		
									BetaProvider.KEY_PROBLEM_ID,
									BetaProvider.KEY_PROBLEM_NAME,
									BetaProvider.KEY_PROBLEM_DATE_ADDED,
									BetaProvider.KEY_PROBLEM_DATE_MODIFIED,
									BetaProvider.KEY_PROBLEM_DETAILS,
									BetaProvider.KEY_PROBLEM_LONGITUDE, 
									BetaProvider.KEY_PROBLEM_LATITUDE , 
									BetaProvider.KEY_PROBLEM_AREA , 
									BetaProvider.KEY_PROBLEM_MASTER , 
									BetaProvider.KEY_PROBLEM_ID_TYPE , 
									BetaProvider.KEY_PROBLEM_STATE , 
									BetaProvider.KEY_PROBLEM_PERMISSION }, 
									whereClause, 
									new String[] {String.valueOf(problemId)}, 
									null);

		if(c.getCount()==1){
			problem = new Problem();
			c.moveToFirst();
			problem.setId(problemId);
			problem.setName(c.getString(1));
			try {
				problem.setDateAdded(BetaProvider.sqliteDateFormater.parse(c.getString(2)));
				problem.setDateModified(BetaProvider.sqliteDateFormater.parse(c.getString(3)));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			problem.setDetails(c.getString(4));
			problem.setLongitude(c.getString(5));
			problem.setLatitude(c.getString(6));		
			problem.setArea(c.getLong(7));		
			problem.setMasterId(c.getLong(8));		
			problem.setIdType(c.getInt(9));			
			problem.setState(c.getInt(10));			
			problem.setPermission(c.getInt(11));	
		}
		c.close();
		return problem;
	}

	
	public static Problem inflateLocation(long problemId, ContentResolver cr){
		Problem problem = null;	
		String whereClause = BetaProvider.KEY_PROBLEM_ID + "= ?";
		Cursor c = cr.query(Problem.CONTENT_URI,  new String[] { 		
								BetaProvider.KEY_PROBLEM_LONGITUDE, 
								BetaProvider.KEY_PROBLEM_LATITUDE }, 
								whereClause, 
								new String[] {String.valueOf(problemId)}, 
								null);

		if(c.getCount()==1){
			problem = new Problem();
			c.moveToFirst();
			problem.setId(problemId);
			problem.setLongitude(c.getString(0));
			problem.setLatitude(c.getString(1));
		}
		
		c.close();
		return problem;
	}

	public static Problem inflateName(long problemId, ContentResolver cr){
		Problem problem = null;	
		String whereClause = BetaProvider.KEY_PROBLEM_ID + "= ?";
		Cursor c = cr.query(Problem.CONTENT_URI,  new String[] { 		
								BetaProvider.KEY_PROBLEM_NAME}, 
								whereClause, 
								new String[] {String.valueOf(problemId)}, 
								null);

		if(c.getCount()==1){
			problem = new Problem();
			c.moveToFirst();
			problem.setId(problemId);
			problem.setName(c.getString(0));
		}
		
		c.close();
		return problem;
	}
	
	public static Problem inflatePermission(long problemId, ContentResolver cr, int idType){
		Problem problem = null;
		String whereClause;
		
		if(idType == IdType.LOCAL)
			whereClause = BetaProvider.KEY_PROBLEM_ID + "= ?";
		else
			whereClause = BetaProvider.KEY_PROBLEM_MASTER + "= ?";
	
		Cursor c = cr.query(Problem.CONTENT_URI,  new String[] { 		
															BetaProvider.KEY_PROBLEM_PERMISSION}, 
															whereClause, 
															new String[] {String.valueOf(problemId)}, 
															null);

		if(c.getCount()==1){
			problem = new Problem();
			c.moveToFirst();
			problem.setId(problemId);
			problem.setPermission(c.getInt(0));
		}
		c.close();
		return problem;
	}

	


	public static void setMasterFromLocalArea(Problem problem, ContentResolver contentResolver) {

		if (problem.getIdType() == IdType.LOCAL){
			Cursor c = contentResolver.query(Area.CONTENT_URI,
									new String[]{BetaProvider.KEY_AREA_MASTER},
									BetaProvider.KEY_AREA_ID + " = ?", 
									new String[]{String.valueOf(problem.getArea())},
									null);
			if(c.moveToNext()){
				if (c.getLong(0) > 0){
					problem.setArea(c.getLong(0));
					problem.setIdType(IdType.MASTER);
				}
			}
			c.close();
		}
		
	}
}
