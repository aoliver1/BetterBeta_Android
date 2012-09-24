package info.betterbeta;

import info.betterbeta.sync.SyncService;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

public abstract class ProgressBarListActivity extends ListActivity {

    protected boolean isSyncing;
    private SyncService mBoundService;
    private ProgressBar progressHorizontal;
    protected boolean syncOnConnect = false;
    protected boolean updateCount = false;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((SyncService.LocalBinder)service).getService();
            mBoundService.registerActivity(ProgressBarListActivity.this);
            if(mBoundService.getProgress() > 0){
            	//updateProgress(mBoundService.getProgress(), mBoundService.getSecondaryProgress(), "syncing");
            	mBoundService.postProgress();
            	setProgressBarVisibility(true);
            	isSyncing = true;
       // 		Toast.makeText(ProgressBarListActivity.this, "service bound, issyncing true", Toast.LENGTH_SHORT).show();  
            }
            else{
            	isSyncing = false;
      //  		Toast.makeText(ProgressBarListActivity.this, "service bound, issyncing FALSE", Toast.LENGTH_SHORT).show();  
            	resetBar();
        		mBoundService.startService(new Intent(ProgressBarListActivity.this, SyncService.class).
        															putExtra("action", SyncService.SYNC_ALL).
        															putExtra("start_sync", false));
            }
            if(syncOnConnect){
            	mBoundService.startService(new Intent(ProgressBarListActivity.this, SyncService.class).
						putExtra("action", SyncService.SYNC_ALL).
						putExtra("start_sync", true));
            	mBoundService.postProgress();
            	syncOnConnect = false;
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };
    
    public abstract void syncComplete();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		
	}
	//callback called from SyncService
	public void updateProgress(int progress, String message){
		setProgress(progress * 100);
		setTitle("Better Beta - " + message);
		setProgressBarVisibility(true); 
 	//	Toast.makeText(ProgressBarListActivity.this, "updating progress", Toast.LENGTH_SHORT).show();  
	}
	
	public void resetBar(){

        progressHorizontal = new ProgressBar(this);
        progressHorizontal.setProgress(0);
        progressHorizontal.setSecondaryProgress(0);
		isSyncing = false;
		setTitle("Better Beta");
        setProgressBarVisibility(false); 
	}
	public void progressDone(){
		resetBar();
		syncComplete();
		Toast.makeText(this, R.string.sync_service_complete, Toast.LENGTH_SHORT).show();  
	}
  
    protected void syncAll(){ 	
    	isSyncing = true; 
		mBoundService.registerActivity(ProgressBarListActivity.this);
		mBoundService.startService(new Intent(ProgressBarListActivity.this, SyncService.class).
															putExtra("action", SyncService.SYNC_ALL).
															putExtra("start_sync", true));
		mBoundService.postProgress();
		setProgressBarVisibility(true);  
    }
    @Override
    protected void onResume() {

    	resetBar();
    	bindService(new Intent(ProgressBarListActivity.this, 
    			SyncService.class), mConnection, Context.BIND_AUTO_CREATE);	 
 
		super.onResume();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	unbindService(mConnection);
    	setProgressBarVisibility(false);
    	super.onPause();
    }
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    }
}
