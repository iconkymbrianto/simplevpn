package baueran.AndroidSimpleVPN;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME    = 
		"vpndata";
	private static final String DATABASE_CREATE1 = 
		"CREATE TABLE vpn (name TEXT PRIMARY KEY, type TEXT NOT NULL);";
	private static final String DATABASE_CREATE2 = 
		"CREATE TABLE pptp (name TEXT PRIMARY KEY, server TEXT NOT NULL, enc INTEGER NOT NULL, domains TEXT, username TEXT NOT NULL, password TEXT NOT NULL);";
	private static final String DATABASE_CREATE3 = 
		"CREATE TABLE prefs (_id TEXT PRIMARY KEY, value TEXT NOT NULL);";
		
	public DatabaseHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
        db.execSQL(DATABASE_CREATE1);
        db.execSQL(DATABASE_CREATE2);
        db.execSQL(DATABASE_CREATE3);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// TODO Auto-generated method stub
	}
}
