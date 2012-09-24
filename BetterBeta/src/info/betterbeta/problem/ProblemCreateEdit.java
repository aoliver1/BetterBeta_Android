package info.betterbeta.problem;

import info.betterbeta.R;
import info.betterbeta.area.AreaHelper;
import info.betterbeta.maps.MapOverview;
import info.betterbeta.menu.MenuHelper;
import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Problem;
import info.betterbeta.model.State;
import info.betterbeta.provider.BetaProvider;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ProblemCreateEdit extends Activity {

	private static final int ACCURACY_LIMIT = 20;

	Problem problem;
	int action = MenuHelper.CREATE;
	long problemId;
	long areaId;
	
	//input fields
	EditText nameEditText;
	EditText detailsEditText;
	EditText longitudeEditText;
	EditText latitudeEditText;
	Button updateLocationButton;
	Button editLocationButton;
	Spinner areaSpinner;
	
	//location specific fields
	Location location;
	LocationManager locationManager;
	LocationListener locationListener;
	String storedLongitude;
	String storedLatitude;



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.problem_create);

		nameEditText = (EditText) findViewById(R.id.problem_name);
		detailsEditText = (EditText) findViewById(R.id.problem_details);
		longitudeEditText =  (EditText) findViewById(R.id.problem_longitude);
		latitudeEditText =  (EditText) findViewById(R.id.problem_latitude);

		getResources().getString(R.string.problem_name);

	//	area = new Area();
		problemId = 0;
		areaId = 0;
		
		if (getIntent().getExtras() != null){
				problemId = getIntent().getExtras().getLong("problem_id");
				areaId = getIntent().getExtras().getLong("area_id");
		}
	
        Cursor areaCursor = getContentResolver().query(Area.CONTENT_URI, null, null, null, null);   
        startManagingCursor(areaCursor);     
        areaSpinner = (Spinner) findViewById(R.id.area);     
        CursorAdapter areaAdapter = new CursorAdapter(this, areaCursor) {
		
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				final LayoutInflater inflater = LayoutInflater.from(context);
	            final TextView view = (TextView) inflater.inflate(
	                    R.layout.simple_text_view, parent, false);
	            view.setText(cursor.getString(BetaProvider.NAME_AREA_COLUMN));
	            return view;
			}
		
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
	            ((TextView) view).setText(cursor.getString(BetaProvider.NAME_AREA_COLUMN));
		
			}
		};
        areaSpinner.setAdapter(areaAdapter);
       // change this to a spinner, update to current, update on map
		updateLocationButton = (Button) findViewById(R.id.update_problem_location);
		updateLocationButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (ProblemCreateEdit.this.updateLocationButton.getText().toString().equals(
									getResources().getString(R.string.update_location)))
					updateLocation();
				else if (ProblemCreateEdit.this.updateLocationButton.getText().toString().equals(
									getResources().getString(R.string.cancel)))
					cancelUpdate();
				else
					useLocation();
			}

		});

		editLocationButton = (Button) findViewById(R.id.edit_problem_location);
		editLocationButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ProblemCreateEdit.this, MapOverview.class);
				storedLongitude = ProblemCreateEdit.this.longitudeEditText.getText().toString();
				storedLatitude = ProblemCreateEdit.this.latitudeEditText.getText().toString();
				i.putExtra("longitude", storedLongitude);
				i.putExtra("latitude", storedLatitude);
				i.putExtra("action", MapOverview.EDIT_LOCATION);
				startActivityForResult(i, MapOverview.EDIT_LOCATION);
			}
		});
        
			
		if (problemId > 0){
			if(!getIntent().getExtras().getBoolean("delete")){
				action = MenuHelper.EDIT;
				problem = ProblemHelper.inflate(problemId, getContentResolver(), IdType.LOCAL);
				nameEditText.setText(problem.getName());
				detailsEditText.setText(problem.getDetails());
				longitudeEditText.setText(problem.getLongitude());
				latitudeEditText.setText(problem.getLatitude());
				
				//if problem.getIdType == IdType.MASTER, then get master ID and compare with that, otherwise compare with local
				long areaToCompare = problem.getArea(); // default to local
				if (problem.getIdType() == IdType.MASTER){
					//look up area_id based on master;
					Cursor c = getContentResolver().query(Area.CONTENT_URI, 
							new String[]{BetaProvider.KEY_AREA_ID},
							BetaProvider.KEY_AREA_MASTER + "=?",
							new String[]{String.valueOf(problem.getArea())},
							null);
					
					if (c.moveToNext())
						areaToCompare = c.getLong(0);
					c.close();
				}
				int areaPosition = 0;
				for (int i = 0; i < areaAdapter.getCount(); i++){
					if(areaAdapter.getItemId(i) == areaToCompare){
						areaPosition = i;
					}
				}
				areaSpinner.setSelection(areaPosition);
				
			}
			
			else {
				String where = BetaProvider.KEY_PROBLEM_ID + " = ?";
				getContentResolver().delete(Problem.CONTENT_URI, where, new String[]{String.valueOf(problemId)});
				finish();
			}
		}
		else if (areaId > 0){
			int areaPosition = 0;
			for (int i = 0; i < areaAdapter.getCount(); i++){
				if(areaAdapter.getItemId(i) == areaId){
					areaPosition = i;
				}
			}
			areaSpinner.setSelection(areaPosition);
		}
		else
			action = MenuHelper.CREATE;
		
		nameEditText.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && (ProblemCreateEdit.this.nameEditText.getText().toString().equals(getResources().getString(R.string.problem_name))))
					ProblemCreateEdit.this.nameEditText.setText("");
			}	
		});
		
		detailsEditText.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && (ProblemCreateEdit.this.detailsEditText.getText().toString().equals(getResources().getString(R.string.problem_details))))
					ProblemCreateEdit.this.detailsEditText.setText("");
			}		
		});
		
		areaSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(ProblemCreateEdit.this.latitudeEditText.getText().toString().equals(getResources().getText(R.string.zero_point_zero)) &&
				   ProblemCreateEdit.this.longitudeEditText.getText().toString().equals(getResources().getText(R.string.zero_point_zero))){
						Area area = AreaHelper.inflateLocation(id, ProblemCreateEdit.this.getContentResolver(), IdType.LOCAL);
						ProblemCreateEdit.this.latitudeEditText.setText(area.getLatitude());
						ProblemCreateEdit.this.longitudeEditText.setText(area.getLongitude());

						storedLongitude = area.getLongitude();
						storedLatitude = area.getLatitude();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// don WORRY about it
				
			}
			
		});
	}
	
	
	protected void useLocation(){
		if (location != null){
			ProblemCreateEdit.this.longitudeEditText.setText(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
			ProblemCreateEdit.this.latitudeEditText.setText(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
			ProblemCreateEdit.this.locationManager.removeUpdates(locationListener);
			ProblemCreateEdit.this.updateLocationButton.setText(R.string.update_location);
		}
	}


	private void cancelUpdate() {
		ProblemCreateEdit.this.locationManager.removeUpdates(locationListener);
		ProblemCreateEdit.this.updateLocationButton.setText(R.string.update_location);
		ProblemCreateEdit.this.longitudeEditText.setText("");
		ProblemCreateEdit.this.latitudeEditText.setText("");
	}
	protected void updateLocation(){
		
		  locationListener = new LocationListener(){
				@Override
				public void onLocationChanged(Location location) {
					if (location.getAccuracy() > ACCURACY_LIMIT)
					{
						ProblemCreateEdit.this.longitudeEditText.setText("Loading, accuracy: " + location.getAccuracy() + " meters");
						ProblemCreateEdit.this.latitudeEditText.setText("Loading, accuracy: " + location.getAccuracy() + " meters");
						ProblemCreateEdit.this.location = location;
						ProblemCreateEdit.this.updateLocationButton.setText(R.string.use_current_accuracy);
					}
					else{
						ProblemCreateEdit.this.longitudeEditText.setText(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
						ProblemCreateEdit.this.latitudeEditText.setText(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
						ProblemCreateEdit.this.locationManager.removeUpdates(this);
						ProblemCreateEdit.this.updateLocationButton.setText(R.string.update_location);
					}
					
				}

				@Override
				public void onProviderDisabled(String provider) {
					if(storedLongitude.length() > 0 && storedLatitude.length() < 0){
						ProblemCreateEdit.this.longitudeEditText.setText("location disabled");
						ProblemCreateEdit.this.latitudeEditText.setText("location disabled");
					}
					else{
						ProblemCreateEdit.this.longitudeEditText.setText(getResources().getText(R.string.zero_point_zero));
						ProblemCreateEdit.this.latitudeEditText.setText(getResources().getText(R.string.zero_point_zero));
					}

					Toast.makeText(ProblemCreateEdit.this, "Location is disabled, please enable to use this feature.", Toast.LENGTH_SHORT).show();      
					
				}

				@Override
				public void onProviderEnabled(String provider) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					// TODO Auto-generated method stub
					
				}
	        };
	        
	        ProblemCreateEdit.this.updateLocationButton.setText(R.string.cancel);
	       	
	        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
	        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);	       	
	       	longitudeEditText.setText(R.string.loading);
	       	latitudeEditText.setText(R.string.loading);
	       	
	}

	void populateProblem() {
		this.problem = ProblemHelper.inflate(problemId, getContentResolver(), IdType.LOCAL);
		if (this.problem == null)
			this.problem = new Problem();
		problem.setName(this.nameEditText.getText().toString());
		problem.setDetails(this.detailsEditText.getText().toString());
		problem.setLongitude(longitudeEditText.getText().toString());
		problem.setLatitude(latitudeEditText.getText().toString());
		problem.setArea(areaSpinner.getSelectedItemId());
		problem.setIdType(IdType.LOCAL); 
		ProblemHelper.setMasterFromLocalArea(problem, getContentResolver());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		if (this.action == MenuHelper.CREATE)
			menu.add(0, MenuHelper.CREATE, 0, R.string.create);
		else if(this.action == MenuHelper.EDIT)
			menu.add(0, MenuHelper.EDIT, 0, R.string.save);
		menu.add(0, MenuHelper.DISCARD, 0, R.string.discard);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MenuHelper.CREATE:
			populateProblem();
			ProblemHelper.addNewProblem(problem, getContentResolver());
			finish();
			return true;
		case MenuHelper.EDIT:
			populateProblem();
			if (problem.getState() != State.NEW  && problem.getMasterId() != 0)
				problem.setState(State.DIRTY);
			ProblemHelper.updateProblem(problem, getContentResolver());
			finish();
			return true;
		case MenuHelper.DISCARD:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == MapOverview.EDIT_LOCATION && resultCode == RESULT_OK){
			this.latitudeEditText.setText(data.getStringExtra("latitude"));
			this.longitudeEditText.setText(data.getStringExtra("longitude"));
		}
		else if (requestCode == MapOverview.EDIT_LOCATION && resultCode == RESULT_CANCELED){
			this.latitudeEditText.setText(this.storedLatitude);
			this.longitudeEditText.setText(this.storedLongitude);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}


