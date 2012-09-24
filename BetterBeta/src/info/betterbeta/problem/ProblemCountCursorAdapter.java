package info.betterbeta.problem;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class ProblemCountCursorAdapter extends SimpleCursorAdapter
{
    public ProblemCountCursorAdapter(Context context, int layout, Cursor c,String[] from, int[] to){
        super(context, layout, c, from, to);
        setViewBinder(new ProblemCountViewBinder(context));
    }
    
} 

