package info.betterbeta.media;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class MediaIconCursorAdapter extends SimpleCursorAdapter
{
    public MediaIconCursorAdapter(Context context, int layout, Cursor c,String[] from, int[] to){
        super(context, layout, c, from, to);
        setViewBinder(new MediaDataViewBinder(context));
    }
    
} 

