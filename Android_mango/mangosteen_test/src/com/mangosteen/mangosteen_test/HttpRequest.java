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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.util.Log;

public class HttpRequest extends AsyncTask<Void, byte[], Boolean>{
	
	String login;
	String password;
	private Boolean resultLogin = false;
	
	public HttpRequest(String login, String password) {
		// TODO Auto-generated constructor stub
		this.login = login;
		this.password = password;
	}

	protected Boolean doInBackground(Void... params) {
		try
		{
		        HttpClient client = new DefaultHttpClient();  
		        String postURL = "http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/login";
		        HttpPost post = new HttpPost(postURL); 
		        Log.i("HttpRequest", login + password);
		        List<NameValuePair> param = new ArrayList<NameValuePair>();
		        param.add(new BasicNameValuePair("email",login));
		        param.add(new BasicNameValuePair("pwd", password));
		        UrlEncodedFormEntity ent = new UrlEncodedFormEntity(param,HTTP.UTF_8);
		        post.setEntity(ent);
		        HttpResponse responsePOST = client.execute(post);  
		        HttpEntity resEntity = responsePOST.getEntity();
		        
		        /*
		        if (resEntity != null)
		        {    
		                Log.i("RESPONSE", EntityUtils.toString(resEntity));
		        }else{
		        	Log.i("RESPONSE", "Login Faile");
		        }
				*/
		        
		        final String json = EntityUtils.toString(resEntity); 
		        resultLogin = jsonParser(json);
		}
		catch (Exception e)
		{
		        e.printStackTrace();
		}
		return resultLogin;
	}
	
	
	public boolean getResult(){
		return resultLogin;
	}
	
	private Boolean jsonParser(String json) throws JSONException {
		
//		JSONArray jsonResult = new JSONArray(json);
        String resultLogin = "";
//                
//        for(int i=0; i<jsonResult.length(); i++){
//        	JSONObject jsonObject = jsonResult.getJSONObject(i);
//        	
//        	resultLogin += String.format("UserName : %s Email : %s", jsonObject.getString("UserName")
//        								,jsonObject.getString("Email"));
//        }
        
        JSONObject jsonResult = new JSONObject(json);
        resultLogin = jsonResult.getString("UserName");
        Log.i("Result Json", jsonResult.toString());
        
        if(resultLogin.equals(login)){
        	Log.i("Login Result", "Login Sucess");
        	return true;
        }else{
        	Log.i("Login Result", "Login Fail");
        	return false;
        }
	}

}