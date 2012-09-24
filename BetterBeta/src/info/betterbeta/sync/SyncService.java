package info.betterbeta.sync;

import info.betterbeta.ProgressBarListActivity;
import info.betterbeta.area.AreaHelper;
import info.betterbeta.media.ImageHelper;
import info.betterbeta.media.MediaHelper;
import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Media;
import info.betterbeta.model.Problem;
import info.betterbeta.model.State;
import info.betterbeta.problem.ProblemHelper;
import info.betterbeta.provider.BetaProvider;
import info.betterbeta.xml.AreaXmlHandler;
import info.betterbeta.xml.MediaXmlHandler;
import info.betterbeta.xml.ProblemXmlHandler;
import info.betterbeta.xml.PutResponseXmlHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The {@link LocalServiceController}
 * and {@link LocalServiceBinding} classes show how to interact with the
 * service.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
public class SyncService extends Service {
	
	public static final int PUT_AREA = 1;
	public static final int PUT_PROBLEM = 2;
	public static final int GET_AREA = 3;
	public static final int GET_PROBLEM = 4;

	public static final int SYNC_ALL_AREAS = 5;
	public static final int SYNC_ALL_PROBLEMS = 6;
	public static final int SYNC_ALL_MEDIA = 7;
	public static final int SYNC_ALL = 8;

	HttpClient httpClient;
	String deviceId;
	String md5DeviceId;
	
	private static final String baseUrl = "http://www.betterbeta.info/";
	private static final String addUpdateProblemUrl = "add_update_problem.php";
	private static final String addUpdateAreaUrl = "add_update_area.php";
	private static final String addUpdateMediaUrl = "add_update_media.php";
	private static final String areasUrl = "areas.php";
	private static final String problemsUrl = "problems.php";
	private static final String mediaUrl = "media.php";
	private static final String uploadPicUrl = "upload_pic.php";
	
	int action;
	long areaId;
	long problemId;
    Bundle extras;
    ProgressBarListActivity currentActivity;

    int progress = 0;
 //   int secondaryProgress = 0;
    String currentMessage = "";

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public SyncService getService() {
            return SyncService.this;
        }
    }
 // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
   
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
     
    public void registerActivity(ProgressBarListActivity activity){
    	this.currentActivity = activity;
    }
    
    final Handler mHandler = new Handler();
    
    public void postProgress(){
    	mHandler.post(mPostRunnable);
    }
    
    @Override
	public void onCreate() {
		super.onCreate();
		setForeground(true);
//		Toast.makeText(SyncService.this, "SyncService Created", Toast.LENGTH_SHORT).show();      
	}
    
    @Override
    public void onStart(Intent intent, int startId) {
    	extras = intent.getExtras();
		if (extras.getBoolean("start_sync")){
			setForeground(true);
			progress = 1;
	    	Thread thread = new Thread(syncRunnable);
	        thread.start();
		}
    	super.onStart(intent, startId);
    }
	
    //RUNNABLES - possibly replace with above handler and handleMessage(Message msg) method
     
    // Need handler for callbacks to the UI thread
    final Runnable mUpdateRunnable = new Runnable(){
    	@Override
    	public void run() {
    		if(progress < 90)
    			progress += 10;
    		if (progress < 40)
    			currentMessage = "syncing areas";
    		else if (progress > 40 && progress < 70)
    			currentMessage = "syncing problems";
    		else if (progress > 70)
    			currentMessage = "syncing media";
    		if (currentActivity != null)
    			currentActivity.updateProgress(progress, currentMessage);
    	}
    }; 

    // Need handler for callbacks to the UI thread
    final Runnable mPostRunnable = new Runnable(){
    	@Override
    	public void run() {
    		//progress = progress + 10;
    		if (progress < 30)
    			currentMessage = "syncing areas";
    		else if (progress > 30 && progress < 70)
    			currentMessage = "syncing problems";
    		else if (progress > 70)
    			currentMessage = "syncing media";
    		if (currentActivity != null)	
    			currentActivity.updateProgress(progress, currentMessage);
    	}
    }; 
    
    
    final Runnable mFinishRunnable = new Runnable(){
    	@Override
    	public void run() {
	    		progress = 0;
	    		if (currentActivity != null){
	        		currentActivity.progressDone();
	    		}
           	}
    };
    
	private Runnable syncRunnable = new Runnable(){
		
		@Override
		public void run() {
			httpClient = new DefaultHttpClient();
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			deviceId = tm.getDeviceId();
			
			byte[] deviceBytes = deviceId.getBytes();
			try {
				
				MessageDigest algorithm = MessageDigest.getInstance("MD5");
				algorithm.reset();
				algorithm.update(deviceBytes);
				byte messageDigest[] = algorithm.digest();
			            
				StringBuffer hexString = new StringBuffer();
				for (int i=0;i<messageDigest.length;i++) {
					hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
				}
				md5DeviceId = hexString.toString();

			} catch (NoSuchAlgorithmException e) {
				md5DeviceId = "";
				e.printStackTrace();
			}

			if (action == -1 || action == 0) {
				action = extras != null ? extras.getInt("action") : -1;
			}

			System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver"); 
			switch (action) {
			case PUT_AREA:
				if (areaId == -1 || areaId == 0) {
					areaId = extras != null ? extras.getLong("area_id") : -1;
				}
				putArea(AreaHelper.inflate(areaId, getContentResolver(), IdType.LOCAL));
				break;
			case PUT_PROBLEM:
				if (problemId == -1 || problemId == 0) {
					problemId = extras != null ? extras.getLong("problem_id") : -1;
				}
				putProblem(ProblemHelper.inflate(problemId, getContentResolver(), IdType.LOCAL));
				break;
			case SYNC_ALL_AREAS:
				updateAllAreas();
				break;

			case SYNC_ALL_PROBLEMS:
				updateAllProblems();
				break;

			case SYNC_ALL_MEDIA:
				updateAllProblems();
				break;
			case SYNC_ALL:
				mHandler.post(mUpdateRunnable);
				updateAllAreas();
				mHandler.post(mUpdateRunnable);
				updateAllProblems();
				mHandler.post(mUpdateRunnable);
				updateAllMedia();
				mHandler.post(mFinishRunnable);	
				setForeground(false);
				break;
			}		
		}

	};

	public boolean putArea(Area area) {
		try {
			boolean update = false;

			if (area.getIdType() == IdType.LOCAL)
				AreaHelper.setMasterFromLocalParentArea(area, getContentResolver());
			String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(area.getName(), "UTF-8") + "&" +
			URLEncoder.encode("details", "UTF-8") + "=" + URLEncoder.encode(area.getDetails(), "UTF-8") + "&" +
			URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(area.getLongitude(), "UTF-8") + "&" +
			URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(area.getLatitude(), "UTF-8") + "&" +
			URLEncoder.encode("date_added", "UTF-8") + "=" + URLEncoder.encode(BetaProvider.mysqlDateFormater.format(area.getDateAdded()), "UTF-8") + "&" +
			URLEncoder.encode("date_modified", "UTF-8") + "=" + URLEncoder.encode(BetaProvider.mysqlDateFormater.format(area.getDateModified()), "UTF-8") + "&" +
			URLEncoder.encode("details", "UTF-8") + "=" + URLEncoder.encode(area.getDetails(), "UTF-8") + "&" +
			URLEncoder.encode("parent_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(area.getParent()), "UTF-8") + "&" +
			URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(this.md5DeviceId), "UTF-8");
			if(area.getState() == State.DIRTY && area.getMasterId() > 0){
				data += data + "&" + URLEncoder.encode("master_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(area.getMasterId()), "UTF-8");
				update = true;
			}
			URL url = new URL(baseUrl + addUpdateAreaUrl);
			
			PutResponseXmlHandler putHandler = new PutResponseXmlHandler();
			String response = doPostRequest(data, url);

		//	Log.d("respones from area put:", response);
			try {
				XMLReader xr = XMLReaderFactory.createXMLReader();
				xr.setContentHandler(putHandler);
				xr.parse(new InputSource(new StringReader(response)));
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (putHandler.isSuccess()){
				if(!update){
					updateProblemsWithAreaMaster(area.getId(), Long.valueOf(putHandler.getId()));
					area.setMasterId(Long.valueOf(putHandler.getId()));
				}
				area.setState(State.CLEAN);
				AreaHelper.updateArea(area, getContentResolver());
			}
			
			// get back ID and 
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean putProblem(Problem problem) {
		try{
			boolean update = false;
			if (problem.getIdType() == IdType.LOCAL) //IdType refers to parent area ID type
				ProblemHelper.setMasterFromLocalArea(problem, getContentResolver());
			String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(problem.getName(), "UTF-8") + "&" +
			URLEncoder.encode("details", "UTF-8") + "=" + URLEncoder.encode(problem.getDetails(), "UTF-8") + "&" +
			URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(problem.getLongitude(), "UTF-8") + "&" +
			URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(problem.getLatitude(), "UTF-8") + "&" +
			URLEncoder.encode("date_added", "UTF-8") + "=" + URLEncoder.encode(BetaProvider.mysqlDateFormater.format(problem.getDateAdded()), "UTF-8") + "&" +
			URLEncoder.encode("date_modified", "UTF-8") + "=" + URLEncoder.encode(BetaProvider.mysqlDateFormater.format(problem.getDateModified()), "UTF-8") + "&" +
			URLEncoder.encode("details", "UTF-8") + "=" + URLEncoder.encode(problem.getDetails(), "UTF-8") + "&" +
			URLEncoder.encode("area_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(problem.getArea()), "UTF-8") + "&" +
			URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(this.md5DeviceId), "UTF-8");
			if(problem.getState() == State.DIRTY && problem.getMasterId() > 0){
				data += data + "&" + URLEncoder.encode("master_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(problem.getMasterId()), "UTF-8");
				update = true;
			}
			URL url = new URL(baseUrl + addUpdateProblemUrl);
			
			PutResponseXmlHandler putHandler = new PutResponseXmlHandler();
			
			String response = doPostRequest(data, url);	
			
		//	Log.d("respones from problem put:", response);
			
			try {
				XMLReader xr = XMLReaderFactory.createXMLReader();
				xr.setContentHandler(putHandler);
				xr.parse(new InputSource(new StringReader(response)));
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (putHandler.isSuccess()){
				if(!update){
					updateMediaWithProblemMaster(problem.getId(), Long.valueOf(putHandler.getId()));
					problem.setMasterId(Long.valueOf(putHandler.getId()));
				}
				problem.setState(State.CLEAN);
				ProblemHelper.updateProblem(problem, getContentResolver());
			}
			
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

		return true;
	}


	public boolean putMedia(Media media) {
		try{		
			if (media.getType() == Media.TYPE_URI_PIC)
			{
				HttpURLConnection conn = null;
				DataOutputStream dos = null;
				DataInputStream inStream = null;	
				String response = "";	 
				String exsistingFileName = "/uploadfile.jpg";
				String lineEnd = "\r\n";
				String twoHyphens = "--";
				String boundary =  "*****";
				int bytesRead, bytesAvailable, bufferSize;
				byte[] buffer;
				int maxBufferSize = 1*1024*1024;
				String urlString = baseUrl + uploadPicUrl;
				
				try	{
					   //------------------ CLIENT REQUEST	 
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
										ImageHelper.getBytes(ImageHelper.resize(
												android.provider.MediaStore.Images.Media.getBitmap(
														getContentResolver(), Uri.parse(media.getPath())))));
					URL url = new URL(urlString);
					conn = (HttpURLConnection) url.openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setUseCaches(false);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
					dos = new DataOutputStream( conn.getOutputStream() );
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
															+ exsistingFileName +"\"" + lineEnd);
					dos.writeBytes(lineEnd);
				//	Log.e("BetterBeta","Headers are written");
					
					// create a buffer of maximum size
					bytesAvailable = byteArrayInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					buffer = new byte[bufferSize];
					
					// read file and write it into form...
					bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);
					
					while (bytesRead > 0){
						dos.write(buffer, 0, bufferSize);
						bytesAvailable = byteArrayInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);
					}
					
					// send multipart form data necesssary after file data...
					
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
					
					// close streams
				//	Log.e("BetterBeta","File is written");
					byteArrayInputStream.close();
					dos.flush();
					dos.close();
				}
				catch (MalformedURLException ex){
				Log.e("BetterBeta", "error: " + ex.getMessage(), ex);
				}
				
				catch (IOException ioe){
				Log.e("BetterBeta", "error: " + ioe.getMessage(), ioe);
				}
					
					
				//------------------ read the SERVER RESPONSE
				try {
					inStream = new DataInputStream ( conn.getInputStream() );
					String str;
					while (( str = inStream.readLine()) != null)
					{
						response += str;
					}
					inStream.close();	
				}
				catch (IOException ioex){
					Log.e("BetterBeta", "error: " + ioex.getMessage(), ioex);
				}
					
				PutResponseXmlHandler putHandler = new PutResponseXmlHandler();
				
				try {
					XMLReader xr = XMLReaderFactory.createXMLReader();
					xr.setContentHandler(putHandler);
					xr.parse(new InputSource(new StringReader(response)));
				} catch (SAXException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
						e.printStackTrace();
			}
			if (putHandler.isSuccess()){
				ContentValues contentValues = new ContentValues();
				contentValues.put(BetaProvider.KEY_MEDIA_PATH, baseUrl + putHandler.getId());
				contentValues.put(BetaProvider.KEY_MEDIA_TYPE, Media.TYPE_URL_PIC);
				getContentResolver().update(Media.CONTENT_URI, contentValues, 
						BetaProvider.KEY_MEDIA_ID + " = ?",
							new String []{String.valueOf(media.getId())});
				media.setType(Media.TYPE_URL_PIC);
				media.setPath(baseUrl + putHandler.getId());
			}
		}
	
	    boolean update = false;
		if (media.getIdType() == IdType.LOCAL && media.getArea() > 0)
			MediaHelper.setMasterFromLocalArea(media, getContentResolver());
		if (media.getIdType() == IdType.LOCAL && media.getProblem() > 0)
			MediaHelper.setMasterFromLocalProblem(media, getContentResolver());
		String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(media.getName(), "UTF-8") + "&" +
		URLEncoder.encode("details", "UTF-8") + "=" + URLEncoder.encode(media.getDetails(), "UTF-8") + "&" +
		URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(media.getLongitude(), "UTF-8") + "&" +
		URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(media.getLatitude(), "UTF-8") + "&" +
		URLEncoder.encode("date_added", "UTF-8") + "=" + URLEncoder.encode(BetaProvider.mysqlDateFormater.format(media.getDateAdded()), "UTF-8") + "&" +
		URLEncoder.encode("date_modified", "UTF-8") + "=" + URLEncoder.encode(BetaProvider.mysqlDateFormater.format(media.getDateModified()), "UTF-8") + "&" +
		URLEncoder.encode("details", "UTF-8") + "=" + URLEncoder.encode(media.getDetails(), "UTF-8") + "&" +
		URLEncoder.encode("area_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(media.getArea()), "UTF-8") + "&" +
		URLEncoder.encode("problem_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(media.getProblem()), "UTF-8") + "&" +
		URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(this.md5DeviceId), "UTF-8") + "&" +
		URLEncoder.encode("path", "UTF-8") + "=" + URLEncoder.encode(media.getPath(), "UTF-8") + "&" +
		URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(media.getType()), "UTF-8");
		if(media.getState() == State.DIRTY && media.getMasterId() > 0){
			data += data + "&" + URLEncoder.encode("master_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(media.getMasterId()), "UTF-8");
			update = true;
		}
		URL url = new URL(baseUrl + addUpdateMediaUrl);
		
		PutResponseXmlHandler putHandler = new PutResponseXmlHandler();
		
		String response = doPostRequest(data, url);	
		
	//	Log.d("respones from media put:", response);
		
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(putHandler);
			xr.parse(new InputSource(new StringReader(response)));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (putHandler.isSuccess()){
			if(!update)
				media.setMasterId(Long.valueOf(putHandler.getId()));
			media.setState(State.CLEAN);
			MediaHelper.updateMedia(media, getContentResolver());
		}
			
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return true;
	}

	public boolean updateAllAreas() {
		// send areas with flag = NEW or UPDATED to server, wipe and get all areas
		
		for(Area a : AreaHelper.getNewAndDirtyAreas(getContentResolver())){
			putArea(a);
		}
		String url = baseUrl + areasUrl + "?user=" + md5DeviceId;
		String responseString = doGetRequest(url);

		mHandler.post(mUpdateRunnable);
		AreaXmlHandler areaXmlHandler = new AreaXmlHandler();
		
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(areaXmlHandler);
			xr.parse(new InputSource(new StringReader(responseString)));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//wipe areas after request has been sent, response receved and areas parsed without error
		
		//TODO: make sure areaXmlHandler.getAreas() is not empty!!
		
		getContentResolver().delete(Area.CONTENT_URI, null, null);
		

		mHandler.post(mUpdateRunnable);
		
		for (Area a : areaXmlHandler.getAreas()){
			AreaHelper.addNewArea(a, getContentResolver());
		}
		
	//	Log.d("xml areas: ", responseString);
		return true;
	}
	
	
	public boolean updateAllProblems() {
		// send problems with flag = NEW or UPDATED to server, wipe and get all areas
		
		for(Problem p : ProblemHelper.getNewAndDirtyProblems(getContentResolver())){
				putProblem(p);
		}
		
		String url = baseUrl + problemsUrl + "?user=" + md5DeviceId;
		String responseString = doGetRequest(url);

		mHandler.post(mUpdateRunnable);
		ProblemXmlHandler problemXmlHandler = new ProblemXmlHandler();
		
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(problemXmlHandler);
			xr.parse(new InputSource(new StringReader(responseString)));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//wipe areas after request has been sent, response recieved and areas parsed without error

		//TODO: make sure areaXmlHandler.getProblems() is not empty!!
		
		getContentResolver().delete(Problem.CONTENT_URI, null, null);

		mHandler.post(mUpdateRunnable);
		for (Problem p : problemXmlHandler.getProblems()){
			ProblemHelper.addNewProblem(p, getContentResolver());
		}
		
	//	Log.d("xml problems: ", responseString);
		return true;
	}

	
	public boolean updateAllMedia() {
		// send problems with flag = NEW or UPDATED to server, wipe and get all areas
		
		for(Media m : MediaHelper.getNewAndDirtyMedias(getContentResolver())){
				putMedia(m);
		}
		
		
		String url = baseUrl + mediaUrl + "?user=" + md5DeviceId;
		String responseString = doGetRequest(url);

		mHandler.post(mUpdateRunnable);
	//	Log.d("BetterBeta", "media get:" + responseString);
		MediaXmlHandler mediaXmlHandler = new MediaXmlHandler();
		
		try {
			XMLReader xr = XMLReaderFactory.createXMLReader();
			xr.setContentHandler(mediaXmlHandler);
			xr.parse(new InputSource(new StringReader(responseString)));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//wipe areas after request has been sent, response recieved and areas parsed without error

		//TODO: make sure mediaXmlHandler.getMedia() is not empty!!
		
		getContentResolver().delete(Media.CONTENT_URI, null, null);

		mHandler.post(mUpdateRunnable);
		for (Media m : mediaXmlHandler.getMedia()){
			MediaHelper.addNewMedia(m, getContentResolver());
		}
		
		return true;
	}

	private String doPostRequest(String data, URL url) {
		try {

			String type = "application/x-www-form-urlencoded";

			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("METHOD", "POST");
			conn.setRequestProperty("Content-Type", type);
			conn.setRequestProperty("Content-Length", String.valueOf(data.length()));

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();
			wr.close();
			// Get the response

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			String response = "";
			while ((line = rd.readLine()) != null) {
				response += line;
			}

			rd.close();
			return response;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "<response><result>failure</result><reason>Failed to upload, Host not found likely</reason></response>";
		}

	}

	private String doGetRequest(String url) {
		try {
			HttpGet method = new HttpGet(new URI(url));
			HttpResponse httpResponse = httpClient.execute(method);
			if (httpResponse != null) {
				return getResponse(httpResponse.getEntity());
			} else
				return "";

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	private String getResponse(HttpEntity entity) {
		String response = "";
		try {
			int length = (int) entity.getContentLength();
			if (length <= 0)
				length = 2000;
			StringBuffer sb = new StringBuffer(length);
			InputStreamReader isr = new InputStreamReader(entity.getContent(), "UTF-8");
			char buff[] = new char[length];
			int cnt;
			
			while ((cnt = isr.read(buff, 0, length - 1)) > 0) {
				sb.append(buff, 0, cnt);
			}
			response = sb.toString();
			isr.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return response;
	}
	
	private boolean updateProblemsWithAreaMaster(long oldAreaId, long newMasterId){
		
		// must do multiple queries to loop through media for each problem.
		//do i? the media will take care of itself, it still point to the same problem
		
		Cursor c = getContentResolver().query(Problem.CONTENT_URI,
						new String[]{BetaProvider.KEY_PROBLEM_ID}, 
						BetaProvider.KEY_PROBLEM_AREA + " = ? AND " + BetaProvider.KEY_PROBLEM_ID_TYPE + " = ?",
						new String[]{String.valueOf(oldAreaId), String.valueOf(IdType.LOCAL)}, null);
		
		while (c.moveToNext()){
			ContentValues values = new ContentValues();
			values.put(BetaProvider.KEY_PROBLEM_AREA, newMasterId);
			values.put(BetaProvider.KEY_PROBLEM_ID_TYPE, IdType.MASTER);
			getContentResolver().update(Problem.CONTENT_URI,
							values, 
							BetaProvider.KEY_PROBLEM_ID + " = ?", 
							new String[]{String.valueOf(c.getLong(0))});
		}

		c.close();
		return true;
		
	}
	private boolean updateMediaWithProblemMaster(long oldProblemId, long newMasterId){
		
		// must do multiple queries to loop through media for each problem.
		//do i? the media will take care of itself, it still point to the same problem (except after the DELEEEETE)
		
		Cursor c = getContentResolver().query(Media.CONTENT_URI,
						new String[]{BetaProvider.KEY_MEDIA_ID}, 
						BetaProvider.KEY_MEDIA_PROBLEM + " = ? AND " + BetaProvider.KEY_MEDIA_ID_TYPE + " = ?",
						new String[]{String.valueOf(oldProblemId), String.valueOf(IdType.LOCAL)}, null);
		
		while (c.moveToNext()){
			ContentValues values = new ContentValues();
			values.put(BetaProvider.KEY_MEDIA_PROBLEM, newMasterId);
			values.put(BetaProvider.KEY_MEDIA_ID_TYPE, IdType.MASTER);
			int i = getContentResolver().update(Media.CONTENT_URI,
							values, 
							BetaProvider.KEY_MEDIA_ID + " = ?",
							new String[]{String.valueOf(c.getLong(0))});
			i++;
		}

		c.close();
		return true;
	}
	
	public int getProgress() {
		return this.progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getCurrentMessage() {
		return this.currentMessage;
	}

	public void setCurrentMessage(String currentMessage) {
		this.currentMessage = currentMessage;
	}
}

