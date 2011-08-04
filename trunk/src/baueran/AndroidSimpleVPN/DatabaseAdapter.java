package baueran.AndroidSimpleVPN;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAdapter 
{
	private static Context ctx;
	private static DatabaseHelper dbHelper = null;
	private static SQLiteDatabase db = null;
	
	public DatabaseAdapter(Context c)
	{
		ctx = c;
	}
	
	public void open()
	{
		if (dbHelper == null || db == null) {
			dbHelper = new DatabaseHelper(ctx);
			db = dbHelper.getWritableDatabase();
		}
	}

	public void close()
	{
		if (dbHelper != null)
			dbHelper.close();
	}
	
	public Cursor getVPNCursor()
	{
		return db.rawQuery("SELECT name AS _id, type FROM vpn", null);
	}

	public Cursor getPPTPCursor()
	{
		return db.rawQuery("SELECT name AS _id, server, enc, domains, username, password FROM pptp", null);
	}

	public Cursor getPrefsCursor()
	{
		return db.rawQuery("SELECT _id, value, rowid FROM prefs", null);
	}

	public long insert(String table, ContentValues values)
	{
		try {
			return db.insert(table, null, values);
		}
		catch (Exception e) {
			System.out.println("Exception thrown when inserting data: " + e.getMessage());
			return -1;
		}
	}

	public long update(int rowId, String table, ContentValues values)
	{
		try {
			return db.update(table, values, "_id=" + rowId, null);
		}
		catch (Exception e) {
			System.out.println("Exception thrown when inserting data: " + e.getMessage());
			return -1;
		}
	}

	public void deleteVPN(String name)
	{
		if (db != null) {
			db.delete("vpn",  "name=?", new String[] { name });
			db.delete("pptp", "name=?", new String[] { name });			
		}
	}
}
