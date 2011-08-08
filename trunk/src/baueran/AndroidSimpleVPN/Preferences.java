package baueran.AndroidSimpleVPN;

public class Preferences
{
	private String masterPassword = new String();
	private int masterPasswordRowId = -1;
	private static Preferences instance = null;
	
	private Preferences() {}
	
	public static Preferences getInstance()
	{
		if (instance == null)
			instance = new Preferences();
		return instance;
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
