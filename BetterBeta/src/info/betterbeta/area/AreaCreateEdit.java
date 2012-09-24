package info.betterbeta.area;

import info.betterbeta.R;
import info.betterbeta.maps.MapOverview;
import info.betterbeta.menu.MenuHelper;
import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
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

public class AreaCreateEdit extends Activity {
	
	private static final int ACCURACY_LIMIT = 20;
	
	EditText nameEditText;
	EditText detailsEditText;
	EditText longitudeEditText;
	EditText latitudeEditText;
	Spinner parentSpinner;
	Area area;
	int action = MenuHelper.CREATE;
	Location location;
	LocationListener locationListener;
	LocationManager locationManager;
	Button updateLocationButton;
	Button editLocationButton;
	long areaId;
	String storedLongitude;
	String storedLatitude;
	
	//TODO make location widgit to handle the location and calls to the map overview.
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.area_create);

		nameEditText = (EditText) findViewById(R.id.area_name);
		detailsEditText = (EditText) findViewById(R.id.area_details);
		longitudeEditText = (EditText) findViewById(R.id.area_longitude);
		latitudeEditText = (EditText) findViewById(R.id.area_latitude);
	
        Cursor parentCursor = getContentResolver().query(Area.CONTENT_URI, null, null, null, null);   
        startManagingCursor(parentCursor);     
        parentSpinner = (Spinner) findViewById(R.id.parent);     
        CursorAdapter parentAdapter = new CursorAdapter(this, parentCursor) {
		
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
        parentSpinner.setAdapter(parentAdapter);
        
        updateLocationButton = (Button) findViewById(R.id.update_area_location);
		updateLocationButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (AreaCreateEdit.this.updateLocationButton.getText().toString().equals(
									getResources().getString(R.string.update_location)))
					updateLocation();
				else if (AreaCreateEdit.this.updateLocationButton.getText().toString().equals(
									getResources().getString(R.string.cancel)))
					cancelUpdate();
				else
					useLocation();
			}


		});

		this.editLocationButton = (Button) findViewById(R.id.edit_problem_location);
		editLocationButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(AreaCreateEdit.this, MapOverview.class);
				storedLongitude = AreaCreateEdit.this.longitudeEditText.getText().toString();
				storedLatitude = AreaCreateEdit.this.latitudeEditText.getText().toString();
				i.putExtra("longitude", storedLongitude);
				i.putExtra("latitude", storedLatitude);
				i.putExtra("action", MapOverview.EDIT_LOCATION);
				startActivityForResult(i, MapOverview.EDIT_LOCATION);
			}
		});
		
		areaId = 0;
		
		if (getIntent().getExtras() != null)
				areaId = getIntent().getExtras().getLong("area_id");
		
		if (areaId > 0){
			if(!getIntent().getExtras().getBoolean("delete")){
				action = MenuHelper.EDIT;
				area = AreaHelper.inflate(areaId, getContentResolver(), IdType.LOCAL);
				nameEditText.setText(area.getName());
				detailsEditText.setText(area.getDetails());
				longitudeEditText.setText(area.getLongitude());
				latitudeEditText.setText(area.getLatitude());
				
				//if problem.getIdType == IdType.MASTER, then get master ID and compare with that, otherwise compare with local
				long areaToCompare = area.getParent(); // default to local
				if (area.getIdType() == IdType.MASTER){
					//look up area_id based on master;
					Cursor c = getContentResolver().query(Area.CONTENT_URI, 
							new String[]{BetaProvider.KEY_AREA_ID},
							BetaProvider.KEY_AREA_MASTER + "=?",
							new String[]{String.valueOf(area.getParent())},
							null);
					
					if (c.moveToNext())
						areaToCompare = c.getLong(0);
					c.close();
				}
				int areaPosition = 0;
				for (int i = 0; i < parentAdapter.getCount(); i++){
					if(parentAdapter.getItemId(i) == areaToCompare){
						areaPosition = i;
					}
				}
				
				parentSpinner.setSelection(areaPosition);
			}
			else {
				String where = BetaProvider.KEY_AREA_ID + " = ?";
				getContentResolver().delete(Area.CONTENT_URI, where, new String[]{String.valueOf(areaId)});
				finish();
			}
		}
		else
			action = MenuHelper.CREATE;
		
		nameEditText.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && (AreaCreateEdit.this.nameEditText.getText().toString().equals(getResources().getString(R.string.area_name))))
					AreaCreateEdit.this.nameEditText.setText("");
			}	
		});
		
		detailsEditText.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && (AreaCreateEdit.this.detailsEditText.getText().toString().equals(getResources().getString(R.string.area_details))))
					AreaCreateEdit.this.detailsEditText.setText("");
			}		
		});
		
		parentSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(AreaCreateEdit.this.latitudeEditText.getText().toString().equals(getResources().getText(R.string.zero_point_zero)) &&
						AreaCreateEdit.this.longitudeEditText.getText().toString().equals(getResources().getText(R.string.zero_point_zero))){
						Area area = AreaHelper.inflateLocation(id, AreaCreateEdit.this.getContentResolver(),  IdType.LOCAL);
						AreaCreateEdit.this.latitudeEditText.setText(area.getLatitude());
						AreaCreateEdit.this.longitudeEditText.setText(area.getLongitude());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// don WORRY about it
				
			}
			
		});
	}
	
	protected void populateArea() {
		area = AreaHelper.inflate(areaId, getContentResolver(), IdType.LOCAL);
		if (area == null)
			area = new Area();
		area.setName(nameEditText.getText().toString());
		area.setDetails(detailsEditText.getText().toString());
		area.setLongitude(longitudeEditText.getText().toString());
		area.setLatitude(latitudeEditText.getText().toString());
		area.setParent(parentSpinner.getSelectedItemId());
		area.setIdType(IdType.LOCAL);
		AreaHelper.setMasterFromLocalParentArea(area, getContentResolver());
	}

	protected void useLocation(){
		if (location != null){
			AreaCreateEdit.this.longitudeEditText.setText(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
			AreaCreateEdit.this.latitudeEditText.setText(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
			AreaCreateEdit.this.locationManager.removeUpdates(locationListener);
			AreaCreateEdit.this.updateLocationButton.setText(R.string.update_location);
		}
	}

	private void cancelUpdate() {
		AreaCreateEdit.this.locationManager.removeUpdates(locationListener);
		AreaCreateEdit.this.updateLocationButton.setText(R.string.update_location);
		AreaCreateEdit.this.longitudeEditText.setText("");
		AreaCreateEdit.this.latitudeEditText.setText("");
	}
	protected void updateLocation(){
		
		  locationListener = new LocationListener(){
				@Override
				public void onLocationChanged(Location location) {
					if (location.getAccuracy() > ACCURACY_LIMIT)
					{
						AreaCreateEdit.this.longitudeEditText.setText("Loading, accuracy: " + location.getAccuracy() + " meters");
						AreaCreateEdit.this.latitudeEditText.setText("Loading, accuracy: " + location.getAccuracy() + " meters");
						AreaCreateEdit.this.location = location;
				        AreaCreateEdit.this.updateLocationButton.setText(R.string.use_current_accuracy);
					}
					else{
						AreaCreateEdit.this.longitudeEditText.setText(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
						AreaCreateEdit.this.latitudeEditText.setText(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
						AreaCreateEdit.this.locationManager.removeUpdates(this);
						AreaCreateEdit.this.updateLocationButton.setText(R.string.update_location);
					}
					
				}

				@Override
				public void onProviderDisabled(String provider) {
					if(storedLongitude.length() > 0 && storedLatitude.length() < 0){
						AreaCreateEdit.this.longitudeEditText.setText("location disabled");
						AreaCreateEdit.this.latitudeEditText.setText("location disabled");
					}
					else{
						AreaCreateEdit.this.longitudeEditText.setText(getResources().getText(R.string.zero_point_zero));
						AreaCreateEdit.this.latitudeEditText.setText(getResources().getText(R.string.zero_point_zero));
					}

					Toast.makeText(AreaCreateEdit.this, "Location is disabled, please enable to use this feature.", Toast.LENGTH_SHORT).show();      
					
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
	        
	        AreaCreateEdit.this.updateLocationButton.setText(R.string.cancel);
	       	
	        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
	        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);	       	
	       	longitudeEditText.setText("loading");
	       	latitudeEditText.setText("loading");
	       	
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
			populateArea();
			area.setState(State.NEW);
			AreaHelper.addNewArea(area, getContentResolver());	
			finish();
			return true;
		case MenuHelper.EDIT:
			populateArea();
			if (area.getState() != State.NEW  && area.getMasterId() != 0)
				area.setState(State.DIRTY);
			AreaHelper.updateArea(area, getContentResolver());
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
		super.onActivityResult(requestCode, resultCode, data);
	}
}
