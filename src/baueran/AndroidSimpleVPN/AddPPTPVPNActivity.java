package baueran.AndroidSimpleVPN;

import java.util.ArrayList;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.AdapterView.OnItemClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddPPTPVPNActivity extends Activity
{
    static final String[] vpnTypes = new String[] {  };
    private ListView lv1;
	private boolean encEnabled = false;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.listlayout);

        MyCustomAdapter adapter = new MyCustomAdapter();
        adapter.addItem("VPN name", "VPN name not set");
        adapter.addItem("Set VPN server", "VPN server is not set");
        adapter.addItemButton("Enable encryption", "PPTP encryption is disabled");
        adapter.addItem("DNS search domain", "DNS search domain not set (optional)");
        
    	lv1 = (ListView)findViewById(R.id.listView1);
        lv1.setAdapter(adapter);
        lv1.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			System.out.println("Position: " + position + " View: " + view + " Id: " + id);
    			switch (position)
    			{
    			case 2:
    				encEnabled = !encEnabled;
    				if (encEnabled) {
    					((TextView)view.findViewById(R.id.textView2)).setText("PPTP encryption is enabled");
    					((CheckBox)(view.findViewById(R.id.checkBox1))).setChecked(true);
    				}
    				else {
    					((TextView)view.findViewById(R.id.textView2)).setText("PPTP encryption is disabled");
    					((CheckBox)(view.findViewById(R.id.checkBox1))).setChecked(false);
    				}
    				break;
    			}
    		}
     	});
    }
	
	private class MyCustomAdapter extends BaseAdapter 
	{
		private final static int TYPE_DOUBLELIST       = 0;
		private final static int TYPE_DOUBLEBUTTONLIST = 1;
        private final static int TYPE_MAX_COUNT = TYPE_DOUBLEBUTTONLIST + 1;
        private LayoutInflater mInflater;
        private ViewHolder holder;

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
			addItem(item, description);

			// save separator position
			buttonPos = data.size() - 1;
        }
	}	   
	
	// A la 
	// http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/view/List14.html
	
	static class ViewHolder 
	{
        TextView text1, text2;
    }
}
