package com.example.digitallighter;

import android.app.Application;

public class DLApplication extends Application {
	 @Override
	 public void onCreate() {
		 super.onCreate();
		 // Initialize TestFlight with your app token.
		// TestFlight.takeOff(this, "70735c1d-2f5e-4ce1-a282-865a5eef206e");
	 }
}
