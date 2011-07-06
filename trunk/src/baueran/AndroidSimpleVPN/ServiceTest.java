package baueran.AndroidSimpleVPN;

import baueran.AndroidSimpleVPN.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class ServiceTest extends Activity implements OnClickListener
{
    static final String[] accounts = new String[] { };
    private ListView lv1;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);

        lv1 = (ListView)findViewById(R.id.listView1);
        lv1.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, accounts));
        lv1.setTextFilterEnabled(true);
        lv1.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // When clicked, show a toast with the TextView text
            Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
          }
        });

        Button button = (Button) this.findViewById(R.id.start);
        button.setOnClickListener(this);
    }

    @Override
	public void onClick(View view) 
	{
		if (view == findViewById(R.id.start)) {
	        Intent intent = new Intent();
	        intent.setAction("com.baueran.test.ServiceTest.MyService");
	        this.getApplicationContext().startService(intent);
		}
	}
}