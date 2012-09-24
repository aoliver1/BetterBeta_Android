package info.betterbeta.problem;

import info.betterbeta.BetterBeta;
import info.betterbeta.ProgressBarListActivity;
import info.betterbeta.R;
import info.betterbeta.maps.MapOverview;
import info.betterbeta.media.MediaCreateEdit;
import info.betterbeta.media.MediaHelper;
import info.betterbeta.media.MediaIconCursorAdapter;
import info.betterbeta.media.SimpleImageViewer;
import info.betterbeta.media.WebViewer;
import info.betterbeta.menu.MenuHelper;
import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Media;
import info.betterbeta.model.Problem;
import info.betterbeta.provider.BetaProvider;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ProblemDetail extends ProgressBarListActivity {
	
	Area area;
	Problem problem;
	Spinner areaSpinner;
	MediaIconCursorAdapter mediaAdapter;
	AlertDialog mediaDeleteDialog;
	AlertDialog problemDeleteDialog;
	
	long problemId = -1;
	long areaId = -1;
	long selectedId;
	int idType = -1;
	boolean mediaLongClicked = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.problem_view);

	    problemId = savedInstanceState != null ? savedInstanceState.getLong("problem_id") : -1;
	    areaId = savedInstanceState != null ? savedInstanceState.getLong("area_id") : -1;
	    idType = savedInstanceState != null ? savedInstanceState.getInt("id_type") : -1;
	    
	    mediaDeleteDialog = new AlertDialog.Builder(ProblemDetail.this)
	       .setTitle(R.string.are_you_sure)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	Intent i = new Intent(ProblemDetail.this, MediaCreateEdit.class);
	    			i.putExtra("delete", true);
	    			if (getSelectedItemId() > 0)
	    				i.putExtra("media_id", getSelectedItemId());
	    			else
	    				i.putExtra("media_id", selectedId);
	    			
	    			startActivity(i);

	    			finish();
	            }
	        })
	        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                /* User clicked Cancel do NOTHING */
	            }
	        })
	        .create();
	    
	    problemDeleteDialog = new AlertDialog.Builder(ProblemDetail.this)
	       .setTitle(R.string.are_you_sure)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	Intent i = new Intent(ProblemDetail.this, ProblemCreateEdit.class);
	    			i.putExtra("delete", true);
	    			if (getSelectedItemId() > 0)
	    				i.putExtra("problem_id", getSelectedItemId());
	    			else
	    				i.putExtra("problem_id", selectedId);
	    			
	    			startActivity(i);

	    			finish();
	            }
	        })
	        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                /* User clicked Cancel do NOTHING */
	            }
	        })
	        .create();
	    
	    
	    Bundle extras = getIntent().getExtras();

	    if (problemId < 0) 
			problemId = extras != null ? extras.getLong("problem_id") : null;
			
		if (idType <= 0 ) 
			idType = extras != null ? extras.getInt("id_type") : null;
			
		if (problemId > 0){
	
			if (idType == IdType.LOCAL)
				problem = ProblemHelper.inflate(problemId, getContentResolver(), IdType.LOCAL);
			else
				problem = ProblemHelper.inflate(problemId, getContentResolver(), IdType.MASTER);
	
			if (problem == null)
				finish(); // possible issue with syncing, return to prev activity if cannot inflate
			
			String where;
			
			if (problem.getIdType() == IdType.LOCAL)
				where = BetaProvider.KEY_AREA_ID + " = ?";
			else
				where =  BetaProvider.KEY_AREA_MASTER + " = ?";
			
			area = new Area();
			Cursor areaCursor = getContentResolver().query(Area.CONTENT_URI,
					new String[] { BetaProvider.KEY_AREA_NAME, BetaProvider.KEY_AREA_ID}, 
					where,
					new String[]{String.valueOf(problem.getArea())},
					null);
			
			if (areaCursor.moveToFirst()){
				area.setName(areaCursor.getString(0));
			}
			
			areaCursor.close();
			
			TextView nameTextView = (TextView) findViewById(R.id.problem_name_view);
			TextView detailsTextView = (TextView) findViewById(R.id.problem_details_view);
			TextView areaTextView = (TextView) findViewById(R.id.problem_area_view);
			
			nameTextView.setText("Name: " + problem.getName());
			detailsTextView.setText("Details: " + problem.getDetails());
			areaTextView.setText("Area: " + area.getName());
			
			// SELECT ... from media where problem_id = problem.getId() AND media.id_type == LOCAL OR id = MasterId and IdType = MASTER
			Cursor mediaCursor = managedQuery(Media.CONTENT_URI,
					new String[] { BetaProvider.KEY_MEDIA_NAME, BetaProvider.KEY_MEDIA_TYPE, BetaProvider.KEY_MEDIA_ID}, 
								BetaProvider.KEY_MEDIA_PROBLEM + " = ? AND " + 
								BetaProvider.KEY_MEDIA_ID_TYPE + " = ? OR " + 
								BetaProvider.KEY_MEDIA_PROBLEM + " = ? AND " +
								BetaProvider.KEY_MEDIA_ID_TYPE + " = ?",
					new String[]{String.valueOf(problem.getId()), 
									String.valueOf(IdType.LOCAL),
									String.valueOf(problem.getMasterId()), 
									String.valueOf(IdType.MASTER)},
					null);
			
			startManagingCursor(mediaCursor);
		
			//((ImageView)findViewById(R.id.problem_image_view)).setImageResource(R.drawable.camera_icon);
			String[] from = new String[] { BetaProvider.KEY_MEDIA_NAME, BetaProvider.KEY_MEDIA_TYPE, BetaProvider.KEY_MEDIA_TYPE};
			int[] to = new int[] { R.id.icon_list_text, R.id.icon_1, R.id.icon_2};
		
			mediaAdapter = new MediaIconCursorAdapter(this, R.layout.icons_list_item, mediaCursor, from, to);
			
			this.getListView().setOnItemClickListener( new OnItemClickListener(){
			
			//short click == view
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Media media = MediaHelper.inflate(id, getContentResolver(), IdType.LOCAL);
				switch(media.getType()){
				case Media.TYPE_URL_PIC:
					startActivity(new Intent(ProblemDetail.this, WebViewer.class).putExtra("url", media.getPath()));
					break;
				case Media.TYPE_URI_PIC:
					startActivity(new Intent(ProblemDetail.this, SimpleImageViewer.class).putExtra("uri", media.getPath()));
					break;
				case Media.TYPE_URL_YOUTUBE:
					Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(media.getPath()));
					startActivity(intent);	
					break;
				}
			}});
			
			this.getListView().setOnItemLongClickListener(new OnItemLongClickListener (){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				ProblemDetail.this.selectedId = id;
				mediaLongClicked = true;
				ProblemDetail.this.openOptionsMenu();
				return false;
			}});
			
			this.setListAdapter(mediaAdapter);

		}
		else
			finish();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(mediaLongClicked)
			MenuHelper.createProblemDetailMenu(menu, isSyncing, selectedId, getContentResolver(), mediaLongClicked);
		else
			MenuHelper.createProblemDetailMenu(menu, isSyncing, problemId, getContentResolver(), mediaLongClicked);
		return super.onCreateOptionsMenu(menu);	
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent i;
		switch (item.getItemId()) {
		case MenuHelper.EDIT:
			i = new Intent(this, ProblemCreateEdit.class);
			i.putExtra("problem_id", problemId);
			startActivity(i);
			return true;
		case MenuHelper.MAP:
			i = new Intent(this, MapOverview.class);
			i.putExtra("problem_id", problemId);
			i.putExtra("action", MapOverview.SHOW_PROBLEM);
			startActivity(i);
			return true;
		case MenuHelper.MAIN_MENU:
			startActivity(new Intent(this, BetterBeta.class));
			return true;

		case MenuHelper.DELETE:
			problemDeleteDialog.show();
			return true;
		case MenuHelper.ADD_MEDIA:
			i = new Intent(this, MediaCreateEdit.class);
			if(problem.getMasterId() > 0){
				i.putExtra("problem_id", problem.getMasterId());
				i.putExtra("id_type", IdType.MASTER);
			}
			else{
				i.putExtra("problem_id", problem.getId());
				i.putExtra("id_type", IdType.LOCAL);
			}
				
			startActivity(i);
			return true;
			
			// media is selected for these options:
			
		case MenuHelper.EDIT_MEDIA:
			i = new Intent(this, MediaCreateEdit.class);
			if (getSelectedItemId() > 0)
				i.putExtra("media_id", getSelectedItemId());
			else
				i.putExtra("media_id", selectedId);
			startActivity(i);
			break;
		case MenuHelper.VIEW_MEDIA:
			Media media = MediaHelper.inflate(selectedId, getContentResolver(), IdType.LOCAL);
			switch(media.getType()){
			case Media.TYPE_URL_PIC:
				startActivity(new Intent(ProblemDetail.this, WebViewer.class).putExtra("url", media.getPath()));
				break;
			case Media.TYPE_URI_PIC:
				startActivity(new Intent(ProblemDetail.this, SimpleImageViewer.class).putExtra("uri", media.getPath()));
				break;
			case Media.TYPE_URL_YOUTUBE:
				Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(media.getPath()));
				startActivity(intent);	
				break;
			}
			break;
		case MenuHelper.DELETE_MEDIA:
			mediaDeleteDialog.show();
			break;

		}
	
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putLong("problem_id", problemId);
	}

	@Override
	protected void onResume() {
		mediaLongClicked = false;
		this.getListView().invalidate();
		super.onResume();
	}
	
	@Override
	public void syncComplete() {
		// TODO Auto-generated method stub
		
	}
	
}


