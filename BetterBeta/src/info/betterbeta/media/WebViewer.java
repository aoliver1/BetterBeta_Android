package info.betterbeta.media;

import info.betterbeta.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

public class WebViewer extends Activity {

	String url;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.web_viewer);

		WebView webView = (WebView) findViewById(R.id.web_view);

		url = savedInstanceState != null ? savedInstanceState.getString("url") : null;
		Bundle extras = getIntent().getExtras();

		if (url == null)
			url = extras != null ? extras.getString("url") : null;

		webView.loadUrl(url);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putString("url", url);
	}

}
