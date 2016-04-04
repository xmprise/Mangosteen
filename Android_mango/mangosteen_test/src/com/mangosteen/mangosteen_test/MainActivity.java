package com.mangosteen.mangosteen_test;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	SharedPreferences pref;
	SharedPreferences.Editor edit;
	CheckBox checkUser;
	Button login_btn;
	Boolean idCheck;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //스플레쉬 화면
    	startActivity(new Intent(this, ShpashActivity.class));
    	
    	pref = getSharedPreferences("LoginPref", 0);
        edit = pref.edit();
        
        initialize();
         
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        login_btn = (Button)findViewById(R.id.loginBtn);
        checkUser = (CheckBox)findViewById(R.id.checkID);
        
        
        checkUser.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					idCheck = true;
				}else{
					idCheck = false;
					//Preferences clear
					edit.clear();
			        edit.commit();
				}
			}
		});
        
        login_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText ediLogin = (EditText)findViewById(R.id.LoginText);
				EditText ediPass = (EditText)findViewById(R.id.passText);
				
				String login = ediLogin.getText().toString();
				String password = ediPass.getText().toString();
				
				String UserName = pref.getString("UserName", "");
				
				//Log.i("MainActivity", login + ":" + password);
				LoginRequest request = new LoginRequest(login, password, MainActivity.this);
				request.execute();
				
			}
		});
        
    }
    
    private void initialize() {
		// TODO Auto-generated method stub
    	InitializationRunnable init = new InitializationRunnable();
        new Thread(init).start();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	private class InitializationRunnable implements Runnable
    {
        public void run()
        {
            // do_something
        	String UserName = pref.getString("UserName", "");
        	Log.i("InitializationRunnable", UserName);
        	if(pref.getString("UserName", "")!=""){
        		Intent appServer = new Intent(MainActivity.this, MangosteenActivity.class);
            	appServer.putExtra("UserName", UserName);
            	startActivity(appServer);
        	}
        }
    }
	
    private class LoginRequest extends AsyncTask<Void, byte[], Boolean>{
    	
    	String login;
    	String password;
    	
    	Context ctx;
		
    	public LoginRequest(String login, String password, Context ctx) {
    		// TODO Auto-generated constructor stub
    		this.login = login;
    		this.password = password;
    		this.ctx = ctx;
    	}

    	protected Boolean doInBackground(Void... params) {
    		try
    		{
    		        HttpClient client = new DefaultHttpClient();  
    		        String postURL = "http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/login";
    		        HttpPost post = new HttpPost(postURL); 
    		        Log.i("HttpRequest", login + password);
    		        //login="jung"; //삭제
    		        //password="2114"; //삭제
    		        List<NameValuePair> param = new ArrayList<NameValuePair>();
    		        param.add(new BasicNameValuePair("email",login));
    		        param.add(new BasicNameValuePair("pwd", password));
    		        UrlEncodedFormEntity ent = new UrlEncodedFormEntity(param,HTTP.UTF_8);
    		        post.setEntity(ent);
    		        HttpResponse responsePOST = client.execute(post);  
    		        HttpEntity resEntity = responsePOST.getEntity();
    		            		        
    		        final String json = EntityUtils.toString(resEntity); 
    		        jsonParser(json);
    		}
    		catch (Exception e)
    		{
    		        e.printStackTrace();
    		}
    		return null;
    	}
    	

    	private void jsonParser(String json) throws JSONException {
    		
            String resultLogin = "";
            
            JSONObject jsonResult = new JSONObject(json);
            resultLogin = jsonResult.getString("UserName");
            Log.i("Result Json", jsonResult.toString());
            
            if(resultLogin.equals(login)){
            	Log.i("Login Result", "Login Sucess");
            	Intent appServer = new Intent(ctx, MangosteenActivity.class);
            	appServer.putExtra("UserName", resultLogin);
            	
            	if(idCheck == true) {
            		edit.putString("UserName", resultLogin);
            		edit.commit();
            	}else{
            		edit.clear();
            		edit.commit();
            	}
            	String UserName = pref.getString("UserName", "");
             	Log.i("InitializationRunnable", UserName); 
            	startActivity(appServer);
            }else{
            	Log.i("Login Result", "Login Fail");
            	Toast.makeText(ctx, "Login Fail", Toast.LENGTH_SHORT).show();
            }
    	}

    }
}
