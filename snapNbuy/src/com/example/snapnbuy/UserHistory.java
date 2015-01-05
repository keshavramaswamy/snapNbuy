package com.example.snapnbuy;

import java.util.HashMap;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.example.snapnbuy.R;
import com.example.snapnbuy.SessionManagement;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UserHistory extends ActionBarActivity
{
	// Session Manager Class
    SessionManagement session;
    String userName;
    private Thread thread;
    private Handler handler = new Handler();
    String[] userHistory;
    ListView lstuserHistory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_history);
		
		// Session class instance
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();
		
		// get user data from session
        HashMap<String, String> user = session.getUserDetails();
         
        // name
        userName = user.get(SessionManagement.KEY_USERNAME);
        lstuserHistory = (ListView) findViewById(R.id.lstuserHistory);
        getUserHistory();
	}
	
	public void setHistory()
	{
		try
		{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this ,android.R.layout.simple_list_item_1,userHistory);			
		lstuserHistory.setAdapter(adapter);
		
		}
		catch(Exception e)
		{
		      e.printStackTrace();
		     }
		
	}

	public void getUserHistory()
    {
    	final String NAMESPACE = "http://ws.WebApp2.org";
 	   final String URL = "http://192.168.0.115:8080/WebApp2/services/User?wsdl";
 	   final String SOAP_ACTION = "http://ws.WebApp2.org/";
 	   final String METHOD_NAME = "getUserHistory";
 	   
		thread = new Thread()
		 {
		    public void run(){
		     try{
		       SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 
		       
		     //Pass value for userName variable of the web service
		          PropertyInfo propUserName =new PropertyInfo();
		          propUserName.setName("userName");//Define the variable name in the web service method
		          propUserName.setValue(userName);//set value for userName variable
		          propUserName.setType(String.class);//Define the type of the variable
		          request.addProperty(propUserName);//Pass properties to the variable								          
		        							          
		          
		          SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		          //envelope.dotNet = true;
		          envelope.setOutputSoapObject(request);
		          HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		          
		        
		          
		          androidHttpTransport.call(SOAP_ACTION, envelope);
		          //SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
		          //webResponse = response.toString();
		          SoapObject result = (SoapObject)envelope.bodyIn;
		          

		          userHistory = new String[result.getPropertyCount()];
		              for(int i= 0; i< result.getPropertyCount(); i++)
		              {
		                  userHistory[i]= result.getProperty(i).toString();
		              }
		              
		          //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this ,android.R.layout.simple_list_item_1,userHistory);
		          //lstuserHistory.setAdapter(adapter);										   
		     }
		      
		     catch(Exception e){
		      e.printStackTrace();
		     }
		      
		     handler.post(getUserHistory);
		     
		     								   
		    }
		   };
		    
		   thread.start();
		
		  }
		   
		   
		  final Runnable getUserHistory = new Runnable()
		  {
		  
			   public void run()
			   {									    
				   setHistory();
			   }
		   
		   };
		   
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_history, menu);
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
