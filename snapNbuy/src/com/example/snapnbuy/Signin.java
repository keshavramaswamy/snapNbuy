package com.example.snapnbuy;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//Normal Login
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.*;
import org.ksoap2.transport.HttpTransportSE;
import com.example.snapnbuy.SessionManagement;
import com.example.snapnbuy.AlertDialogManager;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;



public class Signin extends ActionBarActivity implements OnClickListener,
ConnectionCallbacks, OnConnectionFailedListener 
{
	Button btnSignUp,btnSignIn;
	EditText txtUserName,txtPassword;
	String userName,password;
	
	private Thread thread;
	private Handler handler = new Handler();
	
	private String webResponse = "";
    // Session Manager Class
    SessionManagement session;
    
    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

	
	SignInButton btngooglesignin;
	
	String googleusername,gmailid;
	
	private boolean mIntentInProgress;
	 
    private boolean mSignInClicked;
 
    private ConnectionResult mConnectionResult;
    
    private static final int RC_SIGN_IN = 0;
	
	
	 protected static GoogleApiClient mGoogleApiClient;
	 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		txtUserName=(EditText)findViewById(R.id.txtUserName);
        txtPassword=(EditText)findViewById(R.id.txtPassword);
       
        
        
        btnSignUp=(Button)findViewById(R.id.btnSignUp);
        btnSignIn=(Button)findViewById(R.id.btnSignIn);
        btngooglesignin = (SignInButton) findViewById(R.id.btn_sign_in);
        
        btnSignUp.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        btngooglesignin.setOnClickListener(this);
        
         mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this).addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        
        // Session Manager
       session = new SessionManagement(getApplicationContext());
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
    	   //do nothing
       }
       
	}
    public void onClick(View view)
    {
    
	    if(view==btnSignUp)
		{
			try
			{
				// Redirect to Profile Setting page
                Intent i = new Intent(getApplicationContext(), SignUp.class);
                startActivity(i);
                finish();				
			}
			catch(Exception ex)
			{
				 ex.printStackTrace();
		          return;
			}
		}
	    
	    if(view==btnSignIn)
		{
			try
			{
				userName = txtUserName.getText().toString().trim();
				password = txtPassword.getText().toString().trim();
				callAuthenticateUser();
			}
			catch(Exception ex)
			{
				 ex.printStackTrace();
		          return;
			}
		}
	    
	    if(view==btngooglesignin)
	    {
	    	signInWithGplus();
          
	    }
	    
    }
    
    
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }
 
        if (!mIntentInProgress ){
            mConnectionResult = result;
 
            if (mSignInClicked) {
             resolveSignInError();
            }
        }
		
	}
	@Override
	public void onConnected(Bundle arg0) {
		    mSignInClicked = false;
	        Toast.makeText(this, "You are signed in", Toast.LENGTH_LONG).show();
	 
	        // Get user's information
	        getProfileInformation();
	 
	       
	       // call the next screen
	 
		
	}
    @Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
    	mGoogleApiClient.connect();
	}
	
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }
	
    @Override
    protected void onActivityResult(int requestCode, int responseCode,
            Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }
 
            mIntentInProgress = false;
 
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
 
                googleusername = personName;
                gmailid = email;
                
                Toast.makeText(this, "Hello "+googleusername+" "+gmailid, Toast.LENGTH_LONG).show();
                session.createLoginSession(googleusername,true);
                
                // Redirect to Profile Setting page
                Intent i = new Intent(getApplicationContext(), ProfileSetting.class);
                startActivity(i);
                finish();
                
                
            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public void callAuthenticateUser()
    {
       final String NAMESPACE = "http://ws.WebApp2.org";
 	   final String URL = "http://192.168.0.115:8080/WebApp2/services/login?wsdl";
 	   final String SOAP_ACTION = "http://ws.WebApp2.org/authenticateUser";
 	   final String METHOD_NAME = "authenticateUser";
 	   
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
		          
		          envelope.setOutputSoapObject(request);
		          HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		          
		        
		          
		          androidHttpTransport.call(SOAP_ACTION, envelope);
		          SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
		          webResponse = response.toString();
		     }
		      
		     catch(Exception e){
		      e.printStackTrace();
		     }
		      
		     handler.post(authenticateUser);
		    }
		   };
		    
		   thread.start();
		  }
		   
		   
		  final Runnable authenticateUser = new Runnable()
		  {
		  
			   public void run()
			   {
			    
				   if(webResponse.toLowerCase().equals("true"))
				   {
					   session.createLoginSession(userName,false);
                       
                       // Redirect to Profile Setting page
                       Intent i = new Intent(getApplicationContext(), ProfileSetting.class);
                       startActivity(i);
                       finish();
				   }
				   else
				   {
					// username / password doesn't match
                       alert.showAlertDialog(Signin.this, "Login failed..", "Username/Password is incorrect", false);
				   }
			   }
		   
		   };
}
    
    