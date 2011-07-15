package baueran.AndroidSimpleVPN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;

public class AddVPNActivity extends Activity {
    static final String[] vpnTypes = new String[] {  };
    private ListView lv1;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.listlayout);
         
    	ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();

    	Map<String, String> m = new HashMap<String,String>();
        m.put("activity",    "Add PPTP VPN");
        m.put("explanation", "Point-to-Point Tunneling Protocol");
    	
        data.add(m);

        String[] from = { "activity", "explanation" };
        int[] to = { R.id.name_entry, R.id.type_entry };
        SimpleAdapter adapter = new SimpleAdapter(this, (List<? extends Map<String, ?>>) data, R.layout.doublelistviewitem, from, to);
   	
    	lv1 = (ListView)findViewById(R.id.listView1);
        lv1 = (ListView)findViewById(R.id.listView1);
        lv1.setAdapter(adapter);
    	  	
    	lv1.setTextFilterEnabled(true);
    	lv1.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		Intent intent = new Intent(Intent.ACTION_VIEW);
        		intent.setClassName(AddVPNActivity.this, AddPPTPVPNActivity.class.getName());
        		startActivity(intent);
        		finish();
    		}
     	});
    }
}
