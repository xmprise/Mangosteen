package com.mangosteen.mangosteen_test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.*;
import java.net.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jetty.util.IO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mem.mangosteen.Installer;
import org.mem.mangosteen.MangosteenService;
import org.mem.mangosteen.log.AndroidLog;
import org.apache.http.conn.util.*;

import com.mangosteen.streaming.MjpegActivity;

import io.socket.SocketIO;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MangosteenActivity extends Activity implements MangoSocketCallbackAdapter{
	private static final String TAG = "Jetty";

	public static final String __START_ACTION = "org.mem.mangosteen.start";
	public static final String __STOP_ACTION = "org.mem.mangosteen.stop";

	public static final String __PORT = "org.mem.mangosteen.port";
	public static final String __NIO = "org.mem.mangosteen.nio";
	public static final String __SSL = "org.mem.mangosteen.ssl";

	public static final String __CONSOLE_PWD = "org.mem.mangosteen.console";
	public static final String __PORT_DEFAULT = "8080";
	public static final boolean __NIO_DEFAULT = true;
	public static final boolean __SSL_DEFAULT = false;

	public static final String __CONSOLE_PWD_DEFAULT = "admin";

	public static final String __WEBAPP_DIR = "webapps";
	public static final String __ETC_DIR = "etc";
	public static final String __CONTEXTS_DIR = "contexts";

	public static final String __TMP_DIR = "tmp";
	public static final String __WORK_DIR = "work";
	public static final int __SETUP_PROGRESS_DIALOG = 0;
	public static final int __SETUP_DONE = 2;
	public static final int __SETUP_RUNNING = 1;
	public static final int __SETUP_NOTDONE = 0;

	public static final File __JETTY_DIR;

	private BroadcastReceiver bcastReceiver;
	
	private ToggleButton mjpecModeBtn;
	private Thread progressThread;
	private Handler handler;
	private Handler handler_nat;
	private ProgressDialog progressDialog;
	private ZipProgressThread progressZipDialog;
	public int enjoy;
	public String localIP;
	public String sendPORT;
	ToggleButton appStart;
	TextView txtStatus;
	//SocketIO_Request request;
	MangoSocket mangoSocket;
	String UserName;
	TextView txtNAT;
	
	public MangosteenActivity() {
		super();

		handler = new Handler() {
			public void handleMessage(Message msg) {
				int total = msg.getData().getInt("prog");
				progressDialog.setProgress(total);
				if (total >= 100) {
					dismissDialog(__SETUP_PROGRESS_DIALOG);
				}
			}
		};
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case __SETUP_PROGRESS_DIALOG: {
			progressDialog = new ProgressDialog(MangosteenActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("앱 초기 설정 중...");

			return progressDialog;
		}
		default:
			return null;
		}
	}
	
	class ZipProgressThread extends Thread{
		private Handler _handler;
		
		public ZipProgressThread(Handler h) {
			_handler = h;
		}
		public void sendProgressUpdate(int prog) {
			Message msg = _handler.obtainMessage();
			Bundle b = new Bundle();
			b.putInt("prog", prog);
			msg.setData(b);
			_handler.sendMessage(msg);
		}
		public void run() {
			//실행 시에 웹 앱 설치 전체 로직*******************************
			
			final PackageManager pm = getPackageManager(); // 패키지명알아내기 위한 패키지 매니져
			
			// 패키지 매니져에서 설치된 앱 리스트 가져오기
			String packageName = getApplicationContext().getPackageName();
			Log.d("패키지명", packageName);
			try {
				File dir = new File(packageName); //패키지명
				InputStream is = getApplicationContext().getResources().getAssets().open("Mangosteen.zip"); //파일명
				
				ZipInputStream zip = new ZipInputStream(is); // 파일에서 zip 파일 읽어오고
				ZipEntry ze;
				
				//Log.d("설치경로", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"jetty/webapps/");
				int prog = 10;
				while( (ze = zip.getNextEntry()) != null ) {
					//Log.d("ze.getName",ze.getName());
					  final String path =  Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"jetty/webapps/Mangosteen/" + ze.getName();
					  //Log.d("경로",path);
					  if( ze.getName().indexOf("/") != -1 ) {
					    File parent = new File(path).getParentFile();
					    if( !parent.exists() )
					      if( !parent.mkdirs() )
					        throw new IOException("Unable to create folder " + parent);
					  }
					  
					  FileOutputStream fout = new FileOutputStream(path);
					  byte[] bytes = new byte[1024];
	
					  for(int c=zip.read(bytes); c!=-1; c=zip.read(bytes)) {
					    fout.write(bytes, 0, c);
					  }
					  zip.closeEntry();
					  fout.close();
					  sendProgressUpdate(prog);
					  prog += 10;
					}
				sendProgressUpdate(100);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//*******************************
		}
	}
	
	class ProgressThread extends Thread {
		private Handler _handler;

		public ProgressThread(Handler h) {
			_handler = h;
		}

		public void sendProgressUpdate(int prog) {
			Message msg = _handler.obtainMessage();
			Bundle b = new Bundle();
			b.putInt("prog", prog);
			msg.setData(b);
			_handler.sendMessage(msg);
		}
		public void run() {
			
			boolean updateNeeded = isUpdateNeeded();

			File jettyDir = __JETTY_DIR;
			if (!jettyDir.exists()) {
				boolean made = jettyDir.mkdirs();
				Log.i(TAG, "Made " + __JETTY_DIR + ": " + made);
			}

			sendProgressUpdate(10);

			File workDir = new File(jettyDir, __WORK_DIR);
			if (workDir.exists()) {
				Installer.delete(workDir);
				Log.i(TAG, "removed work dir");
			}

			File tmpDir = new File(jettyDir, __TMP_DIR);
			if (!tmpDir.exists()) {
				boolean made = tmpDir.mkdirs();
				Log.i(TAG, "Made " + tmpDir + ": " + made);
			} else {
				Log.i(TAG, tmpDir + " exists");
			}

			File webappsDir = new File(jettyDir, __WEBAPP_DIR);
			if (!webappsDir.exists()) {
				boolean made = webappsDir.mkdirs();
				Log.i(TAG, "Made " + webappsDir + ": " + made);
			} else {
				Log.i(TAG, webappsDir + " exists");
			}

			File etcDir = new File(jettyDir, __ETC_DIR);
			if (!etcDir.exists()) {
				boolean made = etcDir.mkdirs();
				Log.i(TAG, "Made " + etcDir + ": " + made);
				Log.d("webapp", "없어서 만듬");
			} else {
				Log.i(TAG, etcDir + " exists");
				Log.d("webapp", "있다넹");
			}
			sendProgressUpdate(30);

			File webdefaults = new File(etcDir, "webdefault.xml");
			if (!webdefaults.exists() || updateNeeded) {
				try {
					InputStream is = getResources().openRawResource(
							R.raw.webdefault);
					OutputStream os = new FileOutputStream(webdefaults);
					IO.copy(is, os);
					Log.i(TAG, "Loaded webdefault.xml");
				} catch (Exception e) {
					Log.e(TAG, "Error loading webdefault.xml", e);
				}
			}
			sendProgressUpdate(40);

			File realm = new File(etcDir, "realm.properties");
			if (!realm.exists() || updateNeeded) {
				try {

					InputStream is = getResources().openRawResource(
							R.raw.realm_properties);
					OutputStream os = new FileOutputStream(realm);
					IO.copy(is, os);
					Log.i(TAG, "Loaded realm.properties");
				} catch (Exception e) {
					Log.e(TAG, "Error loading realm.properties", e);
				}
			}
			sendProgressUpdate(50);

			File keystore = new File(etcDir, "keystore");
			if (!keystore.exists() || updateNeeded) {
				try {

					InputStream is = getResources().openRawResource(
							R.raw.keystore);
					OutputStream os = new FileOutputStream(keystore);
					IO.copy(is, os);
					Log.i(TAG, "Loaded keystore");
				} catch (Exception e) {
					Log.e(TAG, "Error loading keystore", e);
				}
			}
			sendProgressUpdate(60);

			File contextsDir = new File(jettyDir, __CONTEXTS_DIR);
			if (!contextsDir.exists()) {
				boolean made = contextsDir.mkdirs();
				Log.i(TAG, "Made " + contextsDir + ": " + made);
			} else {
				Log.i(TAG, contextsDir + " exists");
			}
			sendProgressUpdate(70);

			try {
				PackageInfo pi = getPackageManager().getPackageInfo(
						getPackageName(), 0);
				if (pi != null) {
					setStoredJettyVersion(pi.versionCode);
				}
			} catch (Exception e) {
				Log.w(TAG, "Unable to get PackageInfo for i-jetty");
			}

			File update = new File(__JETTY_DIR, ".update");
			if (update.exists())
				update.delete();

			sendProgressUpdate(100);
		}
	};

	static {
		__JETTY_DIR = new File(Environment.getExternalStorageDirectory(),
				"jetty");

		// 시스템 프로퍼티 : JVM 이 자바 프로그램 시작할 때 운영체제로 부터 읽어와서 자동으로 설정
		// 자바 프로그래밍 중에 어디서든 시스템 프로퍼티에 저장된 데이터 사용가능
		// static과 차이점이라면 윈도우즈가 아닌 다른 시스템에서 실행할 때도 코드를 변경하지 않고 사용 가능

		System.setProperty("org.eclipse.jetty.xml.XmlParser.Validating",
				"false");

		System.setProperty("org.eclipse.jetty.util.log.class",
				"org.mem.mangosteen.AndroidLog");
		org.eclipse.jetty.util.log.Log.setLog(new AndroidLog());
	}

	@Override
	protected void onDestroy() {
		if (bcastReceiver != null)
			unregisterReceiver(bcastReceiver);
		super.onDestroy();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_server);
        
    	// 여기에 웹 앱 미설치시에만 실행 조건..
 		File mangosteen = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"jetty/webapps/Mangosteen/");
 		if(!mangosteen.exists())
 		{
 			Log.d("망고스틴 웹앱 설치","망고스틴 웹앱 설치");
 			webAppInstall();
 		}
        
        Intent intent = getIntent();
        UserName = intent.getStringExtra("UserName");
        txtStatus = (TextView)findViewById(R.id.status);
        txtNAT = (TextView)findViewById(R.id.nat_status);
        appStart = (ToggleButton)findViewById(R.id.toggleStart);
        mjpecModeBtn = (ToggleButton) findViewById(R.id.Mjpec_Mod);
        
        handler_nat = new Handler(){
			
			public void handleMessage(Message msg){
				switch (msg.what) {
				case 0:
					txtNAT.setText("Private IP");
					break;
				case 1:
					txtNAT.setText("Public IP");
					break;
				default:
					break;
			    }
				
				if(msg.what >= 55000){
					Log.i("PORT CLIENT","port client" + msg.what);
					sendPORT = Integer.toString(msg.what);
					mangoSocket.sendPort(sendPORT, UserName);
				}
			}
		
        };
        
        appStart.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(appStart.isChecked()){
					Log.i("Server Start", UserName);
					//request = new SocketIO_Request(MangosteenActivity.this, MangosteenActivity.this);
					//request.execute();
					
					mangoSocket = new MangoSocket(MangosteenActivity.this);
					mangoSocket.start();
					
					//Server Start
					Intent intent = new Intent(MangosteenActivity.this,
							MangosteenService.class);
					intent.putExtra(__PORT, __PORT_DEFAULT);
					intent.putExtra(__NIO, __NIO_DEFAULT);
					intent.putExtra(__SSL, __SSL_DEFAULT);
					intent.putExtra(__CONSOLE_PWD, __CONSOLE_PWD_DEFAULT);
					Log.d("start1", "gogo1");
					startService(intent);
					Log.d("start2", "gogo2");
					
					localIP =  getLocalIpAddress();
					
					txtStatus.setText("Server Running..." + localIP);
					
				}else{
					Log.i("Server Shutdown", UserName);
					//request.onCancelled();
					//request.closeSocket();
					mangoSocket.closeSocket();
					mangoSocket.interrupt();
					txtStatus.setText("");
					txtNAT.setText("");
					//Server Close
					stopService(new Intent(MangosteenActivity.this,
							MangosteenService.class));
					if(relayThread!=null)
					{
						relayThread.interrupt();
						if(relayserver!=null)
							relayserver.StopThread();
					}
				}
			}
		});
        
        mjpecModeBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mjpecModeBtn.isChecked()){
					Intent intent = new Intent(MangosteenActivity.this, MjpegActivity.class);
					startActivity(intent);
				}else{
					
				}
				
			}
		});
    }
    
 // 생명 주기 상 onCreate 코드와 중복 안되도록...
 	@Override
 	 protected void onResume() {
 	   Log.e("MangosteenActivity", "onResume");
 	   // if (!SdCardUnavailableActivity.isExternalStorageAvailable())
 	   // {
 	   // SdCardUnavailableActivity.show(this);
 	   // }
 	   
 	   if (isUpdateNeeded()) {
 	    setupJetty();
 	   }
 	   mjpecModeBtn.setChecked(false);
 	   if (MangosteenService.isRunning()) {
 	    appStart.setChecked(true);
 	   } else {
 	    if (enjoy != 0) {
 	     appStart.setChecked(false);
 	    }
 	   }
 	   super.onResume();
 	  }
    
 	protected void setStoredJettyVersion(int version) {
		File jettyDir = __JETTY_DIR;
		if (!jettyDir.exists()) {
			return;
		}
		File versionFile = new File(jettyDir, "version.code");
		ObjectOutputStream oos = null;
		try {
			FileOutputStream fos = new FileOutputStream(versionFile);
			oos = new ObjectOutputStream(fos);
			oos.writeInt(version);
			oos.flush();
		} catch (Exception e) {
			Log.e(TAG, "Problem writing jetty version", e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (Exception e) {
					Log.d(TAG, "Error closing version.code output stream", e);
				}
			}
		}
	}

	public boolean isUpdateNeeded() {
		int storedVersion = getStoredJettyVersion();
		if (storedVersion <= 0)
			return true;

		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			if (pi == null)
				return true;
			if (pi.versionCode != storedVersion)
				return true;

			File alwaysUpdate = new File(__JETTY_DIR, ".update");
			if (alwaysUpdate.exists()) {
				Log.i(TAG, "Always Update tag found " + alwaysUpdate);
				return true;
			}
		} catch (Exception e) {
			return true;
		}

		return false;
	}

	protected int getStoredJettyVersion() {
		File jettyDir = __JETTY_DIR;
		if (!jettyDir.exists()) {
			return -1;
		}
		File versionFile = new File(jettyDir, "version.code");
		if (!versionFile.exists()) {
			return -1;
		}
		int val = -1;
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(versionFile));
			val = ois.readInt();
			return val;
		} catch (Exception e) {
			Log.e(TAG, "Problem reading version.code", e);
			return -1;
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (Exception e) {
					Log.d(TAG, "Error closing version.code input stream", e);
				}
			}
		}
	}

	// private void printNetworkInterfaces()
	// {
	// try
	// {
	// Enumeration<NetworkInterface> nis =
	// NetworkInterface.getNetworkInterfaces();
	// for (NetworkInterface ni : Collections.list(nis))
	// {
	// Enumeration<InetAddress> iis = ni.getInetAddresses();
	// for (InetAddress ia : Collections.list(iis))
	// {
	// consoleBuffer.append(formatJettyInfoLine("Network interface: %s: %s",ni.getDisplayName(),ia.getHostAddress()));
	// }
	// }
	// }
	// catch (SocketException e)
	// {
	// Log.w(TAG, e);
	// }
	// }

	// public void consolePrint(String format, Object... args)
	// {
	// String msg = String.format(format,args);
	// if (msg.length() > 0)
	// {
	// consoleBuffer.append(msg).append("<br/>");
	// console.setText(Html.fromHtml(consoleBuffer.toString()));
	// Log.i(TAG,msg); // Only interested in non-empty lines being output to Log
	// }
	// else
	// {
	// consoleBuffer.append(msg).append("<br/>");
	// console.setText(Html.fromHtml(consoleBuffer.toString()));
	// }
	//
	// if (scrollTask == null)
	// {
	// scrollTask = new ConsoleScrollTask();
	// }
	//
	// consoleScroller.post(scrollTask);
	// }

	public void setupJetty() {
		Log.d("setupJetty", "셋업은 실행하나?");
		showDialog(__SETUP_PROGRESS_DIALOG);
		progressThread = new ProgressThread(handler);
		progressThread.start();
	};

	public static void show(Context context) {
		final Intent intent = new Intent(context, MangosteenActivity.class);
		context.startActivity(intent);
	}
	
	public void webAppInstall()
	{
		showDialog(__SETUP_PROGRESS_DIALOG);
		progressZipDialog = new ZipProgressThread(handler);
		progressZipDialog.start();
	}

 	
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_app_server, menu);
        return true;
    }
    
    
    public class MangoSocket extends Thread {
        private SocketIO socket;
        private MangoSocketCallback callback;
        
        public MangoSocket(MangoSocketCallbackAdapter callback) {
            this.callback = new MangoSocketCallback(callback);
        }
        
        @Override
        public void run() {
            try {
    			socket = new SocketIO("http://210.118.69.79:3000", callback);
    		} catch (MalformedURLException e) {
    			e.printStackTrace();
    		}
        }
        
        public void sendMessage(String message) {
            try {
                JSONObject json = new JSONObject();
                json.putOpt("message", message);
                socket.emit("server open", json);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        
        public void sendPort(String message, String userName) {
            try {
                JSONObject json = new JSONObject();
                json.putOpt("relayport", message);
                json.putOpt("nick", userName);
                socket.emit("relay open", json);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        
        public void login(String nickname) {
            try {
            	JSONObject json = new JSONObject();
	            json.putOpt("nick", nickname);
	            json.putOpt("m_addr", localIP);
	            json.putOpt("send_PORT", sendPORT);
	            socket.emit("server open", json);
	            
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        public void closeSocket() {
	    	socket.disconnect();
	    }
    }
    
	public void callback(JSONArray data) throws JSONException {
		// TODO Auto-generated method stub
		
	}

	Thread relayThread = null;
	Relayserver relayserver = null;
	public void on(String event, JSONObject data) {
		// TODO Auto-generated method stub
		try {
			//Log.i("Socket.io", "on..." + data.getString("name"));
			Message m = new Message();
			if (event.equals("server open")) {
                //MessagesTextArea.append(obj.getString("user") + ": " + obj.getString("message") + "\n");
            	Log.i("Socket.io", "event: server open..." + (data.getString("user")+": " + data.getString("message")));
            	if(data.getString("message").equals("private_ip")){
            		relayserver = new Relayserver(handler_nat);
            		relayThread = new Thread(relayserver);
            		relayThread.start();
            		m.what = 0;
            		Log.i("여기확인", Integer.toString(m.what));
            	}else{
            		m.what = 1;
            		Log.i("여기확인", Integer.toString(m.what));
            	}
            	handler_nat.sendMessage(m);
			}else if(event.equals("return port")) {
				Log.i("리턴포트 리턴포트", "리턴포트 리턴포트");
				
				//mangoSocket.sendPort(sendPORT);
				
			}

            else if (event.equals("announcement")) {
                //MessagesTextArea.append(obj.getString("user") + " " + obj.getString("action") + "\n");
            	Log.i("Socket.io", "event: announcement");
            }

            else if (event.equals("nicknames")) {
            	Log.i("Socket.io", "event: nicknames");
            	JSONArray names = data.names();
                String str = "";
                for (int i=0; i < names.length(); i++) {
                    if (i != 0)
                        str += ", ";
                    str += names.getString(i);
                }
                //OnlineUsers.setText(str);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
		
	}


	public void onMessage(String message) {
		// TODO Auto-generated method stub
		
    }


	public void onMessage(JSONObject json) {
		// TODO Auto-generated method stub
		
	}


	public void onConnect() {
		// TODO Auto-generated method stub
		Log.i("TAG", "여기 들어 오냐....");
        //request.login(UserName);
        mangoSocket.login(UserName);
	}


	public void onDisconnect() {
		// TODO Auto-generated method stub
		
		Log.i("TAG", "여긴 언제 호출???");
	}


	public void onConnectFailure() {
		// TODO Auto-generated method stub
		
	}
	
	public String getLocalIpAddress() {
		final String IP_NONE = "N/A";
		final String WIFI_DEVICE_PREFIX = "eth";
		String LocalIP;
		String ipAddress = "";
		WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			LocalIP = WIFI_DEVICE_PREFIX;
		} else {
			LocalIP = IP_NONE;
		}
		  try {
			   for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				   NetworkInterface intf = en.nextElement();           
				   		for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
				   			InetAddress inetAddress = enumIpAddr.nextElement();
				   			if (!inetAddress.isLoopbackAddress()) {
				   				if( LocalIP.equals(IP_NONE) )
				   					ipAddress = inetAddress.getHostAddress().toString();
				   				    
				   			else {
				   				 	WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
				   				 	Log.e("IP in Mask Integer", mWifiInfo.getIpAddress()+"");
				   				 	Log.e("IP Address", intToIP(mWifiInfo.getIpAddress())+"");
	                	  
				   				 	ipAddress = intToIP(mWifiInfo.getIpAddress());
	                	  
				   				 	return ipAddress;
				   				 }
				   			}
				   		}
			   		}
		       } catch (SocketException e) {
		    	   Log.e("ERROR_IPADRESS", "getLocalIpAddress Exception:"+e.toString());
		       }
		  return ipAddress;
	}
	
	public String intToIP(int i) {
	       return (( i & 0xFF)+ "."+((i >> 8 ) & 0xFF)+
	                          "."+((i >> 16 ) & 0xFF)+"."+((i >> 24 ) & 0xFF));
	} 
}
