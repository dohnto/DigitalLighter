package com.example.digitallighterserver;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.lightdetector.CameraActivity;
import com.example.lightdetector.ColorManager;
import com.example.lightdetector.ColorMappingPair;

public class SettingsActivity extends Activity implements OnItemClickListener, OnClickListener {

	// FPS
	EditText fps;

	// CHECKBOXES
	CheckBox c_white;
	CheckBox c_blue;
	CheckBox c_green;
	CheckBox c_red;
	CheckBox c_orange;
	CheckBox c_mangeta;

	// PLAYLIST
	ListView playlist;
	ArrayAdapter<String> playAdapter;

	// DIM
	Spinner dimensions;
	ArrayAdapter<String> dataAdapter;

	// BUTTON
	Button btnContinue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		// FPS
		fps = (EditText) findViewById(R.id.fps);
		fps.setText("" + Configuration.FRAME_RATE);

		// CHECKBOXES
		c_blue = (CheckBox) findViewById(R.id.check_Blue);
		c_white = (CheckBox) findViewById(R.id.check_White);
		c_green = (CheckBox) findViewById(R.id.check_Green);
		c_red = (CheckBox) findViewById(R.id.check_Red);
		c_orange = (CheckBox) findViewById(R.id.check_Orange);
		c_mangeta = (CheckBox) findViewById(R.id.check_Magenta);

		// PLAYLIST
		playlist = (ListView) findViewById(R.id.playlist);
		playlist.setOnItemClickListener(this);
		playAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				new ArrayList<String>());
		playlist.setAdapter(playAdapter);

		// DIMENSIONS
		loadDimensions();

		// CONTINUE
		btnContinue = (Button) findViewById(R.id.btn_continue_settings);
		btnContinue.setOnClickListener(this);
	}

	private void loadDimensions() {
		dimensions = (Spinner) findViewById(R.id.dimensions);
		try {
			String[] dim = getAssets().list("");
			dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			dimensions.setAdapter(dataAdapter);
			for (int i = 0; i < dim.length; i++) {
				if (dim[i].contains("x"))
					dataAdapter.add(dim[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		dimensions.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// set proper dimensions to the config variables
				int[] newDimensions = parseDimensionsFromPath(dataAdapter.getItem(arg2));
				Configuration.TILES_X = newDimensions[0];
				Configuration.TILES_Y = newDimensions[1];

				// SET PROPER PLAYLISTS TO LISTVIEW
				playAdapter.clear();

				String[] playLists;
				try {
					String path = Configuration.TILES_X + "x" + Configuration.TILES_Y;
					playLists = getAssets().list(path);
					for (int i = 0; i < playLists.length; i++) {
						playAdapter.add(playLists[i]);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	// WORK WITH MEDIA PLAYER ITEM CLICK
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		Configuration.MEDIA_SOURCE = Configuration.TILES_X + "x" + Configuration.TILES_Y + "/"
				+ playAdapter.getItem(arg2);

		Toast.makeText(SettingsActivity.this, Configuration.MEDIA_SOURCE + " media Selected",
				Toast.LENGTH_SHORT).show();
	}

	public static int[] parseDimensionsFromPath(String path) {
		int[] dimensions = new int[2];
		if (!path.contains("x"))
			return dimensions;

		String[] dimensionsText = path.split("x");

		dimensions[0] = Integer.parseInt(dimensionsText[0]);
		dimensions[1] = Integer.parseInt(dimensionsText[1]);

		return dimensions;
	}

	// WORK WITH BUTTON CLICK
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_continue_settings:

			// SAVE NEW FPS
			Configuration.FRAME_RATE = Integer.parseInt(fps.getText().toString());

			// SAVE COLORS
			Configuration.RARE_COLORS_TREE = new ArrayList<ColorMappingPair>();
			if (c_blue.isChecked())
				Configuration.RARE_COLORS_TREE.add(new ColorMappingPair(ColorManager
						.getHexColor(ColorManager.BLUE)));
			if (c_green.isChecked())
				Configuration.RARE_COLORS_TREE.add(new ColorMappingPair(ColorManager
						.getHexColor(ColorManager.DARK_GREEN), ColorManager.getHexColor(ColorManager.GREEN)));
			if (c_mangeta.isChecked())
				Configuration.RARE_COLORS_TREE.add(new ColorMappingPair(ColorManager
						.getHexColor(ColorManager.MAGENTA)));
			if (c_orange.isChecked())
				Configuration.RARE_COLORS_TREE.add(new ColorMappingPair(ColorManager
						.getHexColor(ColorManager.ORANGE)));
			if (c_red.isChecked())
				Configuration.RARE_COLORS_TREE.add(new ColorMappingPair(ColorManager
						.getHexColor(ColorManager.DARK_RED)));
			if (c_white.isChecked())
				Configuration.RARE_COLORS_TREE.add(new ColorMappingPair(ColorManager
						.getHexColor(ColorManager.WHITE)));

			// START CAMERA ACTIVITY
			startActivity(new Intent(SettingsActivity.this, CameraActivity.class));
			break;
		}

	}
}
