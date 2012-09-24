package info.betterbeta.problem;

import info.betterbeta.model.IdType;
import info.betterbeta.model.Problem;
import info.betterbeta.provider.BetaProvider;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ProblemCountViewBinder implements SimpleCursorAdapter.ViewBinder
{
	Context context;
	
    public ProblemCountViewBinder(Context context) {
    	this.context = context;   	
    }

	@Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex){    
      String areaId = String.valueOf(cursor.getLong(1));
      String masterId = String.valueOf(cursor.getLong(2));
      Cursor c;
      if("1".equals(masterId)) {
    	  c = context.getContentResolver().query(Problem.CONTENT_URI_COUNT, 
					null, 
					null,
					null,
					null);
      }
      else{
      c = context.getContentResolver().query(Problem.CONTENT_URI_COUNT, 
					null, 
					BetaProvider.KEY_PROBLEM_AREA + " = ? AND " +
					BetaProvider.KEY_PROBLEM_ID_TYPE + " = ? OR " +
					BetaProvider.KEY_PROBLEM_AREA + " = ? AND " +
					BetaProvider.KEY_PROBLEM_ID_TYPE + " = ?",
					new String[]{
									areaId, 
									String.valueOf(IdType.LOCAL),
									masterId, 
									String.valueOf(IdType.MASTER)},
					null);
      }
	  c.moveToNext();
	  String count = String.valueOf(c.getLong(0));
	  ((TextView)view).setText(cursor.getString(0) + " (" + count + ")");
	  c.close();
	  return true;
    }
} 
