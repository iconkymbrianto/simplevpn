package baueran.AndroidSimpleVPN;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;

public class ShowAllVPNsActivity extends Activity 
{ 
    protected ListView vpnLV, addLV;
    protected DatabaseAdapter dbA;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);

    	// There is only one action in the following list, but that's 
    	// how the actual VPN client GUI works as well...
    	String[] buttonEntries = new String[] { "Add VPN" };
    	
        addLV = (ListView)findViewById(R.id.listView0);
        addLV.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, buttonEntries));
        addLV.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		Intent intent = new Intent(Intent.ACTION_VIEW);
        		intent.setClassName(ShowAllVPNsActivity.this, AddVPNActivity.class.getName());
        		startActivity(intent);
            } 
        }); 
        
    	// Read data from SQLite DB
    	dbA = new DatabaseAdapter(this);
    	dbA.open();
    	Cursor cursor = dbA.getCursor();
    	startManagingCursor(cursor);
    	
    	// The desired columns of the cursor to be bound
    	String[] from = new String[] { "_id", "type" };
    	// The XML defined views which the data will be bound to
    	int[] to = new int[] { R.id.name_entry, R.id.type_entry };
    	
    	SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, R.layout.doublelistviewitem, cursor, from, to);
    	
        vpnLV = (ListView)findViewById(R.id.listView1);
        vpnLV.setAdapter(mAdapter);
    }
}
