package com.example.snapnbuy;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SignUp extends ActionBarActivity implements OnClickListener
{
	Button btnSignUp,btnSignIn;
	EditText txtUserName,txtPassword;
	String userName,password;
	
	private Thread thread;
	private Handler handler = new Handler();
	
	private String webResponse = "";
    
    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		txtUserName=(EditText)findViewById(R.id.txtEmail);
        txtPassword=(EditText)findViewById(R.id.txtPassword);
        
        btnSignUp=(Button)findViewById(R.id.btnSignup);
        btnSignUp.setOnClickListener(this);
	}
	
	public void onClick(View view)
    {
    
	    if(view==btnSignUp)
		{
			try
			{
				userName = txtUserName.getText().toString().trim();
				password = txtPassword.getText().toString().trim();
				callInsertUser();
			}
			catch(Exception ex)
			{
				 ex.printStackTrace();
		          return;
			}
		}
    }
	
	public void callInsertUser()
    {
	   final String NAMESPACE = "http://ws.WebApp2.org";
 	   //final String URL = "http://10.0.2.2:8080/WebApp2/services/login?wsdl";
	   final String URL = "http://192.168.0.111:8080/WebApp2/services/login?wsdl";
 	   final String SOAP_ACTION = "http://ws.WebApp2.org/insertUser";
 	   final String METHOD_NAME = "insertUser";
 	   
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
		          
		        //Pass value for Password variable of the web service
		          PropertyInfo propPassword =new PropertyInfo();
		          propPassword.setName("password");
		          propPassword.setValue(password);
		          propPassword.setType(String.class);
		          request.addProperty(propPassword);
		          
		          
		          SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		          //envelope.dotNet = true;
		          envelope.setOutputSoapObject(request);
		          HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		          
		        
		          
		          androidHttpTransport.call(SOAP_ACTION, envelope);
		          SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
		          webResponse = response.toString();
		     }
		      
		     catch(Exception e){
		      e.printStackTrace();
		     }
		      
		     handler.post(insertUser);
		    }
		   };
		    
		   thread.start();
		  }
		   
		   
		  final Runnable insertUser = new Runnable()
		  {
		  
			   public void run()
			   {
				   if(webResponse.toLowerCase().equals("true"))
				   {	
					   //alert.showAlertDialog(SignUp.this, "SignUp Successful..", "Redirecting to Login", true);
					   AlertDialog alertDialog = new AlertDialog.Builder(SignUp.this).create();					   
				        // Setting Dialog Title
				        alertDialog.setTitle("SignUp Successful..");				  
				        // Setting Dialog Message
				        alertDialog.setMessage("Login to application");				  
				        // Setting alert dialog icon
				        alertDialog.setIcon(R.drawable.success);
				  
				        // Setting OK Button
				        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int which) {
				            	// Redirect to SignIn page
			                       Intent i = new Intent(getApplicationContext(), Signin.class);
			                       startActivity(i);
			                       finish();
				            }
				        });
				  
				        // Showing Alert Message
				        alertDialog.show();
				        
					
				   }
				   else
				   {
					// username / password doesn't match
                       alert.showAlertDialog(SignUp.this, "SignUp failed..", "Please SignUp again", false);
				   }
			   }
		   
		   };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);
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
