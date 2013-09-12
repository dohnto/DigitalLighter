package com.example.digitallighterserver;

import android.app.Application;
import com.testflightapp.lib.TestFlight;

public class DLSApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		//Initialize TestFlight with your app token.
		TestFlight.takeOff(this, "0322b950-8564-423a-835d-476531c1ab23");  
	}
}
