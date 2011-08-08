package baueran.AndroidSimpleVPN;

import java.util.ArrayList;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
	private PPTPNetwork pptpProfile = new PPTPNetwork();
	private String oldVPNName = null;
	
	private final Preferences prefs = Preferences.getInstance();
	
	public void displayNotEnoughDataError()
	{
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setTitle("Attention");
        dlgAlert.setPositiveButton("Back", null);
        dlgAlert.setCancelable(true);
		
        if (pptpProfile.getName() == null)
        	dlgAlert.setMessage("Enter VPN name");
        else
        	dlgAlert.setMessage("Enter VPN server");
        
        dlgAlert.setPositiveButton("Back", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
        		Intent intent = new Intent(Intent.ACTION_VIEW);
        		intent.setClassName(AddPPTPVPNActivity.this, ShowAllVPNsActivity.class.getName());
        		startActivity(intent);
        		finish();
			}
		});
        
		dlgAlert.create().show();
	}
	
	@Override
	public void onBackPressed() 
	{
		if (pptpProfile.getName() != null && pptpProfile.getServer() != null) {
			deleteOldVPNData();
			writeVPNData();
			
			Intent intent = new Intent(Intent.ACTION_VIEW);
    		intent.setClassName(AddPPTPVPNActivity.this, ShowAllVPNsActivity.class.getName());
    		startActivity(intent);
    		finish();
		}
		else
			displayNotEnoughDataError();
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
    	if (pptpProfile.getName() != null && pptpProfile.getServer() != null) {
	    	DatabaseAdapter adapter = new DatabaseAdapter(getApplicationContext());
			ContentValues values = null;
			
			// Add VPN account data to DB
			values = new ContentValues();
			values.put("name",     pptpProfile.getName());
			values.put("server",   pptpProfile.getServer());
			values.put("enc",      pptpProfile.isEncEnabled()? "1" : "0");
			values.put("domains",  pptpProfile.getDomains() != null? pptpProfile.getDomains() : ""); // TODO: Not sure if != null check is required
			values.put("username", pptpProfile.getEncUsername());
			values.put("password", pptpProfile.getEncPassword());
			adapter.insert("pptp", values);
	
			// Add VPN account name to list of stored and available VPNs
			// to be presented by ShowAllVPNsActivity
			values = new ContentValues();
			values.put("name", pptpProfile.getName());
			values.put("type", "PPTP");
			adapter.insert("vpn", values);
    	}
    }

    public PPTPNetwork readVPNData(String vpnNetworkName) throws Exception
    {
		PPTPNetwork networkInfo = null;
    	DatabaseAdapter adapter = new DatabaseAdapter(getApplicationContext());
    	Cursor result           = adapter.getPPTPCursor();
    	
    	for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
    		if (result.getString(0).equals(vpnNetworkName)) {
    			networkInfo = new PPTPNetwork();
    			networkInfo.setName(vpnNetworkName);
    			networkInfo.setServer(result.getString(1));
    			networkInfo.setEncEnabled(result.getInt(2) == 1? true : false);
    			networkInfo.setDomains(result.getString(3));
    			networkInfo.setEncUsername(result.getString(4));
    			networkInfo.setEncPassword(result.getString(5));
    			break;
    		}
    	}

    	if (networkInfo == null)
    		throw new Exception("readVPNData could not retrieve PPTP account information from SQLite database.");
    		
    	return networkInfo;
    }
    
    // This function deletes old data, in case the user came to this
    // activity to edit an existing VPN network profile.
    
    public void deleteOldVPNData()
    {
    	if (oldVPNName != null) {
    		DatabaseAdapter adapter = new DatabaseAdapter(getApplicationContext());
    		adapter.deleteVPN(oldVPNName);
    	}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClassName(this, ShowAllVPNsActivity.class.getName());

		switch (item.getItemId()) {
    	case saveVPNBtnId:
    		if (pptpProfile.getName() != null && pptpProfile.getServer() != null) {
	    		deleteOldVPNData();
	    		writeVPNData();
	    		startActivity(intent);
	    		finish();
    		}
    		else
    			displayNotEnoughDataError();
    		
    		return true;
    	case cancelVPNBtnId:
    		startActivity(intent);
    		finish();
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

        Bundle bundle = this.getIntent().getExtras();
        // Check if we have been called with a parameter to edit
        // an existing VPN network entry, or want to create a new one.
        // Parameter is called "name" and set inside ShowAllVPNsActivity.
        if (bundle != null) {
        	try {
				pptpProfile = readVPNData(bundle.getString("name"));
			} catch (Exception e) {
				e.printStackTrace();
			}
        	oldVPNName = pptpProfile.getName();
        }

        adapter.addItem("VPN name", 
				pptpProfile.getName() == null? "VPN name not set" : pptpProfile.getName());
        try {
			adapter.addItem("Username", 
					pptpProfile.getEncUsername() == null? "Username not set" : 
								Encryption.decrypt(pptpProfile.getEncUsername(), prefs.getMasterPassword()));
		} catch (Exception e1) {
			adapter.addItem("Username", "<decryption failed>");
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
			adapter.addItem("Password", 
					pptpProfile.getEncPassword() == null? "Password not set" : 
								Encryption.decrypt(pptpProfile.getEncPassword(), prefs.getMasterPassword()));
		} catch (Exception e1) {
			adapter.addItem("Password", "<decryption failed>");
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        adapter.addItem("Set VPN server", 
        		pptpProfile.getServer() == null? "VPN server is not set" : pptpProfile.getServer());
        adapter.addItemButton("Enable encryption", 
        		"PPTP encryption is " + (pptpProfile.isEncEnabled()? "enabled" : "disabled"));
        adapter.addItem("DNS search domain", 
	        	pptpProfile.getDomains() == null? "DNS search domain not set (optional)" : pptpProfile.getDomains());
        
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
    						pptpProfile.setName(input.getText().toString().trim());
    						((String[])(adapter.getItem(0)))[1] = pptpProfile.getName();
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
    				builder.setTitle("Set username");

    				final EditText input = new EditText(AddPPTPVPNActivity.this);
    				builder.setView(input);
    				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						final MyCustomAdapter adapter = ((MyCustomAdapter)lv1.getAdapter());
    						
    						try {
								pptpProfile.setEncUsername(Encryption.encrypt(input.getText().toString().trim(),
														   prefs.getMasterPassword()));
								((String[])(adapter.getItem(1)))[1] = pptpProfile.getEncUsername();
	    						adapter.notifyDataSetChanged();
							} catch (Exception e) {
								SimpleAlertBox.display("Cannot store profile data",
											 		   "Encryption of username failed.", 
											 		   AddPPTPVPNActivity.this);
							}
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
    			case 2: {
    				AlertDialog.Builder builder = new AlertDialog.Builder(AddPPTPVPNActivity.this);
    				builder.setTitle("Set password");

    				final EditText input = new EditText(AddPPTPVPNActivity.this);
    				builder.setView(input);
    				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						final MyCustomAdapter adapter = ((MyCustomAdapter)lv1.getAdapter());

    						try {
								pptpProfile.setEncPassword(Encryption.encrypt(input.getText().toString().trim(),
														   prefs.getMasterPassword()));
	    						((String[])(adapter.getItem(2)))[1] = pptpProfile.getEncPassword();
	    						adapter.notifyDataSetChanged();
							} catch (Exception e) {
								SimpleAlertBox.display("Cannot store profile data",
											 		   "Encryption of password failed.", 
											 		   AddPPTPVPNActivity.this);
							}
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
    			case 3: {
    				AlertDialog.Builder builder = new AlertDialog.Builder(AddPPTPVPNActivity.this);
    				builder.setTitle("Set VPN server");

    				final EditText input = new EditText(AddPPTPVPNActivity.this);
    				builder.setView(input);
    				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						final MyCustomAdapter adapter = ((MyCustomAdapter)lv1.getAdapter()); 
    						pptpProfile.setServer(input.getText().toString().trim());
    						((String[])(adapter.getItem(3)))[1] = pptpProfile.getServer();
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
    			case 4:
					pptpProfile.setEncEnabled(!pptpProfile.isEncEnabled());
    				if (pptpProfile.isEncEnabled()) {
    					((TextView)view.findViewById(R.id.textView2)).setText("PPTP encryption is enabled");
    					((CheckBox)(view.findViewById(R.id.checkBox1))).setChecked(true);
    				}
    				else {
    					((TextView)view.findViewById(R.id.textView2)).setText("PPTP encryption is disabled");
    					((CheckBox)(view.findViewById(R.id.checkBox1))).setChecked(false);
    				}
    				break;
    			case 5: {
    				AlertDialog.Builder builder = new AlertDialog.Builder(AddPPTPVPNActivity.this);
    				builder.setTitle("Set search domain");

    				final EditText input = new EditText(AddPPTPVPNActivity.this);
    				builder.setView(input);
    				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						final MyCustomAdapter adapter = ((MyCustomAdapter)lv1.getAdapter()); 
    						pptpProfile.setDomains(input.getText().toString().trim());
    						((String[])(adapter.getItem(5)))[1] = pptpProfile.getDomains();
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
			return position == buttonPos? TYPE_DOUBLEBUTTONLIST : TYPE_DOUBLELIST;
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
		        
		        if (pptpProfile.isEncEnabled())
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
