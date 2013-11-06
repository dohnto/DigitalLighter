package com.example.digitallighterserver;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnCheckedChangeListener, OnItemClickListener {

	// FPS
	EditText fps;

	// CHECKBOXES
	CheckBox c_white;
	CheckBox c_blue;
	CheckBox c_green;
	CheckBox c_red;
	CheckBox c_orange;
	CheckBox c_mangeta;

	ListView playlist;
	Spinner dimensions;

	// TOAST
	private Toast mToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		// TOAST
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

		// FPS
		fps = (EditText) findViewById(R.id.fps);
		fps.setText("" + Configuration.FRAME_RATE);

		// CHECKBOXES
		c_blue = (CheckBox) findViewById(R.id.check_Blue);
		c_blue.setOnCheckedChangeListener(this);
		c_white = (CheckBox) findViewById(R.id.check_White);
		c_white.setOnCheckedChangeListener(this);
		c_green = (CheckBox) findViewById(R.id.check_Green);
		c_green.setOnCheckedChangeListener(this);
		c_red = (CheckBox) findViewById(R.id.check_Red);
		c_red.setOnCheckedChangeListener(this);
		c_orange = (CheckBox) findViewById(R.id.check_Orange);
		c_orange.setOnCheckedChangeListener(this);
		c_mangeta = (CheckBox) findViewById(R.id.check_Magenta);
		c_mangeta.setOnCheckedChangeListener(this);

		// PLAYLIST
		playlist = (ListView) findViewById(R.id.playlist);
		playlist.setOnItemClickListener(this);

		// DIMENSIONS
		dimensions = (Spinner) findViewById(R.id.dimensions);
		try {
			String[] dim = getAssets().list("");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

}
