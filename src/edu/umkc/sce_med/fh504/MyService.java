package edu.umkc.sce_med.fh504;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
	private final String APP_TAG="Jeev S (Service)";
	
	final DBAdapter db = new DBAdapter(this);
	private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static Random rnd = new Random();
	private String key;
	private AESCrypto encrypter;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		key = "C57K9MOxwfLMln5kA3NIC0Y01fP7tanS";
		encrypter = new AESCrypto(key);
        
		//Toast.makeText(this, "-MyService.onStart()-", Toast.LENGTH_LONG).show();
		
		Log.v("JEEV Service", "WORKING...");
		
		String FILENAME = "LAST_READ_SMS_DATE";
		
		// Log.v(APP_TAG, String.valueOf(deleteFile(FILENAME)));

		scanSmsStoreage(FILENAME);

		stopSelf();
		Log.v("JEEV Service", "DONE");
		return START_NOT_STICKY;
	}

	private void scanSmsStoreage(String FILENAME) {

		String latestDate = getLoggedDate(FILENAME);
		String tempDate = latestDate;

		boolean readAll = false;

		if (latestDate.equals("-1")) {
			readAll = true;
			Log.v(APP_TAG, "READ ALL");
		} else
			// getSimpleTime(latestDate)
			;//Log.v(APP_TAG, "LAST READ SMS DATE: " + latestDate);

		Uri uri = Uri.parse("content://sms/inbox");
		Cursor c = getContentResolver().query(uri, null, null, null, null);

		String BDY, NUM, DT;

		boolean CONTINUE = true;

		if (c.moveToFirst()) {
			int i = 0;
			while (i < c.getCount() && CONTINUE) {

				BDY = c.getString(c.getColumnIndexOrThrow("body")).toString();
				NUM = c.getString(c.getColumnIndexOrThrow("address"))
						.toString();
				DT = c.getString(c.getColumnIndexOrThrow("date")).toString();
				if (i == 0)
					tempDate = DT;

				if (readAll == true) {
					processSMSContent(BDY, NUM);
					Log.v(APP_TAG, "T" + i + ": " + BDY);
				} else if (readAll == false) {
					if (latestDate.equals(DT))// reached a previously read
												// sms:stop
						CONTINUE = false;
					else {
						processSMSContent(BDY, NUM);
						Log.v(APP_TAG, "T" + i + ": " + BDY);
					}
				}

				c.moveToNext();
				i++;
			}
		}

		c.close();
		LogDate(tempDate, FILENAME);
	}

	void insertIntoDB(String[][] validContent) {
		

		long insertionDuration = System.currentTimeMillis();
		String ver = validContent[0][0]; // version
		String date = validContent[0][1]; // date
		String lati = validContent[0][2]; // latitude
		String longi = validContent[0][3]; // longitude
		String fname = validContent[0][4]; // first name
		String lname = validContent[0][5]; // last name
		String sex = validContent[0][6]; // sex
		String dob = validContent[0][7]; // date of birth
		String pid = validContent[0][8]; // parent id

		Log.v("Jeev", "LAT: " + lati + " LONG:" + longi + " FN: "
				+ fname + " DT: " + date);

		// insert patient
		db.openDB();
		long p_id = db.pt_insertPatient(fname, lname, sex, dob, pid);
		db.closeDB();

		// insert vaccination if supplied
		if (validContent[1] != null) {
			String[] vaccinations = validContent[1];// list of vaccines taken by
													// patient
			if (vaccinations.length > 0 && p_id > 0) {
				db.openDB();
				for (int i = 0; i < vaccinations.length; i++) {
					Log.v(APP_TAG, "Vaccine: " + vaccinations[i]);
					// insert vaccine
					String v_id = Long.toString(db.vt_insertVaccination(p_id,vaccinations[i], date));
					// insert loc if valid
					if(lati.equals("-1.0")==false && longi.equals("-1.0")==false)
						db.lt_insertLocation(v_id, lati, longi);
				}
				db.closeDB();
			}// end if length >0
		}// end if != null
		//Date currentDate = new Date(System.currentTimeMillis());
		//Log.v("Jeev", "Insert Done..Time: "+currentDate.toString());
		insertionDuration = System.currentTimeMillis()-insertionDuration;
		Log.v("Jeev", "Insert Done..Time: "+insertionDuration);

	}

	void processSMSContent(String cipherContent, String phNumber) {
		String jeevVer1 = "a001";
		String jeevVer2 = "a002";
		{

			if (cipherContent != null) {
				long proc_duration = System.currentTimeMillis();
				
				long dur = System.currentTimeMillis();
				Log.v("Jeev", "Processing SMS... Start @ "+dur);
				try {
					String[][] validContent = null;
					String content = encrypter.decrypt(cipherContent);
					if (content != null) {

						String[] multiMessage = content.split(jeevVer1);
						if (multiMessage.length > 1) {// SMS store request
							for (int c = 1; c < multiMessage.length; c++) {
								validContent = getValidContent(jeevVer1
										+ multiMessage[c]);
								if (validContent != null) {
									if (validContent[0] != null)
										insertIntoDB(validContent);
									else
										Log.e("ImmuneWorldDB","ERROR: CONTENT NOT VALID!");
								} else
									Log.e("ImmuneWorldDB","ERROR: CONTENT NOT VALID!  (NULL)");
							}// end for
							proc_duration = System.currentTimeMillis()-proc_duration;
							Log.v("Jeev", "Server Procssing..Duration: "+proc_duration+" ms");
						} else if (multiMessage.length == 1) {
							Log.v("ImmuneWorldDB", "Info Request!");
							if (content.substring(0, 4).equals(jeevVer2)) {// is info request
								String returnMessage = jeevVer1 + "%"+processInfoRequest(content);
								Log.v("ImmuneWorldDB", "About to MSG LEN: "+ returnMessage.length());
								Log.v("ImmuneWorldDB", "About to MSG: "+ returnMessage);
								returnMessage=encrypter.encrypt(returnMessage);
								mySendSMS(phNumber, returnMessage);
							} else
								Log.v("ImmuneWorldDB","VER " + content.substring(0, 4));
							
							proc_duration = System.currentTimeMillis()-proc_duration;
							Log.v("Jeev", "Server Procssing..Duration: "+proc_duration+" ms");
						}
						
					} else
						Log.e("ImmuneWorldDB", "ERROR: Could Not Decrypt: "+ cipherContent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean sendSMS(String phNumber, String msg) {
		SmsManager SMSManager = SmsManager.getDefault();
		if (SMSManager == null) {
			return (false);
		}

		int fragmentCount = 0;
		ArrayList<String> parts = SMSManager.divideMessage(msg);
		int pCount = parts.size();
		if (pCount > 1)
			SMSManager.sendMultipartTextMessage(phNumber, null, parts, null,
					null);
		else
			SMSManager.sendTextMessage(phNumber, null, msg, null, null);

		return true;
	}

	private boolean mySendSMS(String phNumber, String msg) {
		SmsManager SMSManager = SmsManager.getDefault();
		if (SMSManager == null)
			return (false);

		if (msg.length() > 160) {
			String[] parts = divideMessage(msg + "*");
			for (int m = 0; m < parts.length; m++) {
				SMSManager.sendTextMessage(phNumber, null, parts[m], null, null);
				Log.v("TEST", "PART " + m + " Len: " + parts[m]);

			}
			return true;
		}
		SMSManager.sendTextMessage(phNumber, null, msg, null, null);
		return true;
	}

	private String[] divideMessage(String msg) {

		String Header = "a003" + randomString(3);
		String body = msg;
		

		String[] parts = splitEqually(body);

		for (int i = 0; i < parts.length; i++) {
			parts[i] = Header + parts[i];
		}
		return parts;
	}

	public static String [] splitEqually(String text) {
		
		int L=text.length();
		int size=153;
		int NUM =((L + size - 1) / size);
		
	    String [] ret = new String[NUM];
	    
	    int start =0;
	    for (int s = 0; s < NUM; s ++) {
	        ret[s]=(text.substring(start, Math.min(L, start + size)));
	        start+=size;
	    }
	    return ret;
	}
	String randomString( int len ) 
	{
	   StringBuilder sb = new StringBuilder( len );
	   for( int i = 0; i < len; i++ ) 
	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
	   return sb.toString();
	}



	private String [][] getValidContent(String content) {
    	if(content==null)
    		return null;//error
    	
    	String [] validContent = new String [9];
    	String [][] result= new String[2][];
    	result[0]=null;
    	result[1]=null;

		Log.v("ImmuneWorldDB", "Validating Content: " + content);
		StringTokenizer st = new StringTokenizer(content, "%");
		
		
		//has enough fields?
		if(st.countTokens()<8)
			return null;//no
		
		String header = st.nextToken();
		validContent[0] = header.substring(0, 4);//version
		validContent[1] = getDateString(header.substring(4, 12));//date
		validContent[2] = st.nextToken();//latitude
		validContent[3] = st.nextToken();//longitude
		validContent[4]= st.nextToken();//first name
		validContent[5]= st.nextToken();//last name
		validContent[6]= st.nextToken();//sex
		validContent[7]= st.nextToken();//date of birth
		validContent[8]= st.nextToken();//parent id
		result[0]=validContent;
		
		//count remaining tokens
		//if > 0 then vaccination code(s) are supplied
		int numVaccincations = st.countTokens();
		String[] vcode =null;
		if (numVaccincations> 0 )
		{
			Log.v("ImmuneWorldDB","Number of vaccinations: " + numVaccincations);
			vcode = new String[numVaccincations];
			for (int i = 0; i < numVaccincations; i++) 
				vcode[i] = st.nextToken();
			
			result[1]=vcode;
		}
		return result;
	}

	private String processInfoRequest(String content) {
		StringTokenizer st = new StringTokenizer(content, "%");
		String jVer = st.nextToken();
		String [] searchCriteria = new String[st.countTokens()];
		
		
		int c=0;
		while(st.hasMoreTokens()){
			searchCriteria[c] = st.nextToken();
			c++;
		}

		String [] searchParameters = getQueryParameters(searchCriteria);
		for(int s=0; s<searchParameters.length;s++){
			Log.v("JEEV", "SP: "+s+" "+searchParameters[s]);	
		}

		db.openDB();
		String patientRecord= db.getPatientRecord(searchParameters);
		db.closeDB();
		return patientRecord;
	}

	private String[] getQueryParameters(String[] searchCriteria) {
		
		if(searchCriteria.length==0)
			return null;

		String[] parameters=new String[5];
		
		for(int q=0;q<searchCriteria.length;q++){
			if(searchCriteria[q].startsWith("f:"))//first name
				parameters[0]=searchCriteria[q].substring(2);
			else if(searchCriteria[q].startsWith("l:"))//last name
				parameters[1]=searchCriteria[q].substring(2);
			else if(searchCriteria[q].startsWith("s:"))//sex
				parameters[2]=searchCriteria[q].substring(2);
			else if(searchCriteria[q].startsWith("d:"))//dob
				parameters[3]=searchCriteria[q].substring(2);
			else if(searchCriteria[q].startsWith("p:"))//pid
				parameters[4]=searchCriteria[q].substring(2);
		}			
		
		
	
		
		return parameters;
	}

	private String getDateString(String str){
    	
    	if(isNumber(str) && str.length()==8)
    			return str.substring(0, 4)+'-'+str.substring(4, 6)+'-'+str.substring(6, 8);
    	
    	return null;
    }
    private boolean isNumber(String num){
        try{
            Integer.parseInt(num);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
	private void LogDate(String date, String FILENAME) {

		FileOutputStream fos = null;
		try {
			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fos.write((date).getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getSimpleTime(String dateMiSec) {

		if (dateMiSec == null || dateMiSec.equals("-1"))
			return "-1";

		Long longDate = Long.valueOf(dateMiSec);
		Calendar cal = Calendar.getInstance();
		int offset = cal.getTimeZone().getOffset(cal.getTimeInMillis());
		Date da = new Date();
		da = new Date(longDate - (long) offset);
		cal.setTime(da);
		return cal.getTime().toString();

	}
	private String getLoggedDate(String FILENAME) {
		FileInputStream fis = null;
		String date = null;

		try {

			fis = openFileInput(FILENAME);
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			date = br.readLine();
			if (date == null)
				date = "-1";
			in.close();
			fis.close();
		} catch (FileNotFoundException e1) {
			date = "-1";
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "-MyService.onBind()-", Toast.LENGTH_LONG).show();
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "-MyService.onCreate()-", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "-MyService.onDestroy()-", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "-MyService.onRebind()-", Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "-MyService.onUnbind()-", Toast.LENGTH_LONG).show();
		return false;
	}

	public class LocalBinder extends Binder {
		MyService getService() {
			return MyService.this;
		}
	}

}