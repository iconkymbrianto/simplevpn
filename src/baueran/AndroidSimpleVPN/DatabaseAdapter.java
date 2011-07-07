package baueran.AndroidSimpleVPN;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAdapter 
{
	private Context ctx;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	public DatabaseAdapter(Context c)
	{
		ctx = c;
	}
	
	public void open()
	{
		dbHelper = new DatabaseHelper(ctx);
		db = dbHelper.getWritableDatabase();
	}

	public void close()
	{
		dbHelper.close();
	}
	
	public Cursor getCursor()
	{
		return db.rawQuery("SELECT name AS _id, type FROM vpn", null);
		// return db.query("vpn", new String[] { "name", "type" }, null, null, null, null, null);
	}
	
	public long insert(ContentValues values)
	{
		if (db != null)
			return db.insert("vpn", null, values);
		else
			return -1;
	}
}
