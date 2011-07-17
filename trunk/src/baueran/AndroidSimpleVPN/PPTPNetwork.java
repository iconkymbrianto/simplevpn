package baueran.AndroidSimpleVPN;

public class PPTPNetwork extends VPNNetwork
{
	private boolean encEnabled = false;

	public void setEncEnabled(boolean encEnabled)
	{
		this.encEnabled = encEnabled;
	}

	public boolean isEncEnabled()
	{
		return encEnabled;
	}
}
