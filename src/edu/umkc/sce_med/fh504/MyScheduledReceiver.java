package edu.umkc.sce_med.fh504;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyScheduledReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		Intent intentService = new Intent(context, MyService.class);
		intentService.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(intentService);
	}

}