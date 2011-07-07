package baueran.AndroidSimpleVPN;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{

	private static final String DATABASE_NAME   = "vpndata";
	private static final String DATABASE_CREATE = "CREATE TABLE vpn (name TEXT PRIMARY KEY, " +
															        "type TEXT NOT NULL);";

	public DatabaseHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
        db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// TODO Auto-generated method stub
	}

}
