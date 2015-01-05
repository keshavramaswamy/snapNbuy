package com.example.snapnbuy;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;


public class MainActivity extends ActionBarActivity
{
	private static int SPLASH_TIME_OUT = 2000;
	// Session Manager Class
    SessionManagement session;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
     // Session Manager
        session = new SessionManagement(getApplicationContext());
      //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {       	 
            @Override
            public void run() {
                //session.checkLogin();
                if(session.isLoggedIn())
                {
             	   // Redirect to Profile Setting page
                    Intent i = new Intent(getApplicationContext(), ProfileSetting.class);
                    startActivity(i);
                    finish();  
                }
                else
                {
                	// Start your app main activity
                    Intent i = new Intent(MainActivity.this, Signin.class);
                    startActivity(i);
                     // close this activity
                    finish();
                }
            }
        }, SPLASH_TIME_OUT); 
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
