package edu.umkc.sce_med.fh504;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AndroidService extends Activity {

	private PendingIntent pendingIntent;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button btnStartSmsScan = (Button) findViewById(R.id.btnStartSmsScan);
		Button btnStopSmsScan = (Button) findViewById(R.id.btnStopSmsScan);

		btnStartSmsScan.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent myIntent = new Intent(AndroidService.this,MyService.class);
				//pendingIntent = PendingIntent.getService(AndroidService.this,0, myIntent, 0);
				  
				
				  AndroidService.this.startService(myIntent);
				  
				//AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
				//Calendar calendar = Calendar.getInstance();
				//calendar.setTimeInMillis(System.currentTimeMillis());
				//calendar.add(Calendar.SECOND, 10);
				//long interval = 1 * 1000; // REPEAT INT (sec) * (milisec)
				//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), interval, pendingIntent);

				// alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),
				// pendingIntent);

			}
		});

		btnStopSmsScan.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				Intent myIntent = new Intent(AndroidService.this,MyService.class);
				  AndroidService.this.stopService(myIntent);
				  
				
				//AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
				//alarmManager.cancel(pendingIntent);
			}
		});

	}

}