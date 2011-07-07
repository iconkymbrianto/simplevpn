package baueran.AndroidSimpleVPN;

import java.util.Date;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.view.View;

public class ShowAllVPNsActivity extends Activity 
{ 
    static final String[] vpnTypes = new String[] { };
    protected ListView lv1;
    protected Button addVPNButton;
    protected DatabaseAdapter dbA;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);

    	dbA = new DatabaseAdapter(this);
    	dbA.open();
    	
    	addVPNButton = (Button)findViewById(R.id.addVPN);
    	addVPNButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// Display AddVPNActivity 
        		System.out.println("Click!!!!!!!!!!!!!!!!!!!!!!!");

        		/*
        		// Add dummy entry to DBMS on click
        		ContentValues values = new ContentValues();
        		values.put("name", new Date().getTime());
        		values.put("type", "test");
        		dbA.insert(values);
        		*/
        		
        		// Change activity
        		Intent intent = new Intent(Intent.ACTION_VIEW);
        		intent.setClassName(ShowAllVPNsActivity.this, AddVPNActivity.class.getName());
        		startActivity(intent);
            }
        });

    	// Read data from SQLite DB
    	Cursor cursor = dbA.getCursor();
    	startManagingCursor(cursor);
    	
    	// The desired columns of the cursor to be bound
    	String[] from = new String[] { "_id", "type" };
    	// The XML defined views which the data will be bound to
    	int[] to = new int[] { R.id.name_entry, R.id.type_entry };
    	
    	SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, R.layout.vpnview, cursor, from, to);
    	
        lv1 = (ListView)findViewById(R.id.listView1);
        lv1.setAdapter(mAdapter);
        
        /*
        lv1.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, vpnTypes));
        lv1.setTextFilterEnabled(true);
        lv1.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          	// CONNECT TO CLICKED VPN PROFILE!!
          	// ...

            // When clicked, show a toast with the TextView text
            Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
          }
        });
        */
    }
}
