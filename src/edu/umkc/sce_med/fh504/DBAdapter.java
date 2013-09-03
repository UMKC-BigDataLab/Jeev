package edu.umkc.sce_med.fh504;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

	private static final String TAG = "DBAdapter";
	private static final String DB_NAME = "JeevDB.db";
	private static final int DB_VERSION = 1;

	private static final String DB_TABLE_pt = "Patient_Table";
	public static final String pt_id = "Patient_ID";
	public static final String pt_fname = "First_Name";
	public static final String pt_lname = "Last_Name";
	public static final String pt_sex = "Sex";
	public static final String pt_dob = "Date_of_Birth";
	public static final String pt_pid = "Parent_ID";

	private static final String DB_TABLE_vt = "Vaccination_Table";
	private static final String vt_id = "Vaccine_ID";
	private static final String vt_patient_id = "Patient_ID";
	private static final String vt_cpt_code = "CPT_Code";
	// private static final String vt_vaccine="Vaccine";
	private static final String vt_date = "Vaccination_Date";
	private static final String vt_CREATE_INDEX_pt_id ="CREATE INDEX IDX_"+vt_patient_id+"_"+vt_patient_id+" ON "+DB_TABLE_vt+" ("+vt_patient_id+");";

	private static final String DB_TABLE_lt = "Location_Table";
	public static final String lt_id = "Location_id";
	public static final String lt_vid = "Vaccine_ID";
	public static final String lt_lat = "Latitude";
	public static final String lt_long = "Longitude";

	private static final String DB_CREATE_TABLE_pt = "CREATE TABLE IF NOT EXISTS "
			+ DB_TABLE_pt
			+ "("
			+ pt_id
			+ " integer primary key autoincrement, "
			+ pt_fname
			+ " text not null, "
			+ pt_lname
			+ " text not null, "
			+ pt_sex
			+ " text, "
			+ pt_dob
			+ " text not null, "
			+ pt_pid
			+ " text,"
			+ " UNIQUE ("
			+ pt_fname
			+ ","
			+ pt_lname
			+ ","
			+ pt_sex
			+ ","
			+ pt_dob + "," + pt_pid + ") ON CONFLICT ABORT);";

	private static final String DB_CREATE_TABLE_vt = "CREATE TABLE IF NOT EXISTS "
			+ DB_TABLE_vt
			+ "("
			+ vt_id
			+ " integer primary key autoincrement,"
			+ vt_patient_id
			+ " integer not null,"
			+ vt_date
			+ " text not null,"
			+ vt_cpt_code
			+ " integer not null,"
			+ " FOREIGN KEY ( "
			+ vt_patient_id
			+ ") REFERENCES "
			+ DB_TABLE_pt
			+ "(" + pt_id + ")" + " ON UPDATE CASCADE);";

	private static final String DB_CREATE_TABLE_lt = "CREATE TABLE IF NOT EXISTS "
			+ DB_TABLE_lt
			+ "("
			+ lt_id
			+ " integer primary key autoincrement, "
			+ lt_vid
			+ " integer not null,"
			+ lt_lat
			+ " text not null, "
			+ lt_long
			+ " text not null, "
			+ " UNIQUE ("
			+ lt_vid
			+ ","
			+ lt_lat
			+ ","
			+ lt_long
			+ ") ON CONFLICT ABORT,"
			+ " FOREIGN KEY ( "
			+ lt_vid
			+ ") REFERENCES "
			+ DB_TABLE_vt
			+ "("
			+ vt_id
			+ ")"
			+ " ON UPDATE CASCADE);";

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			if (!db.isReadOnly()) {
				// Enable foreign key constraints
				db.execSQL("PRAGMA foreign_keys=ON;");				
			}
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(TAG, "Creating tables...");

			try {
				db.execSQL(DB_CREATE_TABLE_pt);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			try {
				db.execSQL(DB_CREATE_TABLE_vt);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			try {
				db.execSQL(DB_CREATE_TABLE_lt);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				db.execSQL(vt_CREATE_INDEX_pt_id);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_pt);
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_vt);
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_lt);

			onCreate(db);
		}
	}

	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	public DBAdapter openDB() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	public void closeDB() {
		DBHelper.close();
	}

	// inert into location Table
	public long lt_insertLocation(String vid, String lati, String longi) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(lt_lat, lati);
		initialValues.put(lt_long, longi);
		initialValues.put(lt_vid, vid);

		long rowId = -1;
		try {
			
			rowId = db.insertWithOnConflict(DB_TABLE_lt, null, initialValues,
					SQLiteDatabase.CONFLICT_ABORT);
		} catch (SQLiteConstraintException e) {
		}

		return rowId;
	}

	public long pt_insertPatient(String fn, String ln, String s, String d,
			String p) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(pt_fname, fn);
		initialValues.put(pt_lname, ln);
		initialValues.put(pt_sex, s);
		initialValues.put(pt_pid, p);
		initialValues.put(pt_dob, d);

		long rowId = -1;
		try {
			rowId = db.insertWithOnConflict(DB_TABLE_pt, pt_sex, initialValues,
					SQLiteDatabase.CONFLICT_ABORT);
		} catch (SQLiteConstraintException e) {
			rowId = findPatient(fn, ln, s, d, p);
		}

		return rowId;

	}

	private long findPatient(String fn, String ln, String s, String d, String p) {

		Cursor mCursor = db.query(DB_TABLE_pt, new String[] { pt_id }, pt_fname
				+ " = '" + fn + "' AND " + pt_lname + " = '" + ln + "' AND  "
				+ pt_sex + " = '" + s + "' AND " + pt_dob + " = '" + d
				+ "' AND " + pt_pid + " = '" + p + "'", null, null, null, null,
				"1");
		if (mCursor != null) {
			mCursor.moveToFirst();
			return Long.parseLong(mCursor.getString(0));
		}
		return -1;
	}

	private String[] findPatientRecord(String selection) {
		if (selection.equals(""))
			return null;
		String patientInfo = null, patient_id = null;
		Log.v(TAG, "Query Selection: " + selection);
		Cursor mCursor = null;
		try {

			mCursor = db.query(DB_TABLE_pt, new String[] { pt_id, pt_fname,
					pt_lname, pt_sex, pt_dob, pt_pid }, selection, null, null,
					null, null, "1");

		} catch (NullPointerException e) {
			Log.v(TAG, "Patient Not Found! ");
		}

		
		if (mCursor != null) {
		if(mCursor.getCount()>0){	
			mCursor.moveToFirst();
			patient_id = mCursor.getString(0);
			Log.v(TAG, "Patient ID: " + patient_id);
			
			patientInfo = mCursor.getString(1)+ "%" + mCursor.getString(2) + "%" + mCursor.getString(3)+ "%" 
			+ mCursor.getString(4) + "%" + mCursor.getString(5)+ "%";
			
			Log.v(TAG, "Patient INFO: " + patientInfo);
			
		}

		} else {
			Log.v(TAG, "Patient is null: " + patientInfo);
		}
		
		String[] result = { patientInfo, patient_id };
		return result;
	}

	private String getSelectionString(String[] QueryParameters) {
		if (QueryParameters.length != 5) {
			Log.v(TAG, "Error: Missing Search Parameters!");
			return null;
		}
		String[] selectionSubStr = new String[5];
		int count = 0;
		if (QueryParameters[0] != null) {
			selectionSubStr[0] = pt_fname + " = '" + QueryParameters[0] + "' ";
			count++;
		}
		if (QueryParameters[1] != null) {
			selectionSubStr[1] = pt_lname + " = '" + QueryParameters[1] + "' ";
			count++;
		}
		if (QueryParameters[2] != null) {
			selectionSubStr[2] = pt_sex + " = '" + QueryParameters[2] + "' ";
			count++;
		}
		if (QueryParameters[3] != null) {
			selectionSubStr[3] = pt_dob + " = '" + QueryParameters[3] + "' ";
			count++;
		}
		if (QueryParameters[4] != null) {
			selectionSubStr[4] = pt_pid + " = '" + QueryParameters[4] + "' ";
			count++;
		}

		String selection = "";
		for (int s = 0; s < 5; s++) {
			if (selectionSubStr[s] != null) {
				selection += selectionSubStr[s];
				count--;
				if (count > 0) {
					selection += " AND ";
				}
			}
		}
		return selection;
	}
		

	public String getPatientRecord(String[] QueryParameters) {
		
		String selection = getSelectionString(QueryParameters);
		
		
		String [] record = findPatientRecord(selection); //[0]->patient record, [1]->patient id 
		String [] vlist=null;
		if(record==null)
			return null;
		else if(record !=null)
			if(record[1]!=null)//patient id != null
				vlist = vt_getVaccinesList(Long.parseLong(record[1]));
		String vlistStr = countVaccinations(vlist);

		if (vlistStr != null) {
			if (vlistStr.length() > 1)
				record[1] = record[0] + vlistStr;
		} else
			record[1] = record[0];
		return record[1];
	}

	
	private static String countVaccinations(String[] vlist) {

		if(vlist==null)
			return null;
		// vlist is assumed to be sorted
		if (vlist.length == 0)
			return null;
		
		Vaccines vtable = new Vaccines();
		String vaccineList = "";
		
		//Initialization
		int count = 0;
		int index, currentIndex = -1;
		index = vtable.lookupVaccineIndex(vlist[0]);
		
		if(index<0)
			return null;
		int k=0;
		for (k=0;k < vlist.length;k++) {
			currentIndex = vtable.lookupVaccineIndex(vlist[k]);
			if(currentIndex>-1 && currentIndex !=index){
				
				vaccineList += vlist[k-1]+ ":" + count + "%";
				index=currentIndex;
				count=1;
				}
			else if(currentIndex>-1)
				count++;
		}
		
		if(currentIndex>-1 && index>-1)
			vaccineList += vlist[k-1]+ ":" + count + "%";
		else 
			return null;
		
		return vaccineList;
	}
	//Retrieve vaccine ID
	public String []vt_getVaccinesList(long patient_id) throws SQLException{
		String []list=null;
		
		Cursor mCursor = db.query(false, DB_TABLE_vt, new String[] { vt_cpt_code },vt_patient_id + " = '" + patient_id + "'", null, null,null, vt_cpt_code, null);
		if (mCursor != null) {
			int nrows = mCursor.getCount();
			if (nrows > 0) {
				list = new String[nrows];
				Log.v(TAG, "Count Vlist: " + String.valueOf(nrows));
				for (int v = 0; v < nrows; v++) {
					mCursor.moveToNext();
					list[v] = mCursor.getString(0);
				}
				Log.v(TAG, "Vlist: " + list.toString());
			}
		}
		return list;
	}
	// inert into Vaccination Table
	public long vt_insertVaccination(long patient_id, String cptcode,
			String date) {
		Vaccines table = new Vaccines();
		String vaccine = table.lookupVaccine(cptcode);

		if (vaccine != null) {
			Log.v(TAG,String.valueOf(patient_id) + " " + cptcode);

			ContentValues initialValues = new ContentValues();
			initialValues.put(vt_patient_id, patient_id);
			initialValues.put(vt_cpt_code, cptcode);
			initialValues.put(vt_date, date);

			// should vaccines be unique or not?
			return db.insertWithOnConflict(DB_TABLE_vt, null, initialValues,SQLiteDatabase.CONFLICT_ABORT);
		} else
			return -1;
	}

	public int[] getVaccinesCount(boolean[] selection) {
		Vaccines table = new Vaccines();
		String query;
		int[] count = new int[table.size];
		openDB();
		for (int p = 0; p < table.size; p++) {
			if (selection[p] == true) {
				query = "SELECT COUNT(DISTINCT " + vt_patient_id
						+ ") AS numP FROM Vaccination_Table WHERE CPT_Code='"
						+ table.cptCode[p] + "'";
				Cursor objCursor = db.rawQuery(query, null);

				if (objCursor != null) {
					objCursor.moveToFirst();
					count[p] = objCursor.getInt(0);
				}
			}
		}

		closeDB();
		return count;
	}

	public Cursor lt_getAllLocation() throws SQLException {
		Cursor mCursor=null;
		try{
		 mCursor = db.query(true, DB_TABLE_lt, new String[] { lt_vid,lt_lat, lt_long }, null, null, null, null, null, null);
		}
		catch(NullPointerException e){;}
		
		if (mCursor != null) {
			if(mCursor.getCount()<1)
				return null;
			mCursor.moveToNext();
		}
		return mCursor;
	}

}