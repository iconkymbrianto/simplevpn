package baueran.AndroidSimpleVPN;

import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;

public class VPNWrapper
{
	private Context context = null;
	private Preferences prefs = Preferences.getInstance();
	
	public VPNWrapper(Context ctx)
	{
		this.context = ctx;
	}
	
	public boolean disconnect()
    {
    	boolean connected = false;

    	ServiceConnection mConnection = new ServiceConnection() {
    		@Override
    		public void onServiceConnected(ComponentName name, IBinder service) 
    		{
    			ApplicationInfo vpnAppInfo;
    	    	Context ctx = context.getApplicationContext();
    	    	PathClassLoader stubClassLoader;

    			try {
    				vpnAppInfo = ctx.getPackageManager().getApplicationInfo("com.android.settings", 0);
    		        stubClassLoader = new PathClassLoader(vpnAppInfo.sourceDir, ClassLoader.getSystemClassLoader());
    				Class<?> stubClass = Class.forName("android.net.vpn.IVpnService$Stub", true, stubClassLoader);
    				Method m = stubClass.getMethod("asInterface", IBinder.class);
    				Object theService = m.invoke(null, service);
    					    		
		    		Method mm = stubClass.getMethod("disconnect");
		    		mm.invoke(theService);
    			} 
    			catch (Exception e) {
    				e.printStackTrace();
    			}
    		}

    		// According to documentation, the following callback is not invoked
    		// upon unbind, but upon a crash!
    		
    		@Override
    		public void onServiceDisconnected(ComponentName name) {}
    	};
    	
    	try {
    		connected = context.bindService(new Intent("android.net.vpn.IVpnService"), mConnection, Context.BIND_AUTO_CREATE);
    	} 
    	catch (Exception e) {
			e.printStackTrace();
		} 

    	return connected;
    }
	
	public boolean connect(final VPNNetwork profile)
    {
    	boolean connected = false;

    	ServiceConnection mConnection = new ServiceConnection() {
    		@Override
    		public void onServiceConnected(ComponentName name, IBinder service) 
    		{
    			ApplicationInfo vpnAppInfo;
    	    	Context ctx = context.getApplicationContext();
    	    	PathClassLoader stubClassLoader;

    			try {
    				vpnAppInfo = ctx.getPackageManager().getApplicationInfo("com.android.settings", 0);
    		        stubClassLoader = new PathClassLoader(vpnAppInfo.sourceDir, ClassLoader.getSystemClassLoader());
    				Class<?> stubClass = Class.forName("android.net.vpn.IVpnService$Stub", true, stubClassLoader);
    				Method m = stubClass.getMethod("asInterface", IBinder.class);
    				Object theService = m.invoke(null, service);
    				
    				Class<?> vpnProfile = Class.forName("android.net.vpn.PptpProfile");
    				Object vpnInstance = vpnProfile.newInstance();
		    		
    				Method mm = vpnInstance.getClass().getMethod("setServerName", String.class);
		    		mm.invoke(vpnInstance, profile.getServer());

    				mm = vpnInstance.getClass().getMethod("setName", String.class);
		    		mm.invoke(vpnInstance, profile.getName());

		    		mm = stubClass.getMethod("connect", new Class[] { Class.forName("android.net.vpn.VpnProfile"), String.class, String.class });
		    		mm.invoke(theService, new Object[]{ vpnInstance, 
		    				  Encryption.decrypt(profile.getEncUsername(), prefs.getMasterPassword()), 
		    				  Encryption.decrypt(profile.getEncPassword(), prefs.getMasterPassword()) });
    			} 
    			catch (Exception e) {
    				e.printStackTrace();
    			}
    		}

    		// According to documentation, the following callback is not invoked
    		// upon unbind, but upon a crash!
    		
    		@Override
    		public void onServiceDisconnected(ComponentName name) {} 
    	};
    	
    	try {
    		connected = context.bindService(new Intent("android.net.vpn.IVpnService"), mConnection, Context.BIND_AUTO_CREATE);
    	} 
    	catch (Exception e) {
			e.printStackTrace();
		} 

    	return connected;
    }
}
