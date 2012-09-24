package info.betterbeta.media;

import info.betterbeta.R;
import info.betterbeta.maps.MapOverview;
import info.betterbeta.menu.MenuHelper;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Media;
import info.betterbeta.model.Permission;
import info.betterbeta.model.Problem;
import info.betterbeta.model.State;
import info.betterbeta.problem.ProblemHelper;
import info.betterbeta.provider.BetaProvider;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MediaCreateEdit extends Activity {

	private static final int ACCURACY_LIMIT = 20;

	Media media;
	
	TextView subjectTextView;
	EditText nameEditText;
	EditText detailsEditText;
	EditText pathEditText;
	EditText longitudeEditText;
	EditText latitudeEditText;
	Button updateLocationButton;
	Button editLocationButton;
	Spinner typeSpinner;
	
	Location location;
	LocationManager locationManager;
	LocationListener locationListener;
	String storedLongitude;
	String storedLatitude;
	Long areaId, problemId;
//	LocationView locationView;
	int action = MenuHelper.CREATE;
	long mediaId;
	int idType = IdType.LOCAL; //of problem or area
	ArrayAdapter<CharSequence> typeAdapter;
	
//	static int PICTURE;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_create);

		subjectTextView = (TextView) findViewById(R.id.media_subject);
		nameEditText = (EditText) findViewById(R.id.media_name);
		detailsEditText = (EditText) findViewById(R.id.media_details);
		pathEditText = (EditText) findViewById(R.id.media_path);
		longitudeEditText =  (EditText) findViewById(R.id.media_longitude);
		latitudeEditText =  (EditText) findViewById(R.id.media_latitude);
		typeSpinner  = (Spinner) findViewById(R.id.media_type);
        typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.media_types, R.layout.simple_text_view); //CAUTION! : this xml array MUST conform to Media.TYPE_XXX
        typeSpinner.setAdapter(typeAdapter);

		mediaId = 0;
		
		if (getIntent().getExtras() != null)
				mediaId = getIntent().getExtras().getLong("media_id");
		
		if (mediaId > 0){
			if(!getIntent().getExtras().getBoolean("delete")){
				action = MenuHelper.EDIT;
				media = MediaHelper.inflate(mediaId, getContentResolver(), IdType.LOCAL);
				nameEditText.setText(media.getName());
				detailsEditText.setText(media.getDetails());
				pathEditText.setText(media.getPath());
				longitudeEditText.setText(media.getLongitude());
				latitudeEditText.setText(media.getLatitude());
				typeSpinner.setSelection(media.getType());
				this.idType = media.getIdType();
			}
			else {
				String where = BetaProvider.KEY_MEDIA_ID + " = ?";
				getContentResolver().delete(Media.CONTENT_URI, where, new String[]{String.valueOf(mediaId)});
				finish();
			}
		}
		else
			action = MenuHelper.CREATE;
		
		// determine what you are adding it to
		if (action == MenuHelper.CREATE)
		{
			if (getIntent().getExtras() != null){
				problemId = getIntent().getExtras().getLong("problem_id");
				areaId = getIntent().getExtras().getLong("area_id");
				idType = getIntent().getExtras().getInt("id_type");
				
				if (problemId != null && problemId> 0){
					Problem problem;
					if (idType == IdType.LOCAL)
						problem = ProblemHelper.inflate(problemId, getContentResolver(), IdType.LOCAL);
					else
						problem = ProblemHelper.inflate(problemId, getContentResolver(), IdType.MASTER);
					subjectTextView.setText("Adding Media to Problem: " + problem.getName());
					longitudeEditText.setText(problem.getLongitude());
					latitudeEditText.setText(problem.getLatitude());
				}
			}
		}
		
		updateLocationButton = (Button) findViewById(R.id.update_media_location);
		updateLocationButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (MediaCreateEdit.this.updateLocationButton.getText().toString().equals(
									getResources().getString(R.string.update_location)))
					updateLocation();
				else if (MediaCreateEdit.this.updateLocationButton.getText().toString().equals(
									getResources().getString(R.string.cancel)))
					cancelUpdate();
				else
					useLocation();
			}

		});

		editLocationButton = (Button) findViewById(R.id.edit_media_location);
		editLocationButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				storedLongitude = MediaCreateEdit.this.longitudeEditText.getText().toString();
				storedLatitude = MediaCreateEdit.this.latitudeEditText.getText().toString();
				Intent i = new Intent(MediaCreateEdit.this, MapOverview.class);
				i.putExtra("longitude", storedLongitude);
				i.putExtra("latitude", storedLatitude);
				i.putExtra("action", MapOverview.EDIT_LOCATION);
				startActivityForResult(i, MapOverview.EDIT_LOCATION);
			}
		});
        
		nameEditText.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && (MediaCreateEdit.this.nameEditText.getText().toString().equals(getResources().getString(R.string.media_name))))
					MediaCreateEdit.this.nameEditText.setText("");
			}	
		});
		
		detailsEditText.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && (MediaCreateEdit.this.detailsEditText.getText().toString().equals(getResources().getString(R.string.media_details))))
					MediaCreateEdit.this.detailsEditText.setText("");
			}		
		});

		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(position == 2){
					Intent intent = new Intent("android.intent.action.PICK");
					intent.setType("image/*");
					startActivityForResult(intent, MenuHelper.PICK_IMAGE);	
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
			MediaCreateEdit.this.longitudeEditText.setText(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
			MediaCreateEdit.this.latitudeEditText.setText(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
			MediaCreateEdit.this.locationManager.removeUpdates(locationListener);
			MediaCreateEdit.this.updateLocationButton.setText(R.string.update_location);
		}
	}


	private void cancelUpdate() {
		MediaCreateEdit.this.locationManager.removeUpdates(locationListener);
		MediaCreateEdit.this.updateLocationButton.setText(R.string.update_location);
		MediaCreateEdit.this.longitudeEditText.setText("");
		MediaCreateEdit.this.latitudeEditText.setText("");
	}
	
	protected void updateLocation(){
		
		  locationListener = new LocationListener(){
				@Override
				public void onLocationChanged(Location location) {
					if (location.getAccuracy() > ACCURACY_LIMIT)
					{
						MediaCreateEdit.this.longitudeEditText.setText("Loading, accuracy: " + location.getAccuracy() + " meters");
						MediaCreateEdit.this.latitudeEditText.setText("Loading, accuracy: " + location.getAccuracy() + " meters");
						MediaCreateEdit.this.location = location;
						MediaCreateEdit.this.updateLocationButton.setText(R.string.use_current_accuracy);
					}
					else{
						MediaCreateEdit.this.longitudeEditText.setText(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS));
						MediaCreateEdit.this.latitudeEditText.setText(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS));
						MediaCreateEdit.this.locationManager.removeUpdates(this);
						MediaCreateEdit.this.updateLocationButton.setText(R.string.update_location);
					}
					
				}

				@Override
				public void onProviderDisabled(String provider) {
					MediaCreateEdit.this.longitudeEditText.setText("location disabled");
					MediaCreateEdit.this.latitudeEditText.setText("location disabled");
					
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
	        
	        MediaCreateEdit.this.updateLocationButton.setText(R.string.cancel);
	       	
	        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
	        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);	       	
	       	longitudeEditText.setText(R.string.loading);
	       	latitudeEditText.setText(R.string.loading);
	       	
	}

	
	protected void populateMedia() {
		media = MediaHelper.inflate(mediaId, getContentResolver(), IdType.LOCAL);
		if (media == null)
			media = new Media();	
		media.setName(this.nameEditText.getText().toString());
		media.setDetails(this.detailsEditText.getText().toString());
		media.setLongitude(longitudeEditText.getText().toString());
		media.setLatitude(latitudeEditText.getText().toString());
		if(media.getIdType() == IdType.MASTER)
			this.idType = IdType.MASTER;
		
		media.setPermission(Permission.GOD);
		media.setIdType(idType);
		
		if (areaId != null && areaId > 0){
			media.setArea(areaId);
			MediaHelper.setMasterFromLocalArea(media, getContentResolver());
		}
		if (problemId != null && problemId > 0){
			media.setProblem(problemId);
			MediaHelper.setMasterFromLocalProblem(media, getContentResolver());
		}
		
		media.setType(typeSpinner.getSelectedItemPosition());
		media.setPath(pathEditText.getText().toString());
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
			if(nameEditText.getText().toString().length() > 0){
				populateMedia();
				media.setState(State.NEW);
				MediaHelper.addNewMedia(media, getContentResolver());
				finish();
				return true;
			}
			else{
				Toast.makeText(MediaCreateEdit.this, "Media must have a Name", Toast.LENGTH_SHORT).show();
				break;
			}
		case MenuHelper.EDIT:
			if(nameEditText.getText().toString().length() > 0){
				populateMedia();
				if (media.getState() != State.NEW  && media.getMasterId() != 0)
					media.setState(State.DIRTY);
				MediaHelper.updateMedia(media, getContentResolver());
				finish();
				return true;
			}
			else{
				Toast.makeText(MediaCreateEdit.this, "Media must have a Name", Toast.LENGTH_SHORT).show();
				break;
			}
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
		else if (requestCode == MenuHelper.PICK_IMAGE && resultCode == RESULT_OK){
			
			this.pathEditText.setText(data.getDataString());
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
}


