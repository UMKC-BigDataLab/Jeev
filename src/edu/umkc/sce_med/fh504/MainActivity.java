package edu.umkc.sce_med.fh504;

import java.util.Calendar;
import java.util.Random;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;


import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	private Vaccines table;
	private PendingIntent pendingIntent;


	private CharSequence[] _options;
	private boolean[] _selections;
	private Button _optionsButton;
	private AlarmManager alarmManager;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final DBAdapter db = new DBAdapter(this);
		alarmManager=null;
		//smsr = new SMSReceiver(db);
		Log.v("ImmuneWorldDB", "STARTED:");

		table = new Vaccines();
		_options = table.sName;
		_selections = new boolean[_options.length];
		_optionsButton = (Button) findViewById(R.id.btnSelectVaccines);
		_optionsButton.setOnClickListener(new ButtonClickHandler());

		//registerReceiver(smsr, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		

		Button btnStartSmsScan = (Button) findViewById(R.id.btnStartSmsScan);
		Button btnStopSmsScan = (Button) findViewById(R.id.btnStopSmsScan);

		btnStartSmsScan.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent myIntent = new Intent(MainActivity.this,MyService.class);
				pendingIntent = PendingIntent.getService(MainActivity.this,0, myIntent, 0);
				AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.add(Calendar.SECOND, 10);
				long interval = 1 * 1000; // REPEAT INT (seconds) * (milli seconds) 1 * 1000
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), interval, pendingIntent);

				// alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),
				// pendingIntent);

				//Toast.makeText(MainActivity.this, "Start Alarm",Toast.LENGTH_SHORT).show();
			}
		});

		btnStopSmsScan.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
				alarmManager.cancel(pendingIntent);

				// Tell the user about what we did.
				//Toast.makeText(MainActivity.this, "Cancel!",Toast.LENGTH_SHORT).show();
			}
		});

	


		final Button btnBar = (Button) findViewById(R.id.btnBarChart);
		btnBar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				BarGraph bar = new BarGraph();
				Intent barIntent = bar.getIntent(getApplicationContext(),
						db.getVaccinesCount(_selections), _selections);
				startActivity(barIntent);
			}
		});
		

		final Button btnPie = (Button) findViewById(R.id.btnPieChart);
		btnPie.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				PieGraph pie = new PieGraph();
				Intent pieIntent = pie.getIntent(getApplicationContext(),
						db.getVaccinesCount(_selections), _selections);
				startActivity(pieIntent);

			}
		});

		final Button btnMap = (Button) findViewById(R.id.btnMap);
		btnMap.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				double lat=0, lon=0;String vid="Hello World";
//				db.openDB();
//				Cursor c = db.lt_getAllLocation();
//				db.closeDB();
//				
//		        if(c!=null){
//		        	Log.v("JEEV - Server", "C is "+c);
//		        	c.moveToFirst();
//		        	vid=c.getString(0);
//		        	lat=Double.parseDouble(c.getString(c.getColumnIndex("Latitude")));
//		        	lon=Double.parseDouble(c.getString(c.getColumnIndex("Longitude")));
//		        	}
		        
		        Intent myIntent = new Intent(MainActivity.this, MapActivity.class);
		        
		        myIntent.putExtra("Marker", vid);
		        myIntent.putExtra("Lat", lat);
		        myIntent.putExtra("Lon", lon);
		        MainActivity.this.startActivity(myIntent);
			}
		});
	}

	public class ButtonClickHandler implements View.OnClickListener {
		public void onClick(View view) {
			showDialog(0);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(this)
				.setTitle("Vaccines")
				.setMultiChoiceItems(_options, _selections,
						new DialogSelectionClickHandler())
				.setPositiveButton("OK", new DialogButtonClickHandler())
				.create();
	}

	public class DialogSelectionClickHandler implements
			DialogInterface.OnMultiChoiceClickListener {
		public void onClick(DialogInterface dialog, int clicked,
				boolean selected) {
			Log.v("ImmuneWorld:", _options[clicked] + " selected: " + selected);
		}
	}

	public class DialogButtonClickHandler implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int clicked) {
			// switch( clicked )
			// {
			// case DialogInterface.BUTTON_POSITIVE:
			// printSelectedPlanets();
			// break;
			// }
		}
	}

	protected void printSelectedPlanets() {
		// for( int i = 0; i < _options.length; i++ ){
		Log.v("Hello", _options[0] + " selected: " + _selections[0]);
		// }
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}