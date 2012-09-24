package info.betterbeta.area;

import info.betterbeta.BetterBeta;
import info.betterbeta.ProgressBarListActivity;
import info.betterbeta.R;
import info.betterbeta.maps.MapOverview;
import info.betterbeta.menu.MenuHelper;
import info.betterbeta.model.Area;
import info.betterbeta.problem.ProblemCountCursorAdapter;
import info.betterbeta.problem.ProblemCreateEdit;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Areas extends ProgressBarListActivity {

	long selectedId = Long.valueOf(-1);
	boolean areaLongClicked;

	private AlertDialog deleteAlertDialog;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.areas);
		
		deleteAlertDialog = new AlertDialog.Builder(Areas.this)
	       .setTitle(R.string.are_you_sure)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	    			Intent i = new Intent(Areas.this, AreaCreateEdit.class);
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
		
		Cursor areaCursor = this.managedQuery(Area.CONTENT_URI, 
					new String[] { BetaProvider.KEY_AREA_NAME,
									BetaProvider.KEY_AREA_ID,
									BetaProvider.KEY_AREA_MASTER},
					BetaProvider.KEY_AREA_MASTER + " != ?", 
					new String[] { String.valueOf(Area.NONE_ID)},
					null);
		
		startManagingCursor(areaCursor);
		
		String[] from = new String[] { BetaProvider.KEY_AREA_NAME };
		int[] to = new int[] { R.id.simple_list_text};

		SimpleCursorAdapter adapter = new ProblemCountCursorAdapter(this, R.layout.simple_list_item, areaCursor, from, to);
		
		setListAdapter(adapter);
		
		this.getListView().setOnItemClickListener( new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startActivity(new Intent(Areas.this, AreaDetail.class).
						putExtra("area_id", id));
			}
			
		});

		
		this.getListView().setOnItemLongClickListener(new OnItemLongClickListener (){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Areas.this.selectedId = id;
				areaLongClicked = true;
				Areas.this.openOptionsMenu();
				return true;
			}});
		
	}
	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (areaLongClicked){
			MenuHelper.createAreasMenu(menu, isSyncing, selectedId, getContentResolver());
			areaLongClicked = false;
		}
		else
			MenuHelper.createAreasMenu(menu, isSyncing, 0, getContentResolver());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case MenuHelper.NEW:
			i = new Intent(this, AreaCreateEdit.class);
			startActivity(i);
		//	finish();
			return true;
		case MenuHelper.VIEW:
			i = new Intent(Areas.this, AreaDetail.class);
			if (getSelectedItemId() > 0)
				i.putExtra("area_id", getSelectedItemId());
			else
				i.putExtra("area_id", selectedId);
			startActivity(i);
			return true;
		case MenuHelper.NEW_PROBLEM:
			i = new Intent(this, ProblemCreateEdit.class);
			if (getSelectedItemId() > 0)
				i.putExtra("area_id", getSelectedItemId());
			else
				i.putExtra("area_id", selectedId);
			startActivity(i);
			return true;
		case MenuHelper.MAIN_MENU:
			i = new Intent(this, BetterBeta.class);
			startActivity(i);
			return true;
		case MenuHelper.EDIT:
			i = new Intent(this, AreaCreateEdit.class);
			if (getSelectedItemId() > 0)
				i.putExtra("area_id", getSelectedItemId());
			else
				i.putExtra("area_id", selectedId);
			startActivity(i);
			return true;
		case MenuHelper.DELETE:
			deleteAlertDialog.show();
			return true;
		case MenuHelper.MAP:
			i = new Intent(this, MapOverview.class);
			i.putExtra("action", MapOverview.SHOW_AREA);
			if (getSelectedItemId() > 0)
				i.putExtra("area_id", getSelectedItemId());
			else
				i.putExtra("area_id", selectedId);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onResume() {
		areaLongClicked = false;
		super.onResume();
	}


	@Override
	public void syncComplete() {
		// TODO Auto-generated method stub
		
	}
}
