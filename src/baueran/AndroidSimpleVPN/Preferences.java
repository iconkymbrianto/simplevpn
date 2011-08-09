package baueran.AndroidSimpleVPN;

public class Preferences
{
	private String masterPassword = null;
	private int masterPasswordRowId = -1;
	// Stores the name of the currently connected network, 
	// empty if no network is connected.
	private String currentlyConnectedNetwork = null;
	private static Preferences instance = null;
	
	private Preferences() 
	{
		masterPassword = new String();
		currentlyConnectedNetwork = new String();
	}
	
	public static Preferences getInstance()
	{
		if (instance == null)
			instance = new Preferences();
		return instance;
	}
	
	public String currentlyConnectedNetwork()
	{
		return currentlyConnectedNetwork;
	}
	
	public void unsetCurrentlyConnectedNetwork()
	{
		currentlyConnectedNetwork = new String();
	}

	public void setCurrentlyConnectedNetwork(String c)
	{
		currentlyConnectedNetwork = c;
	}

	public void setMasterPasswordRowId(int masterPasswordRowId)
	{
		this.masterPasswordRowId = masterPasswordRowId;
	}
	
	public int getMasterPasswordRowId() 
	{
		return masterPasswordRowId;
	}
	
	public void setMasterPassword(String masterPassword)
	{
		this.masterPassword = masterPassword;
	}
	
	public String getMasterPassword() {
		return masterPassword;
	}
}
