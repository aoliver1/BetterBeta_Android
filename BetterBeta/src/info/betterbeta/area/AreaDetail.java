 package info.betterbeta.area;

import info.betterbeta.BetterBeta;
import info.betterbeta.ProgressBarListActivity;
import info.betterbeta.R;
import info.betterbeta.maps.MapOverview;
import info.betterbeta.media.MediaCreateEdit;
import info.betterbeta.media.MediaIconCursorAdapter;
import info.betterbeta.menu.MenuHelper;
import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Problem;
import info.betterbeta.problem.ProblemCreateEdit;
import info.betterbeta.problem.ProblemDetail;
import info.betterbeta.provider.BetaProvider;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class AreaDetail extends ProgressBarListActivity {

	Area area;
	Long areaId;
	SimpleCursorAdapter problemAdapter;
	long selectedId;
	AlertDialog areaDeleteDialog;
	AlertDialog problemDeleteDialog;
	String parentAreaName;
	boolean problemLongClicked;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.area_view);
	    areaId = savedInstanceState != null ? savedInstanceState.getLong("area_id") : null;
	    Bundle extras = getIntent().getExtras();
	       
	    problemLongClicked = false;
	    areaDeleteDialog = new AlertDialog.Builder(AreaDetail.this)
	       .setTitle(R.string.are_you_sure)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	    			Intent i = new Intent(AreaDetail.this, AreaCreateEdit.class);
	    			i.putExtra("delete", true);
	    			if (getSelectedItemId() > 0)
	    				i.putExtra("area_id", getSelectedItemId());
	    			else
	    				i.putExtra("area_id", selectedId);
	    			
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
	    
		problemDeleteDialog = new AlertDialog.Builder(AreaDetail.this)
	       .setTitle(R.string.are_you_sure)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	Intent i = new Intent(AreaDetail.this, ProblemCreateEdit.class);
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
		
        //then try the bundle
		if (areaId == null) 
			areaId = extras != null ? extras.getLong("area_id") : null;
		
		//finally, if area, then animate to it
		if (areaId != null){
	
			Area area = AreaHelper.inflate(areaId, getContentResolver(), IdType.LOCAL);
			
			if(area == null)
				finish();  // cannot inflate, possible issue when syncing
			
			if(area.getParent() > 0)
			{
				parentAreaName = "";
				String where;
				
				if (area.getIdType() == IdType.LOCAL)
					where = BetaProvider.KEY_AREA_ID + " = ?";
				else
					where =  BetaProvider.KEY_AREA_MASTER + " = ?";
				
				Cursor areaCursor = getContentResolver().query(Area.CONTENT_URI,
						new String[] { BetaProvider.KEY_AREA_NAME, BetaProvider.KEY_AREA_ID}, 
						where,
						new String[]{String.valueOf(area.getParent())},
						null);
				
				if (areaCursor.moveToNext())
					parentAreaName = areaCursor.getString(0);
				else
					parentAreaName = "None";
				
				areaCursor.close();
			}
			
			TextView nameTextView = (TextView) findViewById(R.id.area_name_view);
			TextView detailsTextView = (TextView) findViewById(R.id.area_details_view);
			TextView parentTextView = (TextView) findViewById(R.id.area_parent_view);
			
			nameTextView.setText("Name: " + area.getName());
			detailsTextView.setText("Details: " + area.getDetails());
			parentTextView.setText("Parent Area: " + parentAreaName);

			
			Cursor problemCursor = managedQuery(Problem.CONTENT_URI, 
									new String[] { BetaProvider.KEY_PROBLEM_NAME, BetaProvider.KEY_PROBLEM_ID, BetaProvider.KEY_PROBLEM_MASTER },
									BetaProvider.KEY_PROBLEM_AREA + " = ? AND " + BetaProvider.KEY_PROBLEM_ID_TYPE + " = " + IdType.LOCAL  + " OR " +
											BetaProvider.KEY_PROBLEM_AREA + " = ? AND " + BetaProvider.KEY_PROBLEM_ID_TYPE + " = " + IdType.MASTER,	
									new String[] {String.valueOf(areaId), String.valueOf(area.getMasterId())}, 
									BetaProvider.KEY_PROBLEM_NAME);
			startManagingCursor(problemCursor);
			
			problemAdapter = new MediaIconCursorAdapter(this, R.layout.icons_list_item, 
					problemCursor, 
					new String[] { BetaProvider.KEY_PROBLEM_NAME , 
									BetaProvider.KEY_PROBLEM_ID,
									BetaProvider.KEY_PROBLEM_ID}, 
					new int[] { R.id.icon_list_text, R.id.icon_1, R.id.icon_2});


			this.getListView().setOnItemClickListener( new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					startActivity(new Intent(AreaDetail.this, ProblemDetail.class).
							putExtra("problem_id", id));
				}
				
			});

			
			this.getListView().setOnItemLongClickListener(new OnItemLongClickListener (){
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					AreaDetail.this.selectedId = id;
					problemLongClicked = true;
					AreaDetail.this.openOptionsMenu();
					return true;
				}});
			
			this.setListAdapter(problemAdapter);
			
		}
		else
			finish();
		
		
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if(problemLongClicked){
			MenuHelper.createAreaDetailMenu(menu, isSyncing, selectedId, getContentResolver(), problemLongClicked);
			problemLongClicked = false;
		}
		else
			MenuHelper.createAreaDetailMenu(menu, isSyncing, areaId, getContentResolver(), problemLongClicked);
		return  super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		
		 // problem not selected, functions on Area:
		case MenuHelper.NEW_PROBLEM:
			i = new Intent(this, ProblemCreateEdit.class);
			i.putExtra("area_id", areaId);
			startActivity(i);
			return true;
		case MenuHelper.EDIT:
			i = new Intent(this, AreaCreateEdit.class);
			i.putExtra("area_id", areaId);
			startActivity(i);
			return true;
		case MenuHelper.DELETE:
			areaDeleteDialog.show();
			return true;
		case MenuHelper.MAP:
			i = new Intent(this, MapOverview.class);
			i.putExtra("action", MapOverview.SHOW_AREA);
			i.putExtra("area_id", areaId);
			startActivity(i);
			return true;
		case MenuHelper.ADD_MEDIA:
			i = new Intent(this, MediaCreateEdit.class);
			i.putExtra("area_id", areaId);
			i.putExtra("id_type", IdType.LOCAL);
			startActivity(i);
			return true;
		case MenuHelper.MAIN_MENU:
			startActivity(new Intent(this, BetterBeta.class));
			return true;
			
			
			//problem is selected:
		case MenuHelper.MAP_PROBLEM:
			i = new Intent(this, MapOverview.class);
			i.putExtra("action", MapOverview.SHOW_PROBLEM);
			i.putExtra("problem_id", selectedId);
			startActivity(i);
			return true;
		case MenuHelper.EDIT_PROBLEM:
			i = new Intent(this, ProblemCreateEdit.class);
			if (getSelectedItemId() > 0)
				i.putExtra("problem_id", getSelectedItemId());
			else
				i.putExtra("problem_id", selectedId);
			startActivity(i);
			break;
		case MenuHelper.VIEW_PROBLEM:
			i = new Intent(this, ProblemDetail.class);
			if (getSelectedItemId() > 0)
				i.putExtra("problem_id", getSelectedItemId());
			else
				i.putExtra("problem_id", selectedId);
			startActivity(i);
			break;

		case MenuHelper.ADD_MEDIA_TO_PROBLEM:
			i = new Intent(this, MediaCreateEdit.class);
			if (getSelectedItemId() > 0)
				i.putExtra("problem_id", getSelectedItemId());
			else
				i.putExtra("problem_id", selectedId);
			i.putExtra("id_type", IdType.LOCAL);

			startActivity(i);
			break;
		case MenuHelper.DELETE_PROBLEM:
			problemDeleteDialog.show();
			break;
			
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putLong("area_id", areaId);
	}
	
	@Override
	protected void onResume() {
		problemLongClicked = false;
		super.onResume();
	}

	@Override
	public void syncComplete() {
		// TODO Auto-generated method stub
		
	}
	
}


