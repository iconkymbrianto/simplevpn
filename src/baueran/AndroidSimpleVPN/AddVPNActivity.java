package baueran.AndroidSimpleVPN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
    	setContentView(R.layout.listlayout);
         
    	ArrayList<Map> data = new ArrayList<Map>();
    	
        // add some objects into the array list
        Map m = new HashMap();
        m.put("activity", "Add PPTP VPN");
        m.put("explanation", "Point-to-Point Tunneling Protocol");
    	
        data.add(m);

        // setup the data adaptor
        String[] from = { "activity", "explanation" };
        int[] to = { R.id.name_entry, R.id.type_entry };
        SimpleAdapter adapter = new SimpleAdapter(this, (List<? extends Map<String, ?>>) data, R.layout.doublelistviewitem, from, to);
   	
    	lv1 = (ListView)findViewById(R.id.listView1);
        lv1 = (ListView)findViewById(R.id.listView1);
        lv1.setAdapter(adapter);
    	  	
    	lv1.setTextFilterEnabled(true);
    	lv1.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			// Change activity
        		Intent intent = new Intent(Intent.ACTION_VIEW);
        		intent.setClassName(AddVPNActivity.this, AddPPTPVPNActivity.class.getName());
        		startActivity(intent);
        		
    			// When clicked, show a toast with the TextView text
//    			Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
    		}
     	});
    }
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}

}
