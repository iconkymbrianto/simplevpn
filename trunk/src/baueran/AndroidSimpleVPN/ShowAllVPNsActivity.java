package baueran.AndroidSimpleVPN;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;

public class ShowAllVPNsActivity extends Activity
{ 
    private ListView vpnLV, addLV;
    private DatabaseAdapter dbA;
    private Cursor cursor = null;
	private final Preferences prefs = Preferences.getInstance();
    
	private final int CONTEXT_CONNECT = 0;
	private final int CONTEXT_DISCONNECT = 1;
	private final int CONTEXT_EDIT = 2;
	private final int CONTEXT_DELETE = 3;
	
	private static DBMCustomAdapter     dbmAdapter = null;
	private static ArrayAdapter<String> toolButtonsAdapter = null;
		
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);

    	// Get stored VPNs from DB
    	dbA = new DatabaseAdapter(this);
    	dbA.open();

    	cursor = dbA.getVPNCursor();
    	startManagingCursor(cursor);

    	// //////////////////////////////////////////////////
    	// TODO: For testing: delete master password on start
    	// dbA.deletePW();
    	// //////////////////////////////////////////////////

    	dbmAdapter = new DBMCustomAdapter(this, cursor);
    	vpnLV = (ListView)findViewById(R.id.listView1);
        vpnLV.setAdapter(dbmAdapter);
        vpnLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				String selectedNetwork = ((String[])dbmAdapter.getItem(position))[0];
				VPNNetwork profile = getProfile(selectedNetwork);

				if (prefs.currentlyConnectedNetwork().isEmpty()) {
					connectDlg(profile);
				}
			}
        });
        registerForContextMenu(vpnLV);

    	// Get stored, encrypted master password from DB
        if (prefs.getEncMasterPasswordRowId() < 0 || prefs.getEncMasterPassword().isEmpty()) {
	    	cursor = dbA.getPrefsCursor();
	    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	    		if (cursor.getString(0).equals("master_password")) {
	    			prefs.setEncMasterPassword(cursor.getString(1));
	    			prefs.setEncMasterPasswordRowId(cursor.getInt(2));
	    		}
	    	}
        }
        
    	ArrayList<String> buttonEntries = new ArrayList<String>();
    	buttonEntries.add("Add VPN");
    	buttonEntries.add(prefs.getEncMasterPasswordRowId() < 0? "Set master password" : "Change master password"); 
    	toolButtonsAdapter = 
    		new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, buttonEntries);
    	
        addLV = (ListView)findViewById(R.id.listView0);
    	addLV.setAdapter(toolButtonsAdapter);
        addLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		switch (position) {
        		case 0: // Add VPN
        			if (prefs.getEncMasterPasswordRowId() >= 0) {
        				AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllVPNsActivity.this);
	    				final EditText input = new EditText(ShowAllVPNsActivity.this);
	    				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
	    				builder.setView(input);
        				builder.setTitle("Enter master password");
        				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog, int whichButton) {
        						if (prefs.equalsPass(input.getText().toString()))
        						{
            			    		Bundle bundle = new Bundle();
            			    		bundle.putString("password", input.getText().toString());

        		        			Intent intent = new Intent(Intent.ACTION_VIEW);
        			        		intent.setClassName(ShowAllVPNsActivity.this, AddVPNActivity.class.getName());
        			        		intent.putExtras(bundle);
        			        		startActivity(intent);
                				}
                				dialog.cancel();
        					}
        				});
        	    		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	    			public void onClick(DialogInterface dialog, int whichButton) {
        	    				dialog.cancel();
        	    			}
        	    		});
        				AlertDialog alert = builder.create();
        				alert.show();
        			}
        			else {
        				AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllVPNsActivity.this);
        				builder.setTitle("Master password not set");
        				builder.setMessage("You must first set a master password, before you can add accounts.");
        				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog, int whichButton) {
        						dialog.cancel();
        					}
        				});
        				AlertDialog alert = builder.create();
        				alert.show();
        			}
	        		break;
        		case 1: // Set/change master password
    				AlertDialog.Builder builderOld = new AlertDialog.Builder(ShowAllVPNsActivity.this);
    				builderOld.setTitle("Enter master password");
    				final EditText inputOld = new EditText(ShowAllVPNsActivity.this);
    				
    				inputOld.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    				builderOld.setView(inputOld);
    				builderOld.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialogOld, int whichButton) {
    						dialogOld.cancel();
						}
					});
    				builderOld.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogOld, int which) {
							dialogOld.cancel();

							if (prefs.equalsPass(inputOld.getText().toString())) {
			    				AlertDialog.Builder builderNew = new AlertDialog.Builder(ShowAllVPNsActivity.this);
			    				builderNew.setTitle("Set master password");

			    				final EditText inputNew = new EditText(ShowAllVPNsActivity.this);
			    				inputNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			    				builderNew.setView(inputNew);
			    				builderNew.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			    					@SuppressWarnings("unchecked")
									public void onClick(DialogInterface dialogNew, int whichButton) {
			    						if (!inputNew.getText().toString().isEmpty()) {
											String newPass = null;
											final String clearNewPass = inputNew.getText().toString().trim(); 
											final ContentValues values = new ContentValues();
											
											try {
												newPass = Encryption.md5(clearNewPass);
												values.put("_id", "master_password");
												values.put("value", newPass);
											} catch (NoSuchAlgorithmException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}

											// Store new password
											if (prefs.getEncMasterPasswordRowId() < 0) {
												// TODO: Having to store the row-id is really f'ugly...
												prefs.setEncMasterPasswordRowId((int)dbA.insert("prefs", values));
												
												if (prefs.getEncMasterPasswordRowId() >= 0) {
													final ArrayAdapter<String> tAdapter = (ArrayAdapter<String>)addLV.getAdapter();
													tAdapter.remove("Set master password");
													tAdapter.insert("Change master password", 1);
													tAdapter.notifyDataSetChanged();
												}
												
												if (newPass != null)
													prefs.setEncMasterPassword(newPass);
												else
													System.out.println("Pooooo");
											} 
											// Update old password
											else {
												final ProgressDialog myProgressDialog = ProgressDialog.show(ShowAllVPNsActivity.this, 
																						"Please wait...", 
																						"Using new password to encrypt VPN profiles...", true);
												
												Thread recrypt = new Thread() {
													public void run() {
			        									boolean success = false;

			        									try {
				    										success = encryptAllProfiles(clearNewPass, inputOld.getText().toString()) && 
				    													dbA.update(prefs.getEncMasterPasswordRowId(), "prefs", values) > 0;
				                                        } catch (Exception e) { 
				                                        	success = false;
				                                        	e.printStackTrace();
				                                        }
				                                        if (success)
															try {
																prefs.setEncMasterPassword(Encryption.md5(clearNewPass));
															} catch (NoSuchAlgorithmException e) {
																// TODO Auto-generated catch block
																e.printStackTrace();
																finish();
															}
														else
				                                        	System.out.println("Re-encryption failed!!!!!!!!!!!!!!!!!!!!");
				                                        
				                                        myProgressDialog.dismiss();
													}
												};
												recrypt.start();
											}
			    						}
			    					}
			    				});
			    				builderNew.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    					public void onClick(DialogInterface dialogNew, int whichButton) {
			    						dialogNew.cancel();
									}
								});

			    				AlertDialog alertNew = builderNew.create();
			    				alertNew.show();
							}
						}
						
    				});    				

    				AlertDialog alertOld = builderOld.create();
    				alertOld.show();
        			
        			break;
        		}
            } 
        }); 
        
        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
            	Toast.makeText(getApplicationContext(),
                        	   (String)"Received intent", 
                        	   Toast.LENGTH_LONG).show();
            	
                Bundle bundle = intent.getExtras();
                String profName = new String();
                // For details on the bundle, see e.g.,
                // http://hi-android.info/src/android/net/vpn/VpnManager.java.html
                if (bundle != null) {
                	try {
        				profName =  bundle.getString("profile_name");
        				Object state = bundle.get("connection_state");
        				
        				Class<?> VpnState  = Class.forName("android.net.vpn.VpnState");
        				Object[] vpnStates = VpnState.getEnumConstants();
        				
        				// For enum values, see enum in http://hi-android.info/src/android/net/vpn/VpnState.java.html
        				System.out.println("State: " + state);
        				if (state != vpnStates[0] && state != vpnStates[3]) {
        					prefs.unsetCurrentlyConnectedNetwork();
        					dbmAdapter.notifyDataSetChanged();
        				}
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
                }
            	
            	System.out.println(profName + ": !!!!!!!!!!!!!!!!");
            }
        }, new IntentFilter("vpn.connectivity")); 
	}

    /*
     * This method will usually be called when the user changed the
     * master password and an encryption of all usernames and
     * passwords with this new master password becomes necessary.
     */
    
    public boolean encryptAllProfiles(String password, String oldPassword)
    {
		VPNNetwork profile = null;
		
		if (dbA != null) {
	    	cursor = dbA.getVPNCursor();
	    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				profile = getPPTPProfile(cursor.getString(0));

				try {
					final String decrUser = Encryption.decrypt(profile.getEncUsername(), oldPassword);
					profile.setEncUsername(Encryption.encrypt(decrUser, password));
					final String decrPass = Encryption.decrypt(profile.getEncPassword(), oldPassword);
					profile.setEncPassword(Encryption.encrypt(decrPass, password));
					profile.write(this);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
	    	}
		}
		
		return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
    	final AdapterView.AdapterContextMenuInfo info =	(AdapterView.AdapterContextMenuInfo)menuInfo;
    	final String selectedVPN = ((String[])(vpnLV.getAdapter().getItem(info.position)))[0];
		menu.setHeaderTitle(selectedVPN);

		menu.add(0, CONTEXT_CONNECT, CONTEXT_CONNECT, "Connect to network");
    	menu.add(0, CONTEXT_DISCONNECT, CONTEXT_DISCONNECT, "Disconnect from network");
    	menu.add(0, CONTEXT_EDIT, CONTEXT_EDIT, "Edit network");
    	menu.add(0, CONTEXT_DELETE, CONTEXT_DELETE, "Delete network");

    	menu.getItem(0).setEnabled(prefs.currentlyConnectedNetwork().isEmpty());
    	menu.getItem(1).setEnabled(prefs.currentlyConnectedNetwork().equals(selectedVPN));
    }

    @Override 
    public boolean onContextItemSelected(MenuItem item) 
    {
		final ContextMenuInfo menuInfo = item.getMenuInfo();
    	final AdapterView.AdapterContextMenuInfo info =	(AdapterView.AdapterContextMenuInfo)menuInfo;
    	final String selectedVPN = ((String[])(vpnLV.getAdapter().getItem(info.position)))[0];
		VPNWrapper vpn = new VPNWrapper(getApplicationContext());

		switch (item.getItemId()) {
		case CONTEXT_CONNECT: {
			final VPNNetwork profile = getProfile(selectedVPN);
			connectDlg(profile);
			return true;
			}
		case CONTEXT_DISCONNECT: {
			vpn.disconnect();
			prefs.unsetCurrentlyConnectedNetwork();
			dbmAdapter.notifyDataSetChanged();
			return true;
			}
    	case CONTEXT_DELETE: {
    		dbA.deleteVPN(selectedVPN);
    		((DBMCustomAdapter)vpnLV.getAdapter()).deleteVPN(selectedVPN);
    		((DBMCustomAdapter)vpnLV.getAdapter()).notifyDataSetChanged(); 
    		return true; 
    		}
    	case CONTEXT_EDIT: {
    		AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllVPNsActivity.this);
    		builder.setMessage("Enter master password");

    		final EditText input = new EditText(ShowAllVPNsActivity.this);
    		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    		builder.setView(input);
    		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
					if (prefs.equalsPass(input.getText().toString())) {
			    		Bundle bundle = new Bundle();
			    		bundle.putString("name", selectedVPN);
			    		bundle.putString("password", input.getText().toString());
			    		
			    		// TODO: This calls PPTP profiles by default.  Look up which profile is
			    		// associated to the name, then call the right activity for edit.
			    		Intent intent = new Intent(Intent.ACTION_VIEW);
			    		intent.setClassName(ShowAllVPNsActivity.this, AddPPTPVPNActivity.class.getName());
			    		intent.putExtras(bundle); // Send VPN network name to be edited to activity
			    		startActivity(intent);
			    		finish();
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
    		
    		return true;
    		}
		}
    	
    	return false;
    }
    
    public VPNNetwork getProfile(String vpnName)
    {
    	DatabaseAdapter adapter = new DatabaseAdapter(getApplicationContext());
		VPNNetwork profile = null;
		
		if (adapter != null) {
	    	cursor = adapter.getVPNCursor();
	    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	    		if (cursor.getString(0).equals(vpnName)) {
	    			if (cursor.getString(1).equals("PPTP")) {
	    				profile = getPPTPProfile(vpnName);
	    				break;
	    			}
	    			// TODO: Handle other profile types as else-ifs
	    		}
	    	}
		}
		
    	return profile;
    }
    
    private PPTPNetwork getPPTPProfile(String vpnName)
    {
		PPTPNetwork profile = null;
		
		if (dbA != null) {
	    	cursor = dbA.getPPTPCursor();
	    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	    		if (cursor.getString(0).equals(vpnName)) {
	    			profile = new PPTPNetwork();
	    			profile.setName(cursor.getString(0));
	    			profile.setServer(cursor.getString(1));
	    			profile.setEncEnabled(cursor.getInt(2) == 1? true : false);
	    			profile.setDomains(cursor.getString(3));
	    			profile.setEncUsername(cursor.getString(4));
	    			profile.setEncPassword(cursor.getString(5));
	    			break;
	    		}
	    	}
		}
		
		return profile;
    }
    
	private void connectDlg(final VPNNetwork profile)
	{
		final VPNWrapper vpn = new VPNWrapper(getApplicationContext());
		// final String encPass = dbA.getEncryptedMasterPasssword();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllVPNsActivity.this);
		// builder.setTitle(attempts + " attempts left");
		builder.setMessage("Enter master password");

		final EditText input = new EditText(ShowAllVPNsActivity.this);
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		builder.setView(input);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (prefs.equalsPass(input.getText().toString()) && vpn.connect(profile)) {
					prefs.setCurrentlyConnectedNetwork(profile.getName());
					dbmAdapter.notifyDataSetChanged();
				}
				dialog.cancel();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}      
	        
    // The custom adapter is necessary because a SimpleCursorAdapter
    // is more or less a 1:1 mapping from DB to view, and I need to
    // change/update the connection status of the respective connection
    // rather than displaying the connection's type (e.g. PPTP)
    
    private class DBMCustomAdapter extends CursorAdapter 
	{
		private ArrayList<String[]> data = new ArrayList<String[]>();
        private LayoutInflater mInflater;
        private ViewHolder holder;

		@Override
		public void bindView(View view, Context context, Cursor cursor) 
		{
		    final String name = cursor.getString(cursor.getColumnIndex("_id"));
            ((TextView)view.findViewById(R.id.name_entry)).setText(name);
            ((TextView)view.findViewById(R.id.type_entry)).setText("Connect to network");
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
			
			if (prefs.currentlyConnectedNetwork().equals(data.get(position)[0]))
				holder.text2.setText("Connected");
			else
				holder.text2.setText("Connect to network");
			// holder.text2.setText(data.get(position)[1]);
			convertView.setTag(holder);

			return convertView;
		}
		
		@Override
		public Object getItem(int arg0) 
		{
			return data.get(arg0);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) 
		{			
		    return LayoutInflater.from(context).inflate(R.layout.doublelistviewitem, parent, false);
		}
		
        public DBMCustomAdapter(Context context, Cursor cursor)
        {
			super(context, cursor);

			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() )
				data.add(new String[] { cursor.getString(0), cursor.getString(1) } );
		}

		@Override
		public int getCount() 
		{
			return data.size();
		}
		
		public void deleteVPN(String name)
		{
			for (String[] item : data) {
				if (item[0].equals(name)) {
					data.remove(item);
					return;  // There should only ever be one profile for a given name, so one delete should always suffice
				}
			}
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
