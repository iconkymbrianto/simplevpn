package baueran.AndroidSimpleVPN;

import java.util.ArrayList;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    	
    	MyCustomAdapter adapter = new MyCustomAdapter(this, cursor);
    	
        vpnLV = (ListView)findViewById(R.id.listView1);
        vpnLV.setAdapter(adapter);
    }

    // The custom adapter is necessary because a SimpleCursorAdapter
    // is more or less a 1:1 mapping from DB to view, and I need to
    // change/update the connection status of the respective connection
    // rather than displaying the connection's type (e.g. PPTP)
    
    private class MyCustomAdapter extends CursorAdapter 
	{
		private ArrayList<String[]> data = new ArrayList<String[]>();
        private LayoutInflater mInflater;
        private ViewHolder holder;

        // TODO: Not sure when/if this is called...
		@Override
		public void bindView(View view, Context context, Cursor cursor) 
		{
		    final String name = cursor.getString(cursor.getColumnIndex("_id"));
		    // final String type = cursor.getString(cursor.getColumnIndex("type"));
            ((TextView)view.findViewById(R.id.name_entry)).setText(name);
            ((TextView)view.findViewById(R.id.type_entry)).setText("Connect to network");
            // ((TextView)view.findViewById(R.id.type_entry)).setText(type);
		}
		 
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.doublelistviewitem, null);
				holder = new ViewHolder();
                holder.text1 = (TextView)convertView.findViewById(R.id.name_entry);
                holder.text2 = (TextView)convertView.findViewById(R.id.type_entry);
			}
			else {
				holder = (ViewHolder)convertView.getTag();
			}

			holder.text1.setText(data.get(position)[0]);
			holder.text2.setText("Connect to network");
			// holder.text2.setText(data.get(position)[1]);
			convertView.setTag(holder);

			return convertView;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) 
		{			
		    return LayoutInflater.from(context).inflate(R.layout.doublelistviewitem, parent, false);
		}
		
        public MyCustomAdapter(Context context, Cursor cursor)
        {
			super(context, cursor);

			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			for (cursor.moveToFirst(); cursor.moveToNext(); cursor.isAfterLast())
				data.add(new String[] { cursor.getString(0), cursor.getString(1) } );
		}

		@Override
		public int getCount() 
		{
			return data.size();
		}
	}

	// This pattern follows
	// http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/view/List14.html
	
	static class ViewHolder 
	{
	    TextView text1, text2;
	    CheckBox box1;
	}
}

