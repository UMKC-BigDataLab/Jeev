package edu.umkc.sce_med.fh504;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        Intent intent = getIntent();
        String mark = intent.getStringExtra("Marker");
        double lat = intent.getDoubleExtra("Lat", 0);
        double lon = intent.getDoubleExtra("Lon", 0);
        
		GoogleMap mMap;
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		if (mMap!=null){
			mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(mark));	
		}
		        
    }

}