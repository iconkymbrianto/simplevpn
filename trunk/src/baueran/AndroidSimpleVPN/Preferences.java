package baueran.AndroidSimpleVPN;

import java.security.NoSuchAlgorithmException;

public class Preferences
{
	// The md5 encrypted master password
	private String encPassword = null;
	private int encPasswordRowId = -1;
	// Stores the name of the currently connected network, 
	// empty if no network is connected.
	private String currentlyConnectedNetwork = null;
	private static Preferences instance = null;
	
	private Preferences() 
	{
		encPassword = new String();
		currentlyConnectedNetwork = new String();
	}
	
	public static Preferences getInstance()
	{
		if (instance == null)
			instance = new Preferences();
		return instance;
	}
	
	// Returns true if the md5 of string is equivalent to the md5
	// of the stored master password.
	
	boolean equalsPass(String string)
	{
		try {
			if (Encryption.md5(string).equals(getEncMasterPassword()))
				return true;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
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

	public void setEncMasterPasswordRowId(int encMasterPasswordRowId)
	{
		this.encPasswordRowId = encMasterPasswordRowId;
	}
	
	public int getEncMasterPasswordRowId() 
	{
		return encPasswordRowId;
	}
	
	public void setEncMasterPassword(String encMasterPassword)
	{
		this.encPassword = encMasterPassword;
	}
	
	public String getEncMasterPassword() {
		return encPassword;
	}
}
