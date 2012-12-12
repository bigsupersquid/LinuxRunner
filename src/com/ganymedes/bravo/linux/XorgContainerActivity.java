package com.ganymedes.bravo.linux; 
 

import com.ganymedes.bravo.linux.XorgThread.ORIENTATION;
  
import android.app.Activity;  
import android.content.res.Configuration;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent; 
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager; 
import android.widget.TextView;
import android.os.Bundle; 
import android.os.PowerManager;
 
public class XorgContainerActivity extends Activity {
	
	XorgThread xorgthread;
	PowerManager.WakeLock wl;
	private boolean resuming; 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
 
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

    	setContentView(R.layout.main);
    	 
        Log.v(getLocalClassName(), "Activity Created");
         
        xorgthread = ((LinuxInAndroid)getApplicationContext()).getThread();
        wl = ((LinuxInAndroid)getApplicationContext()).getWakeLock();
       
        if (xorgthread != null)
        {
        	wl.acquire();
 
        	if((savedInstanceState ==null) || (savedInstanceState.getBoolean("Running") != true))
        	{
        		resuming =false;
        		TextView txt = (TextView) findViewById(R.id.messageText);
            	txt.setText("Starting Enlightenment WM. Please Stand By...");
            	
        		xorgthread.start();
        	} else
        	{
        		resuming=true;
        		Log.v(getLocalClassName(), "Activity Created but without starting X again!");
        	}
        }
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
    	Log.v(getLocalClassName(), "Config Changed!");
    	TextView txt = (TextView) findViewById(R.id.messageText);
    	txt.setText("Updating the screen..."); 
    	
    	super.onConfigurationChanged(newConfig);
    	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int or = display.getRotation();
        Log.v(getLocalClassName(), "Orientation is:" + or);
        XorgThread.ORIENTATION orient = (or == Surface.ROTATION_0 ? ORIENTATION.Horizontal : ( or== Surface.ROTATION_90 ? ORIENTATION.Clockwise : ORIENTATION.CounterClockwise ) );
        
        if(xorgthread != null)
        	xorgthread.rotateX(orient);

    }
    public void onPause()
    {
    	TextView txt = (TextView) findViewById(R.id.messageText);
    	txt.setText("Pausing the session...");
    	super.onPause();
    	Log.v(getLocalClassName(), "Activity Paused");
    	if(xorgthread != null)
    		xorgthread.pause();
    	wl.release();
    	resuming = true;
    }
    public void onSaveInstanceState(Bundle outState)
    {
    	outState.putBoolean("Running", true);
    	super.onSaveInstanceState(outState);
    }
    public void onBackPressed()
    {
    	TextView txt = (TextView) findViewById(R.id.messageText);
    	txt.setText("Terminating the session...");
    	if (xorgthread != null)
    		xorgthread.terminate();

    	wl.release();
    	super.onBackPressed();
    	System.exit(0);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        // From the docs:
        // "Note that in order to receive this callback, someone in the event [chain]
        // must return true from onKeyDown(int, KeyEvent) and call startTracking() on the event."
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // Override default handling, and don't pop up the soft keyboard.
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onResume()
    {
    	if(resuming)
    	{
    		TextView txt = (TextView) findViewById(R.id.messageText);
    		txt.setText("Resuming the session...");
    	}
    	wl.acquire();
    	super.onResume();
    	Log.v(getLocalClassName(), "Activity Resumed");
    	if(xorgthread != null)
    		xorgthread.restart();
    } 
    public void onWindowFocusChanged(boolean hasFocus)
    {
    	super.onWindowFocusChanged(hasFocus);
    	if(hasFocus)
    	{
    		Log.v(getLocalClassName(), "Activity Got Window Focus");
    		
    		if((xorgthread != null)) 
    		{
    			xorgthread.Refresh();
    		}
    	}
    	else
    		Log.v(getLocalClassName(), "Activity Lost Window Focus");
    }
    
}