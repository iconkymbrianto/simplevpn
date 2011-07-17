package baueran.AndroidSimpleVPN;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.PathClassLoader;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
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
    
	private final int CONTEXT_CONNECT = 0;
	private final int CONTEXT_DISCONNECT = 1;
	private final int CONTEXT_EDIT = 2;
	private final int CONTEXT_DELETE = 3;

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

    	cursor = dbA.getVPNCursor();
    	startManagingCursor(cursor);
    	
    	MyCustomAdapter adapter = new MyCustomAdapter(this, cursor);
    	
        vpnLV = (ListView)findViewById(R.id.listView1);
        vpnLV.setAdapter(adapter);
    	registerForContextMenu(vpnLV);
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

    	// TODO: Dynamically determine whether or not the connection is enabled
    	menu.getItem(1).setEnabled(false);
    }

    public VPNNetwork getProfile(String vpnName)
    {
		VPNNetwork profile = null;
		
		if (dbA != null) {
	    	cursor = dbA.getVPNCursor();
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
	    			break;
	    		}
	    	}
		}
		
		return profile;
    }
    
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			System.out.println("Service connected!!!!!!!!!!!!!!!!");

			ApplicationInfo vpnAppInfo;
	    	Context ctx = getApplicationContext();
	    	PathClassLoader stubClassLoader;

			try {
				vpnAppInfo = ctx.getPackageManager().getApplicationInfo("com.android.settings", 0);
		        stubClassLoader = new PathClassLoader(vpnAppInfo.sourceDir, ClassLoader.getSystemClassLoader());
				Class<?> stubClass = Class.forName("android.net.vpn.IVpnService$Stub", true, stubClassLoader);
				Method m = stubClass.getMethod("asInterface", IBinder.class);
				Object theService = m.invoke(null, service);
				System.out.println("Service Object: " + theService);
				
				
				Class<?> vpnProfile = null;
				try {
					vpnProfile = Class.forName("android.net.vpn.PptpProfile");
				} catch (ClassNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
								
				Object vpnInstance = null;
		    	
		    	try {
		    		vpnInstance = vpnProfile.newInstance();
		    		Method mm = vpnInstance.getClass().getMethod("setServerName", String.class);
		    		System.out.println("Setting server name via " + mm + ": " + mm.invoke(vpnInstance, "vpn.nicta.com.au"));
				} catch (InstantiationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    	
				
		        Method mm = stubClass.getMethod("connect", new Class[] { Class.forName("android.net.vpn.VpnProfile"), String.class, String.class });
		        System.out.println("Invoke " + mm + ":" + mm.invoke(theService, new Object[]{ vpnInstance, "USERNAME", "PASSWORD" }));

			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // mService = IAndiService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
	};

	public boolean connectVPN(VPNNetwork profile)
    {
    	boolean connected = false;
    	
    	Context ctx = getApplicationContext();
    	ApplicationInfo vpnAppInfo;
    	PathClassLoader stubClassLoader;

    	Class<?> vpnProfile = null;
		try {
			vpnProfile = Class.forName("android.net.vpn.PptpProfile");
		} catch (ClassNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//    	Object vpnInstance = null;
    	
    	try {
    		vpnProfile.newInstance();
    		// vpnInstance = vpnProfile.newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	try {
			vpnAppInfo = ctx.getPackageManager().getApplicationInfo("com.android.settings", 0);
	        stubClassLoader = new PathClassLoader(vpnAppInfo.sourceDir, ClassLoader.getSystemClassLoader());
	        
	        // Load class
	        Class<?> stubClass = Class.forName("android.net.vpn.IVpnService$Stub", true, stubClassLoader);

			System.out.println(bindService(new Intent("android.net.vpn.IVpnService"), mConnection, Context.BIND_AUTO_CREATE));

	        
	        // Class<?> vpnManager = Class.forName("android.net.vpn.VpnManager", true, stubClassLoader);

	        System.out.println("Classes loaded?");
	        System.out.println("Class stubClass : " + stubClass);
	        System.out.println("Class vpnProfile: " + vpnProfile);
//	        System.out.println("Class vpnManager: " + vpnManager);
//	        System.out.println("Class vpnManager: " + vpnManager.getConstructor(Context.class).newInstance(ctx));

	        // Method m = stubClass.getMethod("asInterface", IBinder.class);
	        // Object nativeService = m.invoke(null, );
	        Method m = stubClass.getMethod("connect", new Class[] { Class.forName("android.net.vpn.VpnProfile"), String.class, String.class });
	        System.out.println("Method: " + m);
	        
//	        Method n = vpnManager.getMethod("getSupportedVpnTypes");
//	        Object o = n.invoke(vpnManager.getConstructor(Context.class).newInstance(ctx));
	        // TODO: Create instance of stubClass service, but not sure how... service is not running?!

	        // Method enable = c.getMethod("enable");
	        // m.setAccessible(true);
	        // System.out.println(m.invoke(, new Object[] { null, "a", "b" }));
	        
	        // Object o = m.invoke(stubClass.newInstance(), new Object[]{ vpnInstance, "username", "password" });
//	        System.out.println("Invoked: " + o);	        
    	} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

    	
    	return connected;
    }
    
    @Override 
    public boolean onContextItemSelected(MenuItem item) 
    {
		final ContextMenuInfo menuInfo = item.getMenuInfo();
    	final AdapterView.AdapterContextMenuInfo info =	(AdapterView.AdapterContextMenuInfo)menuInfo;
    	final String selectedVPN = ((String[])(vpnLV.getAdapter().getItem(info.position)))[0];

		switch (item.getItemId()) {
		case CONTEXT_CONNECT: {
			VPNNetwork profile = getProfile(selectedVPN); 

			if (connectVPN(profile)) {
				System.out.println("Profile connected!!!!!!!!!!!");
				// TODO: Update context menu
			}
			else
				System.out.println("Profile NOT connected!!!!!!!!!!!");

			return true;
			}
    	case CONTEXT_DELETE: {
    		dbA.deleteVPN(selectedVPN);
    		((MyCustomAdapter)vpnLV.getAdapter()).deleteVPN(selectedVPN);
    		((MyCustomAdapter)vpnLV.getAdapter()).notifyDataSetChanged(); 
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
		public Object getItem(int arg0) 
		{
			return data.get(arg0);
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
