package com.example.digitallighterserver;

import android.app.Application;
import android.content.Context;

public class DLSApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		//Initialize TestFlight with your app token.
		//sTestFlight.takeOff(this, "0322b950-8564-423a-835d-476531c1ab23");  
	}
	
	static private DLSApplication ctx = new DLSApplication();
	
	static public Context getContext() {
		return ctx.getContext(); 
	}
}