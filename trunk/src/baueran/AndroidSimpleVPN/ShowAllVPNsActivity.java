package baueran.AndroidSimpleVPN;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import dalvik.system.PathClassLoader;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
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
    private VPNNetwork selectedVPNProfile = null;
	private final Preferences prefs = Preferences.getInstance();
    
	private final int CONTEXT_CONNECT = 0;
	private final int CONTEXT_DISCONNECT = 1;
	private final int CONTEXT_EDIT = 2;
	private final int CONTEXT_DELETE = 3;
	
	private static DBMCustomAdapter     dbmAdapter = null;
	private static ArrayAdapter<String> toolButtonsAdapter = null;
	
	private void displayPasswordInputDlg(String pw, int att)
	{
		final int attempts = att;
		final String encPassword = pw;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllVPNsActivity.this);
		builder.setTitle(attempts + " attempts left");
		builder.setMessage("Enter master password");

		final EditText input = new EditText(ShowAllVPNsActivity.this);
		builder.setView(input);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					if (Encryption.md5(input.getText().toString()).equals(encPassword)) {
						prefs.setMasterPassword(input.getText().toString());
						dialog.cancel();
					}
					else if (attempts > 1) {
						dialog.cancel();
						displayPasswordInputDlg(encPassword, attempts - 1);
					}
					else
						finish();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					finish();
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
				finish();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}
	
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

				if (prefs.currentlyConnectedNetwork().isEmpty() && connectVPN(profile)) {
					prefs.setCurrentlyConnectedNetwork(selectedNetwork);
					dbmAdapter.notifyDataSetChanged();
				}
			}
        });
        registerForContextMenu(vpnLV);
        
    	// Get stored, encrypted master password from DB
    	cursor = dbA.getPrefsCursor();
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
    		if (cursor.getString(0).equals("master_password")) {
    			final String encPassword = cursor.getString(1);
    			prefs.setMasterPasswordRowId(cursor.getInt(2));
    			displayPasswordInputDlg(encPassword, 3);
    		}
    	}

    	ArrayList<String> buttonEntries = new ArrayList<String>();
    	buttonEntries.add("Add VPN");
    	buttonEntries.add(prefs.getMasterPasswordRowId() < 0? "Set master password" : "Change master password"); 
    	toolButtonsAdapter = 
    		new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, buttonEntries);
    	
        addLV = (ListView)findViewById(R.id.listView0);
    	addLV.setAdapter(toolButtonsAdapter);
        addLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		switch (position) {
        		case 0: // Add VPN
        			if (prefs.getMasterPasswordRowId() >= 0) {
	        			Intent intent = new Intent(Intent.ACTION_VIEW);
		        		intent.setClassName(ShowAllVPNsActivity.this, AddVPNActivity.class.getName());
		        		startActivity(intent);
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
    				AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllVPNsActivity.this);
    				builder.setTitle("Set master password");

    				final EditText input = new EditText(ShowAllVPNsActivity.this);
    				builder.setView(input);
    				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    					@SuppressWarnings("unchecked")
						public void onClick(DialogInterface dialog, int whichButton) {
    						if (!input.getText().toString().isEmpty()) {
								try {
									final String newPass = input.getText().toString().trim();
									ContentValues values = new ContentValues();
									values.put("_id", "master_password");
									values.put("value", Encryption.md5(newPass));

									// Store new password
									if (prefs.getMasterPasswordRowId() < 0) {
										// TODO: Having to store the rowid is really fugly...
										prefs.setMasterPasswordRowId((int)dbA.insert("prefs", values));
										
										if (prefs.getMasterPasswordRowId() >= 0) {
											final ArrayAdapter<String> tAdapter = (ArrayAdapter<String>)addLV.getAdapter();
											tAdapter.remove("Set master password");
											tAdapter.insert("Change master password", 1);
											tAdapter.notifyDataSetChanged();
										}
										
										prefs.setMasterPassword(newPass);
									} 
									// Update old password
									else {
										System.out.println("Updating: " + prefs.getMasterPasswordRowId() + newPass);
										
										if (encryptAllProfiles(newPass) && 
												dbA.update(prefs.getMasterPasswordRowId(), "prefs", values) > 0) {
											prefs.setMasterPassword(newPass);
											Toast.makeText(getApplicationContext(), "New password set", Toast.LENGTH_LONG).show();
										}
										else
											SimpleAlertBox.display("Could not change password", 
																	"Re-encrypting VPN data failed.", 
																	ShowAllVPNsActivity.this);
									}
								} catch (NoSuchAlgorithmException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
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
            } 
        }); 
	}

    /*
     * This method will usually be called when the user changed the
     * master password and an encryption of all usernames and
     * passwords with this new master password becomes necessary.
     */
    
    public boolean encryptAllProfiles(String password)
    {
		VPNNetwork profile = null;
		
		if (dbA != null) {
	    	cursor = dbA.getVPNCursor();
	    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				profile = getPPTPProfile(cursor.getString(0));

				try {
					final String decrUser = Encryption.decrypt(profile.getEncUsername(), prefs.getMasterPassword());
					System.out.println(decrUser);
					profile.setEncUsername(Encryption.encrypt(decrUser, password));
					final String decrPass = Encryption.decrypt(profile.getEncPassword(), prefs.getMasterPassword());
					System.out.println(decrPass);
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

		switch (item.getItemId()) {
		case CONTEXT_CONNECT: {
			final VPNNetwork profile = getProfile(selectedVPN); 

			if (connectVPN(profile)) {
				prefs.setCurrentlyConnectedNetwork(profile.getName());
				dbmAdapter.notifyDataSetChanged();
			}
			
			return true;
			}
		case CONTEXT_DISCONNECT: {
			disconnectVPN();
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
    		Bundle bundle = new Bundle();
    		bundle.putString("name", selectedVPN);
    		
    		// TODO: This calls PPTP profiles by default.  Look up which profile is
    		// associated to the name, then call the right activity for edit.
    		Intent intent = new Intent(Intent.ACTION_VIEW);
    		intent.setClassName(ShowAllVPNsActivity.this, AddPPTPVPNActivity.class.getName());
    		intent.putExtras(bundle); // Send VPN network name to be edited to activity
    		startActivity(intent);
    		finish();
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
    
	private ServiceConnection mConnection = new ServiceConnection() 
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) 
		{
			ApplicationInfo vpnAppInfo;
	    	Context ctx = getApplicationContext();
	    	PathClassLoader stubClassLoader;

			try {
				vpnAppInfo = ctx.getPackageManager().getApplicationInfo("com.android.settings", 0);
		        stubClassLoader = new PathClassLoader(vpnAppInfo.sourceDir, ClassLoader.getSystemClassLoader());
				Class<?> stubClass = Class.forName("android.net.vpn.IVpnService$Stub", true, stubClassLoader);
				Method m = stubClass.getMethod("asInterface", IBinder.class);
				Object theService = m.invoke(null, service);
				
				Class<?> vpnProfile = null;
				try {
					vpnProfile = Class.forName("android.net.vpn.PptpProfile");
				} catch (ClassNotFoundException e2) {
					e2.printStackTrace();
				}
								
				Object vpnInstance = null;
		    	
		    	try {
		    		vpnInstance = vpnProfile.newInstance();
		    		Method mm = vpnInstance.getClass().getMethod("setServerName", String.class);
		    		mm.invoke(vpnInstance, selectedVPNProfile.getServer());
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
				
				Method mm = stubClass.getMethod("connect", new Class[] { Class.forName("android.net.vpn.VpnProfile"), String.class, String.class });
		        mm.invoke(theService, new Object[]{ vpnInstance, selectedVPNProfile.getEncUsername(), selectedVPNProfile.getEncPassword() });
			} 
			catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
			catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			Toast.makeText(getApplicationContext(), "VPN should be ready now", Toast.LENGTH_LONG).show();
		}

		// According to documentation, the following callback is not invoked
		// upon unbind, but upon a crash!
		
		@Override
		public void onServiceDisconnected(ComponentName name) 
		{
			Toast.makeText(getApplicationContext(), "VPN disconnected", Toast.LENGTH_LONG).show();
		}
	};

	public void disconnectVPN()
	{
		unbindService(mConnection);
	}
	
	public boolean connectVPN(VPNNetwork profile)
    {
    	boolean connected = false;
    	
    	try {
    		selectedVPNProfile = profile;
    		connected = bindService(new Intent("android.net.vpn.IVpnService"), mConnection, Context.BIND_AUTO_CREATE);
    	} 
    	catch (SecurityException e) {
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		
		Toast.makeText(getApplicationContext(), connected? "Service bound" : "Service not bound", Toast.LENGTH_LONG).show();

    	return connected;
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
