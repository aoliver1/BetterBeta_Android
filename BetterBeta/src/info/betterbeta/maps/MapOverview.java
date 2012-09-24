package info.betterbeta.maps;

import info.betterbeta.R;
import info.betterbeta.area.AreaHelper;
import info.betterbeta.menu.MenuHelper;
import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Locatable;
import info.betterbeta.model.Problem;
import info.betterbeta.problem.ProblemHelper;
import info.betterbeta.provider.BetaProvider;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MapOverview extends MapActivity {

	
	public final static int NO_ACTION = -1;
	public final static int EDIT_LOCATION= 1;
	public final static int SHOW_PROBLEM= 2;
	public final static int SHOW_AREA= 3;

	public final static int PROBLEM_ZOOM = 18;
	public final static int AREA_ZOOM = 13;
	public final static int ME_ZOOM = 15;
	public final static int EARTH_ZOOM = 2;
	
    private MapView mapView;
    private MapController mapController;
    private GeoPoint myLocation;
    private Long problemId;
    private Long areaId;
    private ArrayList<Problem> problemList;
    private Area area;
 //   ProblemOverlay problemOverlay;
    ProblemItemizedOverlay problemItemizedOverlay;
    AreaItemizedOverlay areaItemizedOverlay;
    CrosshairsOverlay crosshairsOverlay;
    LocationManager locationManager;
    Boolean follow = false;
	private int action;
	private boolean showingProblems = false;
	private boolean showingMyLocation = false;
	MyLocationOverlay myLocationOverlay;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.map_overview);
        ViewGroup zoom=(ViewGroup)findViewById(R.id.zoom);
        
        problemList = new ArrayList<Problem>(1);
        mapView = (MapView) findViewById(R.id.mv);
        mapController = mapView.getController();
        mapView.setSatellite(true);
        myLocationOverlay = new MyLocationOverlay(this, mapView);
        zoom.addView(mapView.getZoomControls()); 
  //      mapView.setBuiltInZoomControls(true);
        
        action = savedInstanceState != null ? savedInstanceState.getInt("action") : -1;
        Bundle extras = getIntent().getExtras();
        if (action == -1 || action == 0){
        	action = extras != null ? extras.getInt("action") : -1;
        }
        
        switch (action) {
			case EDIT_LOCATION:
	        	String longitude = extras != null ? extras.getString("longitude") : null;
	        	String latitude = extras != null ? extras.getString("latitude") : null;
				updateLocation(longitude, latitude);
				break;
			case SHOW_PROBLEM:
			 	//try getting problem from saved instance state
		        problemId = savedInstanceState != null ? savedInstanceState.getLong("problem_id") : null;

		        //then try the bundle
				if (problemId == null) 
					problemId = extras != null ? extras.getLong("problem_id") : null;
				
				//finally, if problem, then animate to it
				if (problemId != null){
					Problem p = ProblemHelper.inflate(problemId, getContentResolver(), IdType.LOCAL);
					if (p != null)
						animateToProblem(p);
				}
				break;
			case SHOW_AREA:
				//try getting area from saved instance state
		        areaId = savedInstanceState != null ? savedInstanceState.getLong("area_id") : null;

		        //then try the bundle
				if (areaId == null) 
					areaId = extras != null ? extras.getLong("area_id") : null;
				
				//finally, if area, then animate to it
				if (areaId != null){
					Area a = AreaHelper.inflate(areaId, getContentResolver(), IdType.LOCAL);
					if (a != null){
						this.area = a;
						animateToArea(a);
					}
				}
				break;
			case NO_ACTION:
				showAllAreas();
        }
    }
    
    private void updateLocation(String longitude, String latitude) {
    	
    	GeoPoint p = Locatable.getGeoPointFromStrings(longitude, latitude);
    	crosshairsOverlay = new CrosshairsOverlay(findViewById(R.id.map_overview), longitude, latitude);
        mapView.getOverlays().add(crosshairsOverlay);
         
        mapController.animateTo(p);
        if(longitude.equals(getResources().getString(R.string.zero_point_zero)) && latitude.equals(getResources().getString(R.string.zero_point_zero)))
        	mapController.setZoom(EARTH_ZOOM);
        else
        	mapController.setZoom(PROBLEM_ZOOM);
        action = EDIT_LOCATION;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (action == EDIT_LOCATION)
			redrawCrosshairs();
		return super.dispatchTouchEvent(ev);
	}
	
	
	private void redrawCrosshairs(){
		crosshairsOverlay.setLongitude(Location.convert(mapView.getMapCenter().getLongitudeE6()/1E6, Location.FORMAT_SECONDS));
		crosshairsOverlay.setLatitude(Location.convert(mapView.getMapCenter().getLatitudeE6()/1E6, Location.FORMAT_SECONDS));
		mapView.invalidate(); 
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	if (problemId != null)
    	outState.putLong("problem_id", problemId);	
    	if (areaId != null)
    	outState.putLong("area_id", areaId);
    	
    	outState.putInt("action", action);	
   
    	super.onSaveInstanceState(outState);
    }
    
    public void onLocationChange(Location newLocation){
    	double lat = newLocation.getLatitude();
        double lng = newLocation.getLongitude();

       	myLocation = new GeoPoint(
            (int) (lat * 1E6),
            (int) (lng * 1E6));

       	if (follow)
       		mapController.animateTo(myLocation);
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_3:
            mapController.zoomIn();
            break;
        case KeyEvent.KEYCODE_1:
            mapController.zoomOut();
            break;
        }
        return super.onKeyDown(keyCode, event);
    }   

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.clear();
		switch (action){
			case EDIT_LOCATION:
				menu.add(Menu.NONE, MenuHelper.USE, Menu.NONE, R.string.use);
				menu.add(Menu.NONE, MenuHelper.CANCEL_EDIT, Menu.NONE, R.string.cancel);
				break;
			case NO_ACTION:
				menu.add(Menu.NONE, MenuHelper.ME, Menu.NONE, R.string.add_problem);
			//	menu.add(Menu.NONE, MenuHelper.FOLLOW_ME, Menu.NONE, R.string.follow_me);
				menu.add(Menu.NONE, MenuHelper.TOGGLE_LOCATION, Menu.NONE, R.string.toggle_location);
				break;
			case SHOW_AREA:
				menu.add(Menu.NONE, MenuHelper.TOGGLE_PROBLEMS, Menu.NONE, R.string.toggle_problems);
				menu.add(Menu.NONE, MenuHelper.ADD_PROBLEM, Menu.NONE, R.string.add_problem);
				menu.add(Menu.NONE, MenuHelper.TOGGLE_LOCATION, Menu.NONE, R.string.toggle_location);
				break;
			case SHOW_PROBLEM:
				menu.add(Menu.NONE, MenuHelper.ADD_PROBLEM, Menu.NONE, R.string.add_problem_near);
				menu.add(Menu.NONE, MenuHelper.TOGGLE_LOCATION, Menu.NONE, R.string.toggle_location);
				break;
		}
	
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MenuHelper.ME:
			if (myLocation != null)
				mapController.animateTo(myLocation);
			return true;
//		case MenuHelper.FOLLOW_ME:
//			follow = !follow; //toggle following
	//		return true;
		case MenuHelper.PROBLEM:
			if (this.problemList.size() > 0)
				animateToProblem(this.problemList.get(0));
			return true;
		case MenuHelper.AREA:
			if (this.area !=  null)
				animateToArea(this.area);
			return true;
		case MenuHelper.TOGGLE_PROBLEMS:
			if (this.area !=  null){
				if (showingProblems)
					hideProblems();
				else
					showProblems();
			}
			return true;
		case MenuHelper.TOGGLE_LOCATION:
			if (showingMyLocation)
				hideMyLocation();
			else
				showMyLocation();
			return true;
		case MenuHelper.USE:
			Bundle bundle = new Bundle();
	    	bundle.putString("longitude", Location.convert(mapView.getMapCenter().getLongitudeE6()/1E6,  Location.FORMAT_SECONDS));
	    	bundle.putString("latitude",Location.convert(mapView.getMapCenter().getLatitudeE6()/1E6,  Location.FORMAT_SECONDS)); 
	    	Intent mIntent = new Intent();
	    	mIntent.putExtras(bundle);
	    	setResult(RESULT_OK, mIntent);
	    	finish();
			return true;
		case MenuHelper.CANCEL_EDIT:
			setResult(RESULT_CANCELED);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
    private void showAllAreas(){
    	Cursor c = managedQuery(Area.CONTENT_URI, 
				new String []{BetaProvider.KEY_AREA_ID,
							  BetaProvider.KEY_AREA_NAME,
							  BetaProvider.KEY_AREA_LATITUDE,
							  BetaProvider.KEY_AREA_LONGITUDE}, 
							  BetaProvider.KEY_AREA_MASTER + " != ?", 
							  new String[] { String.valueOf(Area.NONE_ID)}, 
							  null);
							  
		startManagingCursor(c);
		areaItemizedOverlay = new AreaItemizedOverlay(getResources().getDrawable(R.drawable.bubble_red), this);
		while(c.moveToNext()){
			if (!"none".equals(c.getString(1))){
				Area a = new Area();
				a.setId(c.getLong(0));
				a.setName(c.getString(1));
				a.setLatitude(c.getString(2));
				a.setLongitude(c.getString(3));
				areaItemizedOverlay.addArea(a);
			}
		} 
		
		mapView.getOverlays().add(areaItemizedOverlay); 

     //   this.startLocationService();
        mapController.setZoom(EARTH_ZOOM);
    }

	private void showProblems() {
		// select from problems where id = area.id and id_type = LOCAL OR id = area.master AND id_type = MASTER
		Cursor c = getContentResolver().query(Problem.CONTENT_URI, 
												new String[]{BetaProvider.KEY_PROBLEM_ID, 
												BetaProvider.KEY_PROBLEM_NAME,
												BetaProvider.KEY_PROBLEM_LATITUDE,
												BetaProvider.KEY_PROBLEM_LONGITUDE},
												BetaProvider.KEY_PROBLEM_AREA + " = ? AND " + BetaProvider.KEY_PROBLEM_ID_TYPE + " = ? OR " +
													BetaProvider.KEY_PROBLEM_AREA + " = ? AND " + BetaProvider.KEY_PROBLEM_ID_TYPE + " = ?",
												new String[]{String.valueOf(this.area.getId()), String.valueOf(IdType.LOCAL),
														String.valueOf(this.area.getMasterId()), String.valueOf(IdType.MASTER)},
												null);
		 
		problemItemizedOverlay = new ProblemItemizedOverlay(getResources().getDrawable(R.drawable.bubble_blue), this);
		
		while (c.moveToNext()){
			Problem problem = new Problem();
			problem.setId(c.getLong(0));
			problem.setName(c.getString(1));
			problem.setLatitude(c.getString(2));
			problem.setLongitude(c.getString(3));
			problemItemizedOverlay.addProblem(problem);
		}	
		c.close();
		
		this.mapView.getOverlays().add(problemItemizedOverlay);
		this.showingProblems = true;
		mapView.invalidate();
	}

    public void animateToProblem(Problem problem){
        GeoPoint p = Locatable.getGeoPointFromStrings(problem.getLongitude(), problem.getLatitude());
		problemItemizedOverlay = new ProblemItemizedOverlay(getResources().getDrawable(R.drawable.bubble_blue), this);
		problemItemizedOverlay.addProblem(problem);
        mapView.getOverlays().add(problemItemizedOverlay);
        mapController.animateTo(p);
        mapController.setZoom(PROBLEM_ZOOM);
    }
    
    public void animateToArea(Area area){
    	GeoPoint p = Locatable.getGeoPointFromStrings(area.getLongitude(), area.getLatitude());
    	areaItemizedOverlay = new AreaItemizedOverlay(getResources().getDrawable(R.drawable.bubble_red), this);
		areaItemizedOverlay.addArea(area);
    	mapView.getOverlays().add(areaItemizedOverlay);
    	mapController.animateTo(p);
        mapController.setZoom(AREA_ZOOM);
    }
    
	private void hideProblems(){
		this.mapView.getOverlays().remove(problemItemizedOverlay);
		this.showingProblems = false;
		mapView.invalidate();
	}
	private void hideMyLocation(){
		showingMyLocation = false;
		myLocationOverlay.disableMyLocation();
		mapView.getOverlays().remove(myLocationOverlay);
		mapView.invalidate();
	}
	private void showMyLocation(){
		if (myLocationOverlay.enableMyLocation()){
			mapView.getOverlays().add(myLocationOverlay);
			mapView.invalidate();
			showingMyLocation = true;
		}
		else
			Toast.makeText(this, "Location is disabled, please enable to use this feature.", Toast.LENGTH_SHORT).show();      
			
	}
} 