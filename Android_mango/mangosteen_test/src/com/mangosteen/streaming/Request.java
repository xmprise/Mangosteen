package com.mangosteen.streaming;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class Request {
	private InputStream input;
	private String uri;
	
	public Request(InputStream input){
		this.input = input;
	}
	public int parse(){
		//소켓으로부터 일련의 문자들을 읽음
		StringBuffer request = new StringBuffer(2048);
		int i, result;
		byte[] buffer = new byte[2048];
		try{
			i = input.read(buffer);
		}
		catch(IOException e){
			e.printStackTrace();
			result = i = -1;
		}
		for(int j=0; j<i; j++){
			request.append((char)buffer[j]);
		}
//		Log.i("Http server", request.toString());
		if(request.toString().contains("MSIE"))
			result = 1;
		else result = 0;
		uri = parseUri(request.toString());
		return result;
		
	}
	private String parseUri(String requestString){
		int index1, index2;
		index1 = requestString.indexOf(' ');
		if (index1 != -1){
			index2  = requestString.indexOf(' ', index1 + 1);
			if(index2 > index1)
				return requestString.substring(index1 + 1, index2);
		}
		return null;
	}
	public String getUri(){
		return uri;
	}
}
