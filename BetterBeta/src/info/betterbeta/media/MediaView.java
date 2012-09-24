package info.betterbeta.media;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

public class MediaView extends ViewGroup {

	String name;
	int mediaType;
	Long id;
	public MediaView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	

	public MediaView(Context context,String name, int mediaType, Long id) {
		super(context);
		this.name = name;
		this.mediaType = mediaType;
		this.id = id;


	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		TextView nameTextView = new TextView(this.getContext());
		nameTextView.setText(this.name);
		this.addView(nameTextView);
		
	}

}
