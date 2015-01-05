package com.example.snapnbuy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.example.snapnbuy.MainActivity;
import com.example.snapnbuy.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BarcodeScanning extends ActionBarActivity implements OnClickListener,OnNavigationListener,ConnectionCallbacks, OnConnectionFailedListener 
{
	private Button btnScan,btnHistory;
	private TextView formatTxt, contentTxt;
	String profile,barcodeId,userName;
	// Session Manager Class
    SessionManagement session;
    private Thread thread;
	private Handler handler = new Handler();
	private String webResponse;
	
	private boolean isGoogleLogin;
	
	//Image Processing
	String encoded;	
	private File photoFile = null;
	String mCurrentPhotoPath;
	static final int REQUEST_IMAGE_CAPTURE=1;
	byte[] s;
	int IngredientCount;
	StringBuffer Ingredient;
	//Image Processing
	//private String[] harmfulIngredient;
	private StringBuffer harmfulIngredient;
	private int harmfulIngredientCount;
	private int goodToGo=0;
	
	private final int scan_for_product = 100;
	private final int scan_for_ingredient = 101;
	
	// Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();
    
    //Google - Start
SignInButton btngooglesignin;
	
	String googleusername,gmailid;
	
	private boolean mIntentInProgress;
	 
    private boolean mSignInClicked;
 
    private ConnectionResult mConnectionResult;
    
    private static final int RC_SIGN_IN = 0;
	
	
	 protected static GoogleApiClient mGoogleApiClient;

    //Google - End
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_scanning);
		
		final ActionBar actionBar = getActionBar();

    	//actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
       ArrayList<String> itemList = new ArrayList<String>();
   
        itemList.add("");
        itemList.add("Change Profile");
        itemList.add("Logout");
        
        ArrayAdapter<String> aAdpt = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , android.R.id.text1, itemList);
       
        actionBar.setListNavigationCallbacks(aAdpt, this);

		
		// Session class instance
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        HashMap<String, Boolean> userLogin = session.getGoogleLoginDetails(); 
        // name
        userName = user.get(SessionManagement.KEY_USERNAME);
        isGoogleLogin = userLogin.get(SessionManagement.KEY_ISGOOGLELOGIN);
        
		btnScan = (Button)findViewById(R.id.scan_button);
		btnHistory = (Button)findViewById(R.id.btnHistory);
		
		formatTxt = (TextView)findViewById(R.id.scan_format);
		contentTxt = (TextView)findViewById(R.id.scan_content);
		btnScan.setOnClickListener(this);
		btnHistory.setOnClickListener(this);
		
		//Get the bundle
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        profile = bundle.getString("profile");
        
     // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this).addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN).build();

	}
	
	//For Google--Start
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
                //mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (Exception e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
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
	        //Toast.makeText(this, "You are signed in", Toast.LENGTH_LONG).show();
	 
	        // Get user's information
	        //getProfileInformation();
	 
	       
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
	
 
	//For Google--End

	@Override
	   
    public boolean onNavigationItemSelected(int position, long id) 
	{
    	switch(position)
    	{
    	case 1:
    		// Redirect to Profile Setting page
            Intent i = new Intent(getApplicationContext(), ProfileSetting.class);
            startActivity(i);
            finish();
    	break;
    	case 2:
    		if(isGoogleLogin==false)
    		{
    		session.logoutUser();
    		}
    		else
    		{
    				 
    		}
    		
    	break;
    	}
		return false;    
	}   


	
	public void onClick(View v)
	{
		if(v==btnScan)
		{
			Intent intent = new Intent("com.google.zxing.client.android.SCAN"); 
			intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE"); 
			startActivityForResult(intent, scan_for_product);
			//barcodeId = "070650800541";
			//updateUserHistory();
			//checkProfile();
		}
		
		if(v==btnHistory)
		{
			// Redirect to User History page
            Intent i = new Intent(getApplicationContext(), UserHistory.class);
            startActivity(i);
            finish();
		}

	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		switch(requestCode)
		{
		case scan_for_product:
			if(requestCode == scan_for_product && resultCode == RESULT_OK)   
			{  
				String contents = intent.getStringExtra("SCAN_RESULT"); 
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT"); 
				formatTxt.setText("FORMAT: " + format);
				contentTxt.setText("CONTENT: " + contents);
				
				barcodeId = contents;
				checkProductExists();			
				//updateUserHistory();
				//checkProfile();
	        }         
		   else if(resultCode == RESULT_CANCELED)     
		   {              // Handle cancel             Log.i("xZing", "Cancelled");       
			   
		   }
		case scan_for_ingredient:
			if (requestCode == scan_for_ingredient && resultCode == RESULT_OK) 
			{
				postdata();
			}

		}
	}
	
	public void checkProductExists()
	{
			final String NAMESPACE = "http://ws.WebApp2.org";
	 	   final String URL = "http://192.168.0.115:8080/WebApp2/services/Product?wsdl";
	 	   final String SOAP_ACTION = "http://ws.WebApp2.org/";
	 	   final String METHOD_NAME = "checkProduct";
	 	   
			thread = new Thread()
			 {
			    public void run(){
			     try{
			       SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 
			       
			       	//Pass value for userName variable of the web service
			          PropertyInfo propBarcodeId =new PropertyInfo();
			          propBarcodeId.setName("barcodeId");//Define the variable name in the web service method
			          propBarcodeId.setValue(barcodeId);//set value for userName variable
			          propBarcodeId.setType(String.class);//Define the type of the variable
			          request.addProperty(propBarcodeId);//Pass properties to the variable			          
			          	        							          
			          
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
			      
			     handler.post(checkProductExists);
			     
			     								   
			    }
			   };
			    
			   thread.start();
			
			  }
			   
			   
			  final Runnable checkProductExists = new Runnable()
			  {
			  
				   public void run()
				   {									    
					   if(webResponse.toLowerCase().equals("false"))
					   {						                    
						 //alert.showAlertDialog(SignUp.this, "SignUp Successful..", "Redirecting to Login", true);
						   AlertDialog alertDialog = new AlertDialog.Builder(BarcodeScanning.this).create();					   
					        // Setting Dialog Title
					        alertDialog.setTitle("Product not found..");				  
					        // Setting Dialog Message
					        alertDialog.setMessage("Please Scan the product ingredients to continue");				  
					        // Setting alert dialog icon
					        alertDialog.setIcon(R.drawable.wrong);
					  
					        // Setting OK Button
					        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					            public void onClick(DialogInterface dialog, int which) {
					            	takePicture();
					            }
					        });
					  
					        // Showing Alert Message
					        alertDialog.show();
					   }
					   else
					   {
						   updateUserHistory();
						   checkProfile();
					   }
				   }
			   
			   };
			   
	//Image Processing - Start
	//static final int REQUEST_TAKE_PHOTO = 1;
	public void takePicture()
    {    	
    	Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            //File photoFile=null;
            try {
                photoFile = createImageFile();
                
           } catch (IOException ex)
           {
                // Error occurred while creating the File
               
            }
           
           
            // Continue only if the File was successfully created
            if (photoFile != null) {
            
             takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
             Uri.fromFile(photoFile));
             startActivityForResult(takePictureIntent, scan_for_ingredient);
             
            }
          
        }
    }
    
    

    public void postdata()
    {
    	try
    	{
    	s=getImageBytes(photoFile);
        //encoded= new String(s, "ISO-8859-1");
        getIngredientsFromOcr();
    	}
    	catch(IOException ex)
    	{
    		
    	}
    }
    
    public void getIngredientsFromOcr()
    {
    	final String NAMESPACE = "http://ws.WebApp2.org";
  	   final String URL = "http://192.168.0.115:8080/WebApp2/services/OcrProcess?wsdl";
  	   final String SOAP_ACTION = "http://ws.WebApp2.org/processImage";
  	   final String METHOD_NAME = "processImage";
  	   
 		thread = new Thread()
 		 {
 		    public void run(){
 		     try{
 		       SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 
 		       
 		     //Pass value for userName variable of the web service
 		          PropertyInfo propUserString =new PropertyInfo();
 		         propUserString.setName("imageBytes");//Define the variable name in the web service method
 		        propUserString.setValue(s);//set value for userName variable
 		       propUserString.setType(ByteArrayInputStream.class);//Define the type of the variable
 		          request.addProperty(propUserString);//Pass properties to the variable
 		          

 		          
 		          
 		          SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
 		         new MarshalBase64().register(envelope);
 		         
 		          //envelope.dotNet = true;
 		          envelope.setOutputSoapObject(request);
 		          HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
 		          
 		        
 		          
 		          androidHttpTransport.call(SOAP_ACTION, envelope);
 		          //SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
 		          //webResponse = response.toString();
 		          SoapObject result = (SoapObject)envelope.bodyIn;
 		          IngredientCount = result.getPropertyCount();
 		          
		        	Ingredient = new StringBuffer();
			          for(int i= 0; i< result.getPropertyCount(); i++)
			          {
			        	  Ingredient.append("-"+result.getProperty(i).toString()+"\n"); 

			          }
 		     }
 		      
 		     catch(Exception e){
 		      e.printStackTrace();
 		     }
 		      
 		     handler.post(getIngredientsFromOcr);
 		    }
 		   };
 		    
 		   thread.start();
 		  }
 		   
 		   
 		  final Runnable getIngredientsFromOcr = new Runnable()
 		  {
 		  
 			   public void run()
 			   {
 			    //lblResult.setText("Login Result"+webResponse);
 				  alert.showAlertDialog(BarcodeScanning.this, "Ingredients", Ingredient.toString(), true);
 				   /*if(webResponse.toLowerCase().equals("true"))
 				   {
 					   session.createLoginSession(userName,password);
                        
                        // Staring MainActivity
                        Intent i = new Intent(getApplicationContext(), LoggedIn.class);
                        startActivity(i);
                        finish();
 				   }
 				   else
 				   {
 					// username / password doesn't match
                        alert.showAlertDialog(MainActivity.this, "Login failed..", "Username/Password is incorrect", false);
 				   }*/
 			   }
 		   
 		   };
    
    private File createImageFile() throws IOException {
        try
        {
    	// Create an image file name
    	
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
       // this.photoFile=image;
        return image;
        }
        
        catch(IOException ex)
        {
        	ex.printStackTrace();
        	return null;

        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        	return null;
        }
    }
    
    
    	private byte[] getImageBytes (File f) throws IOException
		{
			//URL resource = this.getClass().getResource("/snapNbuyWS/test.jpg");
			
			URL resource=f.toURL();
		    InputStream in = resource.openStream();
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    byte[] buf = new byte[1024];
		    for(int read; (read = in.read(buf)) != -1;) {
		        bos.write(buf, 0, read);
		    }
		    return bos.toByteArray();
		}
    	//Image Processing - End
	public void updateUserHistory()
	{
			final String NAMESPACE = "http://ws.WebApp2.org";
	 	   final String URL = "http://192.168.0.115:8080/WebApp2/services/User?wsdl";
	 	   final String SOAP_ACTION = "http://ws.WebApp2.org/";
	 	   final String METHOD_NAME = "updateUserHistory";
	 	   
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
			          
			          //Pass value for barcodeId variable of the web service
			          PropertyInfo propbarcodeId =new PropertyInfo();
			          propbarcodeId.setName("barcodeId");//Define the variable name in the web service method
			          propbarcodeId.setValue(barcodeId);//set value for userName variable
			          propbarcodeId.setType(String.class);//Define the type of the variable
			          request.addProperty(propbarcodeId);//Pass properties to the variable
			        							          
			          
			          SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			          //envelope.dotNet = true;
			          envelope.setOutputSoapObject(request);
			          HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			          
			        
			          
			          androidHttpTransport.call(SOAP_ACTION, envelope);
			          SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
			          //webResponse = response.toString();		          			              
			          										   
			     }
			      
			     catch(Exception e){
			      e.printStackTrace();
			     }
			      
			     handler.post(updateUserHistory);
			     
			     								   
			    }
			   };
			    
			   thread.start();
			
			  }
			   
			   
			  final Runnable updateUserHistory = new Runnable()
			  {
			  
				   public void run()
				   {									    
					   //setHistory();
				   }
			   
			   };

	public void checkProfile()
    {
	   
    	final String NAMESPACE = "http://ws.WebApp2.org";
 	   	final String URL = "http://192.168.0.115:8080/WebApp2/services/Profile?wsdl";
 	   	final String SOAP_ACTION = "http://ws.WebApp2.org/checkProfile";
 	   	final String METHOD_NAME = "checkProfile";
 	   
		thread = new Thread()
		 {
		    public void run(){
		     try{
		       SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME); 
		       
		     //Pass value for barcode variable of the web service
		          PropertyInfo propBarcode =new PropertyInfo();
		          propBarcode.setName("barcodeId");//Define the variable name in the web service method
		          propBarcode.setValue(barcodeId);//set value for userName variable
		          propBarcode.setType(String.class);//Define the type of the variable
		          request.addProperty(propBarcode);//Pass properties to the variable
		          
		        //Pass value for profile variable of the web service
		          PropertyInfo propProfile =new PropertyInfo();
		          propProfile.setName("profileName");
		          propProfile.setValue(profile);
		          propProfile.setType(String.class);
		          request.addProperty(propProfile);
		          
		          
		          SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		          //envelope.dotNet = true;
		          envelope.setOutputSoapObject(request);
		          HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		          
		        
		          
		          androidHttpTransport.call(SOAP_ACTION, envelope);
		          //SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
		          //result = (List<String>) envelope.getResponse();						          			          
		          //webResponse = response.toString();
		          SoapObject result = (SoapObject)envelope.bodyIn;
		          harmfulIngredientCount = result.getPropertyCount();
		          if(harmfulIngredientCount==0)
		          {
		        	  goodToGo=1;
		          }
		          else
		          {
		          //harmfulIngredient = new String[result.getPropertyCount()];
		        	harmfulIngredient = new StringBuffer();
		          for(int i= 0; i< result.getPropertyCount(); i++)
		          {
		        	  harmfulIngredient.append("-"+result.getProperty(i).toString()+"\n"); 

		          }
		          //webResponse = "Hi";
		          }
		     }
		      
		     catch(Exception e){
		      e.printStackTrace();
		     }
		      
		     handler.post(checkProfile);
		    }
		   };
		    
		   thread.start();
		  }
		   
		   
		  final Runnable checkProfile = new Runnable()
		  {
		  
			   public void run()
			   {
				  				   
				   if(goodToGo==1)
				   {
					   
					   alert.showAlertDialog(BarcodeScanning.this, "GOOD TO GO:",
							   "The scanned product has no harmful Ingredients or has harmful Ingredients " +
							   "under safe levels for selected profile", true);					   
				   }
				   else
				   {
					   
					   alert.showAlertDialog(BarcodeScanning.this, "Warning:",
							   "The scanned product has high levels of following ingredients for selected profile." +
							   "\n\n"+harmfulIngredient, false);					   
				   }   
				   
			   }
		   };
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.barcode_scanning, menu);
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