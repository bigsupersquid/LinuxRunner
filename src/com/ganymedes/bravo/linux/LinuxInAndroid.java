package com.ganymedes.bravo.linux;

import java.io.IOException;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class LinuxInAndroid extends Application {

	private XorgThread xorgthread;
	private PowerManager.WakeLock wakelock;
	private Process runningX;

	public XorgThread getThread()
	{
		return xorgthread;
	}
	
	public WakeLock getWakeLock()
	{
		return wakelock;
	}
	@Override
	public void onCreate()
	{
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
		wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Xorg Running Lock");


		try {
			runningX = Runtime.getRuntime().exec("su");
			runningX.getOutputStream().write(". $ENV; sleep 4; \n".getBytes());
			runningX.getOutputStream().flush();
			
	    	xorgthread = new XorgThread(runningX);
		} catch (IOException e) { 
			System.exit(1);
		}

    	super.onCreate();
 
	}

}
