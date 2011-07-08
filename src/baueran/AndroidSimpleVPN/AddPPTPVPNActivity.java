package baueran.AndroidSimpleVPN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddPPTPVPNActivity extends Activity implements OnClickListener {
    static final String[] vpnTypes = new String[] {  };
    private ListView lv1;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.listlayout);

//    	ArrayList<Map> data = new ArrayList<Map>();
//    	
//        // Add some objects into the array list
//        Map m = new HashMap();
//        m.put("activity",    "VPN name");
//        m.put("explanation", "VPN name not set");
//        
//        data.add(m);

        // setup the data adaptor
//        String[] from = { "activity", "explanation" };
//        int[] to = { R.id.name_entry, R.id.type_entry };
        
        // SimpleAdapter adapter = new SimpleAdapter(this, (List<? extends Map<String, ?>>) data, R.layout.doublelistviewitem, from, to);

        MyCustomAdapter adapter = new MyCustomAdapter();
        adapter.addItem("VPN name", "VPN name not set");
        adapter.addItem("Set VPN server", "VPN server is not set");
        adapter.addItemButton("Enable encryption", "PPTP encryption is disabled");
        adapter.addItem("DNS search domain", "DNS search domain not set (optional)");
        
    	lv1 = (ListView)findViewById(R.id.listView1);
        lv1 = (ListView)findViewById(R.id.listView1);
        lv1.setAdapter(adapter);
    	  	
    	lv1.setTextFilterEnabled(true);
    	lv1.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			// Add new VPN
    			// ...
    		}
     	});
    }
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}

	private class MyCustomAdapter extends BaseAdapter 
	{
		private final static int TYPE_DOUBLELIST       = 0;
		private final static int TYPE_DOUBLEBUTTONLIST = 1;
        private static final int TYPE_MAX_COUNT = TYPE_DOUBLEBUTTONLIST + 1;
        private LayoutInflater mInflater;
        ViewHolder holder;

		private ArrayList<String[]> data = new ArrayList<String[]>();
		private int buttonPos = -1;
		
		public MyCustomAdapter() 
		{
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    }
		
		@Override
	    public int getViewTypeCount() 
		{
			return TYPE_MAX_COUNT;
	    }
		
		@Override
        public int getItemViewType(int position) 
		{
			return position == 2? TYPE_DOUBLEBUTTONLIST : TYPE_DOUBLELIST;
        }
		
		@Override
		public int getCount() 
		{
			return data.size();
		}

		@Override
		public Object getItem(int arg0) 
		{
			return data.get(arg0);
		}

		@Override
		public long getItemId(int position) 
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			if (position == buttonPos) {
				convertView = mInflater.inflate(R.layout.doublelistviewitembutton, null);
				holder = new ViewHolder();
		        holder.text1 = (TextView) convertView.findViewById(R.id.textView1);
		        holder.text2 = (TextView) convertView.findViewById(R.id.textView2);
			}
			else {
				convertView = mInflater.inflate(R.layout.doublelistviewitem, null);
				holder = new ViewHolder();
                holder.text1 = (TextView) convertView.findViewById(R.id.name_entry);
                holder.text2 = (TextView) convertView.findViewById(R.id.type_entry);
			}

			holder.text1.setText(data.get(position)[0]);
	        holder.text2.setText(data.get(position)[1]);
			convertView.setTag(holder);

			return convertView;
		}
		
		public void addItem(final String item, final String description) 
		{
			data.add(new String[] { item, description } );
			notifyDataSetChanged();
		}
	 
		public void addItemButton(final String item, final String description) 
		{
			data.add(new String[] { item, description } );

			// save separator position
			buttonPos = data.size() - 1;
            notifyDataSetChanged();
        }
	}	   
	
	static class ViewHolder 
	{
        TextView text1, text2;
    }
}
