package info.betterbeta.media;

import info.betterbeta.R;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Media;
import info.betterbeta.provider.BetaProvider;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class MediaDataViewBinder implements SimpleCursorAdapter.ViewBinder
{
	Context context;
	
    public MediaDataViewBinder(Context context) {
    	this.context = context;   	
    }

	@Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex)
    {
      int mediaTypeIndex = cursor.getColumnIndex(BetaProvider.KEY_MEDIA_TYPE);
      int problemIdIndex = cursor.getColumnIndex(BetaProvider.KEY_PROBLEM_ID);
      int problemMasterIndex = cursor.getColumnIndex(BetaProvider.KEY_PROBLEM_MASTER);
      
      if(mediaTypeIndex==columnIndex) {
			int type = Integer.parseInt(cursor.getString(mediaTypeIndex));
			switch(type){
			case Media.TYPE_URL_PIC: 
			case Media.TYPE_URI_PIC: 
				if (R.id.icon_1 == view.getId()){
					((ImageView)view).setImageResource(R.drawable.camera_icon);
					((ImageView)view).setPadding(2, 8, 3, 0);
				}
				break;
			case Media.TYPE_URL_YOUTUBE: 
				if (R.id.icon_2 == view.getId()){
					((ImageView)view).setImageResource(R.drawable.video_icon);
					((ImageView)view).setPadding(2, 3, 1, 0);
				}
				break;
			}
			return true;
      }
      else if (problemIdIndex == columnIndex || problemMasterIndex == columnIndex){

    	  Cursor c;
    	  if (R.id.icon_1 == view.getId()){
    		  c = context.getContentResolver().query(Media.CONTENT_URI_COUNT, 
							null, 
							"(" + BetaProvider.KEY_MEDIA_TYPE + " = ? OR " + 
							BetaProvider.KEY_MEDIA_TYPE + " = ?) AND ("+
							BetaProvider.KEY_MEDIA_PROBLEM + " = ? AND " + 
							BetaProvider.KEY_MEDIA_ID_TYPE + " = ? OR " + 
							BetaProvider.KEY_MEDIA_PROBLEM + " = ? AND " +
							BetaProvider.KEY_MEDIA_ID_TYPE + " = ?)",
							new String[]{String.valueOf(Media.TYPE_URI_PIC), 
											String.valueOf(Media.TYPE_URL_PIC), 
											String.valueOf(cursor.getLong(problemIdIndex)), 
											String.valueOf(IdType.LOCAL),
											String.valueOf(cursor.getLong(problemMasterIndex)), 
											String.valueOf(IdType.MASTER)},
							null);
    		  c.moveToNext();
				long count = c.getLong(0);;
    	  		if(count > 0){
					((ImageView)view).setImageResource(R.drawable.camera_icon);
					((ImageView)view).setPadding(2, 8, 3, 0);
    	  		}
    	  		else if (((ImageView)view).getWidth() > 0){
    	  			((ImageView)view).setImageDrawable(null);
    				((ImageView)view).setPadding(0, 0, 0, 0);
    	  		}
				c.close();
    	  		
	  		}
	  	  	if (R.id.icon_2 == view.getId()){
		  	  	c = context.getContentResolver().query(Media.CONTENT_URI_COUNT, 
	    	  			null, 
	    	  			BetaProvider.KEY_MEDIA_TYPE + " = ? AND ("+
	    	  			BetaProvider.KEY_MEDIA_PROBLEM + " = ? AND " + 
	    				BetaProvider.KEY_MEDIA_ID_TYPE + " = ? OR " + 
	    				BetaProvider.KEY_MEDIA_PROBLEM + " = ? AND " +
	    				BetaProvider.KEY_MEDIA_ID_TYPE + " = ?)",
	    				new String[]{String.valueOf(Media.TYPE_URL_YOUTUBE), 
		  								String.valueOf(cursor.getLong(problemIdIndex)), 
	    								String.valueOf(IdType.LOCAL),
	    								String.valueOf(cursor.getLong(problemMasterIndex)), 
	    								String.valueOf(IdType.MASTER)},
	    				null);
		  	  		
		  		c.moveToNext();
        	  	long count = c.getLong(0);
    	  		if(count > 0){
					((ImageView)view).setImageResource(R.drawable.video_icon);
					((ImageView)view).setPadding(2, 8, 3, 0);
    	  		}
    	  		else if (((ImageView)view).getWidth() > 0){
    	  			((ImageView)view).setImageDrawable(null);
    				((ImageView)view).setPadding(0, 0, 0, 0);
    	  		}
    	  		c.close();
	  	  	}
    	  	return true;
      }
      return false;
    }
} 
