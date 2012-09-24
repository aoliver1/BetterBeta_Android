package info.betterbeta.problem;

import info.betterbeta.BetterBeta;
import info.betterbeta.ProgressBarListActivity;
import info.betterbeta.R;
import info.betterbeta.area.AreaHelper;
import info.betterbeta.maps.MapOverview;
import info.betterbeta.media.MediaCreateEdit;
import info.betterbeta.media.MediaIconCursorAdapter;
import info.betterbeta.menu.MenuHelper;
import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Problem;
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
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class Problems extends ProgressBarListActivity {
	
	SimpleCursorAdapter problemAdapter;
	Cursor areaCursor;
	Cursor problemCursor;
	Cursor mediaCursor;
	
	AlertDialog problemDeleteDialog;
	Spinner areaSpinner;
	long selectedId = -1;
	boolean problemLongClicked;

	int selectedPosition = -1;
	long areaId;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.problems);
	
        areaSpinner = (Spinner) findViewById(R.id.problems_area);     
        areaId = savedInstanceState != null ? savedInstanceState.getLong("area_id") : -1;
 	   
        Cursor areaCursor = this.managedQuery(Area.CONTENT_URI, 
				new String[] { BetaProvider.KEY_AREA_NAME,
								BetaProvider.KEY_AREA_ID,
								BetaProvider.KEY_AREA_MASTER},
				null, 
				null,
				null);
	
		startManagingCursor(areaCursor);
		
		String[] from = new String[] { BetaProvider.KEY_AREA_NAME };
		int[] to = new int[] { R.id.simple_list_text};
	
		SimpleCursorAdapter adapter = new ProblemCountCursorAdapter(this, R.layout.simple_list_item, areaCursor, from, to);
	
	    areaSpinner.setAdapter(adapter);
		
	    areaSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				filterByArea(id);
			//	areaId = id;
				selectedPosition = position;
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
	    });


		
		this.getListView().setOnItemClickListener( new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startActivity(new Intent(Problems.this, ProblemDetail.class).
						putExtra("problem_id", id));
			}
			
		});
		
		this.getListView().setOnItemLongClickListener(new OnItemLongClickListener (){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Problems.this.selectedId = id;
				problemLongClicked = true;
				Problems.this.openOptionsMenu();
				return true;
			}});
		
        
		problemDeleteDialog = new AlertDialog.Builder(Problems.this)
	       .setTitle(R.string.are_you_sure)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	Intent i = new Intent(Problems.this, ProblemCreateEdit.class);
	    			i.putExtra("delete", true);
	    			if (getSelectedItemId() > 0)
	    				i.putExtra("problem_id", getSelectedItemId());
	    			else
	    				i.putExtra("problem_id", selectedId);
	    			
	    			startActivity(i);

	            }
	        })
	        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	                /* User clicked Cancel do NOTHING */
	            }
	        })
	        .create();


	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();
		if(problemLongClicked){
			MenuHelper.createProblemsMenu(menu, isSyncing, selectedId, getContentResolver());
			problemLongClicked = false;
		}
		else
			MenuHelper.createProblemsMenu(menu, isSyncing, 0, getContentResolver());
		return super.onPrepareOptionsMenu(menu);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case MenuHelper.NEW:
			i = new Intent(this, ProblemCreateEdit.class);
			startActivity(i);
			break;
		case MenuHelper.EDIT:
			i = new Intent(this, ProblemCreateEdit.class);
			if (getSelectedItemId() > 0)
				i.putExtra("problem_id", getSelectedItemId());
			else
				i.putExtra("problem_id", selectedId);
			startActivity(i);
			break;
		case MenuHelper.VIEW:
			i = new Intent(this, ProblemDetail.class);
			if (getSelectedItemId() > 0)
				i.putExtra("problem_id", getSelectedItemId());
			else
				i.putExtra("problem_id", selectedId);
			startActivity(i);
			break;
		case MenuHelper.DELETE:
			problemDeleteDialog.show();
			break;
		case MenuHelper.ADD_MEDIA:
			i = new Intent(this, MediaCreateEdit.class);			
			if (getSelectedItemId() > 0)
				i.putExtra("problem_id", getSelectedItemId());
			else
				i.putExtra("problem_id", selectedId);
			i.putExtra("id_type", IdType.LOCAL);					
			startActivity(i);
			return true;
		case MenuHelper.MAIN_MENU:
			startActivity(new Intent(this, BetterBeta.class));
			break;
		case MenuHelper.MAP:
			i = new Intent(this, MapOverview.class);
			i.putExtra("action", MapOverview.SHOW_PROBLEM);
			if (getSelectedItemId() > 0)
				i.putExtra("problem_id", getSelectedItemId());
			else
				i.putExtra("problem_id", selectedId);
			startActivity(i);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void filterByArea(long areaId){
		
		Area area = AreaHelper.inflate(areaId, getContentResolver(), IdType.LOCAL);
		
		if (area.getMasterId() == Area.NONE_ID){
			problemCursor = managedQuery(Problem.CONTENT_URI,
											new String[] { BetaProvider.KEY_PROBLEM_NAME, BetaProvider.KEY_PROBLEM_ID, BetaProvider.KEY_PROBLEM_MASTER }, 
											null, 
											null, 
											BetaProvider.KEY_PROBLEM_NAME);  
		}
			
		else{
		problemCursor = managedQuery(Problem.CONTENT_URI, 
									new String[] { BetaProvider.KEY_PROBLEM_NAME, BetaProvider.KEY_PROBLEM_ID, BetaProvider.KEY_PROBLEM_MASTER }, 
									BetaProvider.KEY_PROBLEM_AREA + " = ? AND " + BetaProvider.KEY_PROBLEM_ID_TYPE + " = " + IdType.LOCAL  + " OR " +
									BetaProvider.KEY_PROBLEM_AREA + " = ? AND " + BetaProvider.KEY_PROBLEM_ID_TYPE + " = " + IdType.MASTER,	
									new String[] {String.valueOf(areaId), String.valueOf(area.getMasterId())}, 
									BetaProvider.KEY_PROBLEM_NAME);   
		}

		this.startManagingCursor(problemCursor);
		problemAdapter = new MediaIconCursorAdapter(this, R.layout.icons_list_item, 
															problemCursor, 
															new String[] { BetaProvider.KEY_PROBLEM_NAME , 
																			BetaProvider.KEY_PROBLEM_ID,
																			BetaProvider.KEY_PROBLEM_ID}, 
															new int[] { R.id.icon_list_text, R.id.icon_1, R.id.icon_2});

		this.setListAdapter(problemAdapter);
	}
	@Override
	protected void onResume() {
		problemLongClicked = false;
		if(selectedPosition > 0){
			areaSpinner.setSelection(selectedPosition);
			filterByArea(areaSpinner.getSelectedItemId());
		}
		super.onResume();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("area_id", areaId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void syncComplete() {
		// TODO Auto-generated method stub
		
	}
	
}