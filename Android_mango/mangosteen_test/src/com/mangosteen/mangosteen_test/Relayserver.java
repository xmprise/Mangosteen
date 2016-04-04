package com.mangosteen.mangosteen_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;
import org.eclipse.jetty.util.Utf8StringBuffer;

import android.R.array;
import android.R.integer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml.Encoding;

public class Relayserver implements Runnable {
	private Socket Relaysock;
	private String host = "210.118.69.49";
	private String me = "127.0.0.1";
	private int port = 19999;
	private int clientPort = 0;
	private String TAG = "RelayServer";
	private boolean ThreadOn = true;
	private byte[] buffer;
	private Handler handler;
	
	
	public Relayserver(Handler handler) {
		this.handler = handler;
		
	}

	public void StopThread() {
		try {
			ThreadOn = false;
			if (Relaysock != null)
			{
				Relaysock.getOutputStream().write("stop".getBytes());
				Relaysock.close();
			}
			for(int i=0; i< threadList.size(); i++)
			{
				if(threadList.get(i) != null)
				{
					threadList.get(i).interrupt();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	List<Thread> threadList = new ArrayList<Thread>();
	public void run() {
		InputStream relayinput = null;
		int size;
		try {
			Relaysock = new Socket(host, port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			relayinput = Relaysock.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		buffer = new byte[4];
		try {
			Message client_port = new Message();
			size = relayinput.read(buffer);
			clientPort = byteArrayToInt(buffer);
			client_port.what = clientPort;
			handler.sendMessage(client_port);
			Log.e("clientPort", "" + clientPort);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (ThreadOn) {
			try {
				size = relayinput.read(buffer, 0, 4);
				if (size == 4) {
					port = byteArrayToInt(buffer);
					Log.e("PORT NUM", "" + port);
					Relayhandler relayhandler = new Relayhandler(port);
					Thread ddthread = new Thread(relayhandler);
					threadList.add(ddthread);
					ddthread.start();
				}
//				else
//				{
//					for(int i=0; i < size/4; i++)
//					{
//						byte[] temp = new byte[4];
//						System.arraycopy(buffer, i*4, temp, 0, 4);
//						port = byteArrayToInt(temp, 4);
//						Log.e("PORT NUM", "" + port);
//						Relayhandler relayhandler = new Relayhandler(port);
//						Thread ddthread = new Thread(relayhandler);
//						ddthread.start();
//						
//					}
//				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private int byteArrayToInt(byte[] bytes) {
		int newValue = 0;

		if (bytes[0] < 0)
			newValue += (int) bytes[0] + 256;
		else
			newValue += (int) bytes[0];
		if (bytes[1] < 0)
			newValue += ((int) bytes[1] + 256) * 256;
		else
			newValue += ((int) bytes[1]) * 256;
		if (bytes[2] < 0)
			newValue += ((int) bytes[2] + 256) * 256 * 256;
		else
			newValue += ((int) bytes[2]) * 256 * 256;
		if (bytes[3] < 0)
			newValue += ((int) bytes[3] + 256) * 256 * 256;
		else
			newValue += ((int) bytes[3]) * 256 * 256;

		return newValue;
	}
}