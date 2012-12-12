package com.ganymedes.bravo.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.util.Log; 

public class XorgThread extends Thread implements Runnable {
 
	enum ORIENTATION { Horizontal, CounterClockwise, UpsideDown, Clockwise };
 
	private boolean state; // 0 - stopped; 1 - running
	Process runner;
	public XorgThread(Process p)
	{ 
		 runner = p;
	} 
	public int getPid()
	{
		try
		{
			Process getpid = Runtime.getRuntime().exec("pidof X");
			getpid.waitFor();
			BufferedReader reader = new BufferedReader( new InputStreamReader(getpid.getInputStream()));
			return Integer.valueOf( reader.readLine() );
		}
		catch (IOException exp) {
			return -1;
		}
    	catch (InterruptedException e2) { 
    		return -1;
    	}
		catch (NumberFormatException e3)
		{
			return -1;
		}
	}
	public void run() { 
		try
        {
			Log.v("Xorg Thread", "Thread is Running");
			if ( getPid() < 0 )
			{ 
				runner.getOutputStream().write("startx & \n".getBytes()); 
				state=true;
			} else
			{
				// X is running, but stopped. Restart it.
				restart();
			}
        }
        catch (IOException e)  { }

	}
	public void restart()
	{
		int pid= getPid();
		if (pid > 0)
		{
			Log.v("Xorg Thread", "X at " + pid + " is being restarted");
			try
			{
				String command;
				command="/system/xbin/kill -CONT " + pid;
				command+=" \n";
				runner.getOutputStream().write(command.getBytes()); 
				runner.getOutputStream().flush(); 
				state=true;
			}
			catch (IOException e) {}
		}
	}
	public void pause()
	{
		int pid= getPid();
		if (pid > 0)
		{
			Log.v("Xorg Thread", "X at " + pid + " is being paused");
			try
			{
				String command;
				command="/system/xbin/kill -STOP " + pid;
				command+=" \n";
				runner.getOutputStream().write(command.getBytes()); 
				runner.getOutputStream().flush(); 
				state=false;
			}
			catch (IOException e) {}
		}
	}
	public void terminate()
	{ 
		int pid= getPid();
		if (pid > 0)
		{
			Log.v("Xorg Thread", "X at " + pid + " is being terminated");
			try
			{
				String command;
				command="/system/xbin/kill -TERM " + pid; 
				command+=" \n";
				runner.getOutputStream().write(command.getBytes()); 
				runner.getOutputStream().flush(); 
				state=false;
			}
			catch (IOException e) {}
		}
	}
	public void rotateX(ORIENTATION neworient)
	{
		if(!state)
			return;
		int o_number = (neworient == ORIENTATION.Clockwise ? 3 : ( neworient == ORIENTATION.CounterClockwise ? 1 : (neworient == ORIENTATION.UpsideDown ? 2 : 0) ) );
		Log.v("Xorg Thread", "Rotating the X");
		try
		{
			String command;
			command=". $ENV; /data/bin/xrandr -o " + o_number + "; " ;
			command+="xinput set-prop Trackball \"Evdev Axes Swap\" ";
			command+= (o_number == 0 ? " 0 ; " : " 1 ; ");
			command+="xinput set-prop Trackball \"Evdev Axis Inversion\" ";
			command+= (o_number == 0? " 0 0; " : (o_number == 3 ? " 0 1; " : " 1 0; " ));
			command+= "  xrefresh; \n";
			runner.getOutputStream().write(command.getBytes()); 
			runner.getOutputStream().flush(); 
		}
		catch (IOException e) {}

	}
	public boolean isRunning()
	{
		return state;
	}
	public void Refresh()
	{
		if ((getPid() < 0) || (!state))   
			return;
		try
		{
			Log.v("Xorg Thread", "Refresh!");
			String command=". $ENV;  sleep 1 ; xrandr -o 1; xrandr -o 0; xrefresh \n";
			runner.getOutputStream().write(command.getBytes());
		}
		catch (IOException ex) {}
	}
}
