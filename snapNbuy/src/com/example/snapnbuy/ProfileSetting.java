package com.example.snapnbuy;

import com.example.snapnbuy.SessionManagement;
import com.example.snapnbuy.R;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class ProfileSetting extends ActionBarActivity implements OnClickListener
{
	Spinner spinnerProfile;
	Button btnOk;
	String profile;
	// Session Manager Class
    SessionManagement session;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_setting);
		
		// Session class instance
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();
        
		spinnerProfile = (Spinner) findViewById(R.id.spinnerProfile);
		btnOk=(Button)findViewById(R.id.btnOk);
		
		
		btnOk.setOnClickListener(this);
		
		setProfile();
	}
	public void onClick(View view)
    {
    
	    if(view==btnOk)
		{
			try
			{
				profile = spinnerProfile.getSelectedItem().toString();
				// Redirect to Profile Setting page
                Intent i = new Intent(getApplicationContext(), BarcodeScanning.class);
              //Create the bundle
				  Bundle bundle = new Bundle();
				  //Add your data to bundle
				  bundle.putString("profile", profile);  
				  //Add the bundle to the intent
				  i.putExtras(bundle);
                startActivity(i);
                finish();
				
			}
			catch(Exception ex)
			{
				 ex.printStackTrace();
		          return;
			}
		}
    }
	public void setProfile()
	{
		
		try
		{
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.profile, android.R.layout.simple_spinner_dropdown_item);					
			spinnerProfile.setAdapter(adapter);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile_setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
