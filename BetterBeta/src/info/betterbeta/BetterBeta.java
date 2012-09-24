package info.betterbeta;

import info.betterbeta.area.AreaHelper;
import info.betterbeta.area.Areas;
import info.betterbeta.maps.MapOverview;
import info.betterbeta.media.MediaHelper;
import info.betterbeta.menu.MenuHelper;
import info.betterbeta.model.Area;
import info.betterbeta.model.Problem;
import info.betterbeta.problem.ProblemHelper;
import info.betterbeta.problem.Problems;
import info.betterbeta.provider.BetaProvider;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BetterBeta extends ProgressBarListActivity {
	Button mapButton;
	Button areasButton;
	Button problemsButton;
	boolean showSyncWarning = false;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);

		mapButton = (Button) findViewById(R.id.map_button);
		mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showMap();
            }
        });
		areasButton = (Button) findViewById(R.id.areas_button);
		areasButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showAreas();
            }
        });
		
		Cursor areaCountCursor = getContentResolver().query(Area.CONTENT_URI_COUNT, null, null, null, null);
		areaCountCursor.moveToNext();
		areasButton.setText(getResources().getText(R.string.areas) + " (" + areaCountCursor.getLong(0) + ")");
		areaCountCursor.close();
		
		problemsButton = (Button) findViewById(R.id.problems_button);
		problemsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showProblems();
            }
        });

		
		Cursor problemCountCursor = getContentResolver().query(Problem.CONTENT_URI_COUNT, null, null, null, null);
		problemCountCursor.moveToNext();
		problemsButton.setText(getResources().getText(R.string.problems) + " (" + problemCountCursor.getLong(0) + ")");
		problemCountCursor.close();
		
		Cursor areaCursor = this.getContentResolver().query(Area.CONTENT_URI, 
					new String[] { BetaProvider.KEY_AREA_NAME, BetaProvider.KEY_AREA_ID },
					BetaProvider.KEY_AREA_MASTER + " != ?", 
					new String[] { String.valueOf(Area.NONE_ID)},
					null);
		
		if (areaCursor.getCount() == 0 && (!isSyncing))
			this.syncOnConnect = true;
	
		areaCursor.close();
		
	}

	public void showMap()
	{
		Intent i = new Intent(this, MapOverview.class);
		startActivity(i);
	}

	public void showAreas()
	{
		Intent i = new Intent(this, Areas.class);
		startActivity(i);
	}

	public void showProblems()
	{
		Intent i = new Intent(this, Problems.class);
		startActivity(i);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();
		if(!isSyncing)
			menu.add(0, MenuHelper.SYNC, 0, R.string.sync);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MenuHelper.SYNC:
			this.syncAll();
			((TextView)findViewById(R.id.sync_warning_text)).setText("");
			showSyncWarning = false;
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		
		showSyncWarning = false;
	
		if (MediaHelper.getNewAndDirtyMedias(getContentResolver()).size() +
						ProblemHelper.getNewAndDirtyProblems(getContentResolver()).size() +
						AreaHelper.getNewAndDirtyAreas(getContentResolver()).size() > 0){
			showSyncWarning = true;
		}
		
		if (showSyncWarning)
			((TextView)findViewById(R.id.sync_warning_text)).setText(getResources().getText(R.string.sync_warning));
		else
			((TextView)findViewById(R.id.sync_warning_text)).setText("");
	
		updateCounts();
		super.onResume();
	}

	@Override
	public void syncComplete() {	
		updateCounts();
		((TextView)findViewById(R.id.sync_warning_text)).setText("");
	}
	
	
	private void updateCounts(){
		
		Cursor areaCountCursor = getContentResolver().query(Area.CONTENT_URI_COUNT, null, null, null, null);
		areaCountCursor.moveToNext();
		areasButton.setText(getResources().getText(R.string.areas) + " (" + areaCountCursor.getLong(0) + ")");
		areaCountCursor.close();
		
		Cursor problemCountCursor = getContentResolver().query(Problem.CONTENT_URI_COUNT, null, null, null, null);
		problemCountCursor.moveToNext();
		problemsButton.setText(getResources().getText(R.string.problems) + " (" + problemCountCursor.getLong(0) + ")");
		problemCountCursor.close();
		
	}

}