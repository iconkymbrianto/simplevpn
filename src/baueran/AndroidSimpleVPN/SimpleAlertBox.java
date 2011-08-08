package baueran.AndroidSimpleVPN;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class SimpleAlertBox
{
	
	public static void display(String title, String message, Context context)
	{
		display(title, message, "Ok", context);
	}
	
	public static void display(String title, String message, String buttonText, Context context)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Cannot store profile data");
		builder.setMessage("Encryption of username failed.");
		builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
