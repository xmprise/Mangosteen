package com.mangosteen.streaming;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class MjpegActivity extends Activity {
	/** Called when the activity is first created. */
	private Preview_mjpeg mPreview;
	int defaultCameraId;
	int port = 8555;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.e("MjpegActivity", "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        mPreview = new Preview_mjpeg(this, 20, 640, 480, port);
        setContentView(mPreview);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
//        Log.e("MjpegActivity", "onResume");
        // Open the default i.e. the first rear facing camera.
        
       
    }

    @Override
    protected void onPause() {
    	super.onPause();
//    	 Log.e("MjpegActivity", "onPause");

    }
    
    @Override
    protected void onStop() {
        super.onStop();
//        Log.e("MjpegActivity", "onStop");
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        Log.e("MjpegActivity", "onCreateOptionsMenu");
        // Inflate our menu which can gather user input for switching camera
//        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.camera_menu, menu);
        return true;
    }
}