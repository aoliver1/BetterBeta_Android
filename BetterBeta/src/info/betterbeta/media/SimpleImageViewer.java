package info.betterbeta.media;

import info.betterbeta.R;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class SimpleImageViewer extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  

		setContentView(R.layout.simple_image_view);
		String uri = savedInstanceState != null ? savedInstanceState.getString("uri") : null;
		Bundle extras = getIntent().getExtras();
		if (uri == null)
			uri = extras != null ? extras.getString("uri") : null;
			
		System.gc();
		
		try {
			((ImageView) findViewById(R.id.the_image_view)).setImageBitmap(ImageHelper.resize(android.provider.MediaStore.Images.Media.getBitmap(
															getContentResolver(), Uri.parse(uri))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		((ImageView) findViewById(R.id.the_image_view)).setImageBitmap(null);
		System.gc();
		super.onPause();
	}
}
