package baueran.AndroidSimpleVPN;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;

public class AddVPNActivity extends Activity implements OnClickListener {
    static final String[] vpnTypes = new String[] {  };
    private ListView lv1;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.addvpnlayout);
    	
    	lv1 = (ListView)findViewById(R.id.listView1);
         
    	ArrayAdapter<String[]> adapter = new ArrayAdapter<String[]>(this, android.R.layout.simple_list_item_1);
    	String[] pptpEntry = new String[] { "Add PPTP VPN", "Point-to-Point Tunneling Protocol" };
    	String[] testEntry = new String[] { "Test", "Test" };
    	adapter.add(pptpEntry);
    	adapter.add(testEntry);
    	lv1.setAdapter(adapter);
    	
//    	lv1.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, vpnTypes));
    	lv1.setTextFilterEnabled(true);
    	lv1.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			// When clicked, show a toast with the TextView text
    			Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
    		}
     	});
    }
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}

}
