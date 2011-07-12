package baueran.AndroidSimpleVPN;

import java.util.ArrayList;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.AdapterView.OnItemClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class AddPPTPVPNActivity extends Activity
{
    private ListView lv1;
    
	private final int saveVPNBtnId   = Menu.FIRST;
	private final int cancelVPNBtnId = Menu.FIRST + 1;
	private final int group1Id = 2;
	
	// VPN data
	private String vpnName = null, vpnServer = null, vpnDomain = null;
	private boolean vpnEnc = false;
	
	@Override
	public void onBackPressed() 
	{
		if (vpnName != null && vpnServer != null) {
			writeVPNData();
			
			Intent intent = new Intent(Intent.ACTION_VIEW);
    		intent.setClassName(AddPPTPVPNActivity.this, ShowAllVPNsActivity.class.getName());
    		startActivity(intent);
		}
		else {
			AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
	        dlgAlert.setTitle("Attention");
	        dlgAlert.setPositiveButton("Back", null);
	        dlgAlert.setCancelable(true);
			
	        if (vpnName == null)
	        	dlgAlert.setMessage("Enter VPN name");
	        else
	        	dlgAlert.setMessage("Enter VPN server");
	        
	        dlgAlert.setPositiveButton("Back", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
	        		Intent intent = new Intent(Intent.ACTION_VIEW);
	        		intent.setClassName(AddPPTPVPNActivity.this, ShowAllVPNsActivity.class.getName());
	        		startActivity(intent);
				}
			});
	        
			dlgAlert.create().show();
		}			

		return;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	menu.add(group1Id, saveVPNBtnId, saveVPNBtnId, "Save");
    	menu.add(group1Id, cancelVPNBtnId, cancelVPNBtnId, "Cancel");
    	return super.onCreateOptionsMenu(menu);
    }
    
    public void writeVPNData()
    {
    	if (vpnName != null && vpnServer != null) {
	    	DatabaseAdapter adapter = new DatabaseAdapter(getApplicationContext());
			ContentValues values = null;
			
			// Add VPN account data to DB
			values = new ContentValues();
			values.put("name",    vpnName);
			values.put("server",  vpnServer);
			values.put("enc",     vpnEnc? "1" : "0");
			// TODO: Not sure if this != null check is required
			if (vpnDomain != null)
				values.put("domains", vpnDomain);
			else
				values.put("domains", "");
			adapter.insert(values);
	
			// Add VPN account name to list of stored and available VPNs
			// to be presented by ShowAllVPNsActivity
			values = new ContentValues();
			values.put("name", vpnName);
			values.put("type", "PPTP");
			adapter.insert(values);
    	}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClassName(this, ShowAllVPNsActivity.class.getName());

		switch (item.getItemId()) {
    	case saveVPNBtnId:
    		writeVPNData();
    		startActivity(intent);
    		return true;
    	case cancelVPNBtnId:
    		startActivity(intent);
        	return true;
    	}

    	return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.listlayout);

        lv1 = (ListView)findViewById(R.id.listView1);

        MyCustomAdapter adapter = new MyCustomAdapter();

		adapter.addItem("VPN name", 
				vpnName == null? "VPN name not set" : vpnName);
        adapter.addItem("Set VPN server", 
        		vpnServer == null? "VPN server is not set" : vpnServer);
        adapter.addItemButton("Enable encryption", 
        		"PPTP encryption is " + (vpnEnc? "enabled" : "disabled"));
        adapter.addItem("DNS search domain", 
	        		vpnDomain == null? "DNS search domain not set (optional)" : vpnDomain);
        
        lv1.setAdapter(adapter);
    	lv1.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	    	switch (position)
    			{
    			case 0: {
    				AlertDialog.Builder builder = new AlertDialog.Builder(AddPPTPVPNActivity.this);
    				builder.setTitle("Set VPN name");

    				final EditText input = new EditText(AddPPTPVPNActivity.this);
    				builder.setView(input);
    				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						final MyCustomAdapter adapter = ((MyCustomAdapter)lv1.getAdapter()); 
    						vpnName = input.getText().toString().trim();
    						((String[])(adapter.getItem(0)))[1] = vpnName;
    						adapter.notifyDataSetChanged();
    					}
    				});
    				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						dialog.cancel();
						}
					});

    				AlertDialog alert = builder.create();
    				alert.show();
    		        break;
    			}
    			case 1: {
    				AlertDialog.Builder builder = new AlertDialog.Builder(AddPPTPVPNActivity.this);
    				builder.setTitle("Set VPN server");

    				final EditText input = new EditText(AddPPTPVPNActivity.this);
    				builder.setView(input);
    				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						final MyCustomAdapter adapter = ((MyCustomAdapter)lv1.getAdapter()); 
    						vpnServer = input.getText().toString().trim();
    						((String[])(adapter.getItem(1)))[1] = vpnServer;
    						adapter.notifyDataSetChanged();
    					}
    				});
    				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						dialog.cancel();
						}
					});

    				AlertDialog alert = builder.create();
    				alert.show();
    		        break;
    			}
    			case 2:
					vpnEnc = !vpnEnc;
    				if (vpnEnc) {
    					((TextView)view.findViewById(R.id.textView2)).setText("PPTP encryption is enabled");
    					((CheckBox)(view.findViewById(R.id.checkBox1))).setChecked(true);
    				}
    				else {
    					((TextView)view.findViewById(R.id.textView2)).setText("PPTP encryption is disabled");
    					((CheckBox)(view.findViewById(R.id.checkBox1))).setChecked(false);
    				}
    				break;
    			case 3: {
    				AlertDialog.Builder builder = new AlertDialog.Builder(AddPPTPVPNActivity.this);
    				builder.setTitle("Set search domain");

    				final EditText input = new EditText(AddPPTPVPNActivity.this);
    				builder.setView(input);
    				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						final MyCustomAdapter adapter = ((MyCustomAdapter)lv1.getAdapter()); 
    						vpnDomain = input.getText().toString().trim();
    						((String[])(adapter.getItem(3)))[1] = vpnDomain;
    						adapter.notifyDataSetChanged();
    					}
    				});
    				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						dialog.cancel();
						}
					});

    				AlertDialog alert = builder.create();
    				alert.show();
    		        break; }
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
			// Check if the encryption check button list entry was pressed
			if (position == buttonPos) {
				convertView = mInflater.inflate(R.layout.doublelistviewitembutton, null);
				holder = new ViewHolder();
		        holder.text1 = (TextView)convertView.findViewById(R.id.textView1);
		        holder.text2 = (TextView)convertView.findViewById(R.id.textView2);
		        holder.box1 = (CheckBox)convertView.findViewById(R.id.checkBox1);
		        
		        if (vpnEnc)
		        	holder.box1.setChecked(true);
			}
			else {
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.doublelistviewitem, null);
					holder = new ViewHolder();
	                holder.text1 = (TextView)convertView.findViewById(R.id.name_entry);
	                holder.text2 = (TextView)convertView.findViewById(R.id.type_entry);
				}
				else {
					holder = (ViewHolder)convertView.getTag();
				}
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
	
	// This pattern follows
	// http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/view/List14.html
	
	static class ViewHolder 
	{
        TextView text1, text2;
        CheckBox box1;
    }
}
