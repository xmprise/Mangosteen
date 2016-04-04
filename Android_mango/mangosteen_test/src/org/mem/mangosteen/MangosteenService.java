package org.mem.mangosteen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.security.Credential;
import org.mem.mangosteen.deployer.AndroidContextDeployer;
import org.mem.mangosteen.deployer.AndroidWebAppDeployer;
import org.mem.mangosteen.handler.DefaultHandler;

import com.mangosteen.mangosteen_test.MangosteenActivity;
import com.mangosteen.mangosteen_test.R;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class MangosteenService extends Service
{
    private static final String TAG = "Jetty";
    
    private static Resources __resources;
    private static final String CONTENT_RESOLVER_ATTRIBUTE = "org.mem.mangosteen.contentResolver";
    private static final String ANDROID_CONTEXT_ATTRIBUTE = "org.mem.mangosteen.context"; 
    
    
    public static final int __START_PROGRESS_DIALOG = 0;
    public static final int __STARTED = 0;
    public static final int __NOT_STARTED = 1;
    public static final int __STOPPED = 2;
    public static final int __NOT_STOPPED = 3;
    public static final int __STARTING = 4;
    public static final int __STOPPING = 5;
    
    public static final String[] __configurationClasses = 
        new String[]
        {
            "org.mem.mangosteen.webapp.AndroidWebInfConfiguration",
            "org.eclipse.jetty.webapp.WebXmlConfiguration",
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
            "org.eclipse.jetty.webapp.TagLibConfiguration" 
        };
    
    
    private static boolean __isRunning;
 
    private NotificationManager mNM;
    
    // 제티 서버
    private Server server; 
    private ContextHandlerCollection contexts;
    
    // 서버 환경 설정
    private boolean _useNIO;
    private boolean _useSSL;
    private int _port;
    private int _sslPort;
    private String _consolePassword;
    private String _keymgrPassword;
    private String _keystorePassword;
    private String _truststorePassword;
    private String _keystoreFile;
    private String _truststoreFile;
    
    // 앱 임시 저장
    private SharedPreferences preferences;
    
    //패키지 정보
    private PackageInfo pi;
    
    
    private android.os.Handler _handler;

    private PowerManager.WakeLock wakeLock;
    private final IBinder binder = new LocalBinder();

    
    static 
    {
        __isRunning = false;
    }
    
    public class LocalBinder extends Binder {
        MangosteenService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MangosteenService.this;
        }
    }

    public class JettyStarterThread extends Thread
    {
        android.os.Handler _handler;
        
        public JettyStarterThread(android.os.Handler handler)
        {
            _handler = handler;
        }
        public void run ()
        {
            try
            {
                sendMessage(__STARTING);
                startJetty();
                sendMessage(__STARTED);
              
                Log.i(TAG, "Jetty started");
            }
            catch (Exception e)
            {
                sendMessage(__NOT_STARTED);
                Log.e(TAG, "Error starting jetty", e);
                
            }
        }
        
        public void sendMessage(int state)
        {
            Message msg = _handler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("state", state);
            msg.setData(b);
            _handler.sendMessage(msg);
        }
    }
    
    
    public class JettyStopperThread extends Thread
    { 
        android.os.Handler _handler;
        
        public JettyStopperThread(android.os.Handler handler)
        {
            _handler = handler;
        }
        
        public void run ()
        {
            try
            {
                sendMessage(__STOPPING);
                stopJetty();
                Log.i(TAG, "Jetty stopped");
                sendMessage(__STOPPED);
               
            }
            catch (Exception e)
            {
                
                sendMessage(__NOT_STOPPED);
                Log.e(TAG, "Error stopping jetty", e);
            }
        }
        
        public void sendMessage(int state)
        {
            Message msg = _handler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("state", state);
            msg.setData(b);
            _handler.sendMessage(msg);
        }
    }
    
    public static InputStream getStreamToRawResource(int id)
    {
        if (__resources != null)
            return __resources.openRawResource(id);
        else
            return null;
    }


    
    public static boolean isRunning ()
    {
        return __isRunning;
    }

    public MangosteenService()
    {
        super();
        _handler = new android.os.Handler ()
        {
            public void handleMessage(Message msg) {
                switch (msg.getData().getInt("state"))
                {
                    case __STARTED:
                    {
                        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        
                        PendingIntent contentIntent = PendingIntent.getActivity(MangosteenService.this, 0,
                                new Intent(MangosteenService.this, MangosteenActivity.class), 0);

                        CharSequence text = getText(R.string.manage_jetty);

                        Notification notification = new Notification(R.drawable.mangosteen_noti, 
                                text, 
                                System.currentTimeMillis());

                        notification.setLatestEventInfo(MangosteenService.this, getText(R.string.app_name),
                                text, contentIntent);

                        mNM.notify(R.string.jetty_started, notification);
                        
                        Intent startIntent = new Intent(MangosteenActivity.__START_ACTION);
                        startIntent.addCategory("default");
                        Connector[] connectors = server.getConnectors();
                        if (connectors != null)
                        {
                            String[] tmp = new String[connectors.length];
                            
                            for (int i=0;i<connectors.length;i++)
                                tmp[i] = connectors[i].toString();

                            startIntent.putExtra("connectors", tmp);
                        }
                       
                        sendBroadcast(startIntent);
                        break;
                    }
                    case __NOT_STARTED:
                    {
                        break;
                    }
                    case __STOPPED:
                    {
                        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNM.cancel(R.string.jetty_started);
                     
                        Intent stopIntent = new Intent(MangosteenActivity.__STOP_ACTION);
                        stopIntent.addCategory("default");
                        sendBroadcast(stopIntent);
                        break;
                    }
                    
                    case __NOT_STOPPED:
                    {
                        
                        break;
                    }
                    case __STARTING:
                    {
                        
                        break;
                    }                    
                    case __STOPPING:
                    {

                        break;
                    }
                }
               
            }
 
        };
    }
    
    


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /** 
     * Android Service create
     * @see android.app.Service#onCreate()
     */
    public void onCreate()
    {
    	
    	// Context에서 지원하는 메소드. 프로젝트 생성 시 기본으로 상속받게 되는 Activity가 Context의 자식 이므로 부모의 기능 모두 사용가능, 즉 객체 생성 없이도 사용가능
    	// 현재 앱의 패키지에 리소스 객체를 반환 한다.
        __resources = getResources();

        try
        {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0); 
        }
        catch (Exception e)
        {
            Log.e(TAG, "Unable to determine running jetty version");
        }
    }


    /** 
     * Android Service Start
     * @see android.app.Service#onStart(android.content.Intent, int)
     */
    public void onStart(Intent intent, int startId)
    {
    	Log.d("onStart", "되나?");
        if (server != null)
        {
            return;
        }

        try
        {
        	// 제티 서버 환경 설정 객체
        	// 안드로이드 preference에 설정된 정보들을 제티 서버 환경 객체에 셋팅
            preferences = PreferenceManager.getDefaultSharedPreferences(this);

            
            String portDefault = getText(R.string.pref_port_value).toString();
            String sslPortDefault = getText(R.string.pref_ssl_port_value).toString();
            String pwdDefault = getText(R.string.pref_console_pwd_value).toString();
            
            String nioEnabledDefault = getText(R.string.pref_nio_value).toString();
            String sslEnabledDefault = getText(R.string.pref_ssl_value).toString();

            String portKey = getText(R.string.pref_port_key).toString();
            String sslPortKey = getText(R.string.pref_ssl_port_key).toString();
            String pwdKey = getText(R.string.pref_console_pwd_key).toString();
            String nioKey = getText(R.string.pref_nio_key).toString();
            String sslKey = getText(R.string.pref_ssl_key).toString();
            
            _useSSL = preferences.getBoolean(sslKey, Boolean.valueOf(sslEnabledDefault));
            _useNIO = preferences.getBoolean(nioKey, Boolean.valueOf(nioEnabledDefault));
            _port = Integer.parseInt(preferences.getString(portKey, portDefault));
            
            
            // 여기서 raw 폴더의 3가지 설정 파일 keystore, realm_properties , webdefault ??????
            if (_useSSL)
            {
              _sslPort = Integer.parseInt(preferences.getString(sslPortKey, sslPortDefault));
              String defaultValue = getText(R.string.pref_keystore_pwd_value).toString();
              String key = getText(R.string.pref_keystore_pwd_key).toString();
              _keystorePassword = preferences.getString(key, defaultValue);
              
              defaultValue = getText(R.string.pref_keymgr_pwd_value).toString();
              key = getText(R.string.pref_keymgr_pwd_key).toString();
              _keymgrPassword = preferences.getString(key, defaultValue);
              
              defaultValue = getText(R.string.pref_truststore_pwd_value).toString();
              key = getText(R.string.pref_truststore_pwd_key).toString();
              _truststorePassword = preferences.getString(key, defaultValue);
              
              defaultValue = getText(R.string.pref_keystore_file).toString();
              key = getText(R.string.pref_keystore_file_key).toString();
              _keystoreFile = preferences.getString(key, defaultValue);
              
              defaultValue = getText(R.string.pref_truststore_file).toString();
              key = getText(R.string.pref_truststore_file_key).toString();
              _truststoreFile = preferences.getString(key, defaultValue);
            }

            _consolePassword = preferences.getString(pwdKey, pwdDefault);

            Log.d("Jetty", "pref port = "+_port);
            Log.d("Jetty", "pref use nio = "+_useNIO);
            Log.d("Jetty", "pref use ssl = "+_useSSL);
            Log.d("Jetty", "pref ssl port = "+_sslPort);
           
            //Get a wake lock to stop the cpu going to sleep
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MangosteenActivity");
            wakeLock.acquire();

            new JettyStarterThread(_handler).start();
 
            super.onStart(intent, startId);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error starting jetty", e);
           
        }
    }


    /** 
     * Android Service destroy
     * @see android.app.Service#onDestroy()
     */
    public void onDestroy()
    {
        try
        {
            if (wakeLock != null)
            {
                wakeLock.release();
                wakeLock = null;
            }
            
            if (server != null)
            {
                new JettyStopperThread(_handler).start();
                
            }
            else
            {
                Log.i(TAG, "Jetty not running");
               
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error stopping jetty", e);

        }
    }
    
    
   
    

    public void onLowMemory()
    {
        Log.i(TAG, "Low on memory");
        super.onLowMemory();
    }


    
    /**
     * Get a reference to the Jetty Server instance
     * @return
     */
    public Server getServer()
    {
        return server;
    }
    

    
    protected Server newServer()
    {
        return new Server();
    }
    
    protected ContextHandlerCollection newContexts()
    {
        return new ContextHandlerCollection();
    }
  
    
    // 환경 설정에 따라 제티 서버 구동 방식 처리 
    // NIO, SSL 지원 여부 
    protected void configureConnectors()
    {
        if (server != null)
        {
            if (_useNIO)
            {
                SelectChannelConnector nioConnector = new SelectChannelConnector();
                nioConnector.setUseDirectBuffers(false);
                nioConnector.setPort(_port);
                server.addConnector(nioConnector);
                Log.i(TAG, "Configured "+SelectChannelConnector.class.getName()+" on port "+_port);
            }
            else
            {
                SocketConnector bioConnector = new SocketConnector();
                bioConnector.setPort(_port);
                server.addConnector(bioConnector);
                Log.i(TAG, "Configured "+SocketConnector.class.getName()+" on port "+_port);
            }

            if (_useSSL)
            {
                SslContextFactory sslContextFactory = new SslContextFactory();
                sslContextFactory.setKeyStore(_keystoreFile);
                sslContextFactory.setTrustStore(_truststoreFile);
                sslContextFactory.setKeyStorePassword(_keystorePassword);
                sslContextFactory.setKeyManagerPassword(_keymgrPassword);
                sslContextFactory.setKeyStoreType("bks");
                sslContextFactory.setTrustStorePassword(_truststorePassword);
                sslContextFactory.setTrustStoreType("bks");

                //TODO SslSelectChannelConnector does not work on android 1.6, but does work on android 2.2
                if (_useNIO)
                {
                    SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(sslContextFactory);
                    sslConnector.setPort(_sslPort);
                    server.addConnector(sslConnector);
                    Log.i(TAG, "Configured "+sslConnector.getClass().getName()+" on port "+_sslPort); 
                }
                else
                {
                    SslSocketConnector sslConnector = new SslSocketConnector(sslContextFactory);
                    sslConnector.setPort(_sslPort);
                    server.addConnector(sslConnector);
                    Log.i(TAG, "Configured "+sslConnector.getClass().getName()+" on port "+_sslPort); 
                }
               
            }
        }
    }
    
    // 뭐지 제티 쪽으로 뭔가 메세지를 보내는거 같은데....;;;
    protected void configureHandlers()
    {
        if (server != null)
        {
            HandlerCollection handlers = new HandlerCollection();
            contexts = new ContextHandlerCollection();
            handlers.setHandlers(new Handler[] {contexts, new DefaultHandler()});
            server.setHandler(handlers);
        }
    }
    
    @SuppressWarnings("deprecation")
	protected void configureDeployers () throws Exception
    {
    	// webapp 폴더랑 파일이 있는지 없는지 검사하는거 같은디... 좀더 분석
        AndroidWebAppDeployer staticDeployer =  new AndroidWebAppDeployer();
        
        //
        AndroidContextDeployer contextDeployer = new AndroidContextDeployer();
     
        File jettyDir = MangosteenActivity.__JETTY_DIR;
        
        if (jettyDir.exists())
        {
            // If the webapps dir exists, start the static webapp deployer
            if (new File(jettyDir, MangosteenActivity.__WEBAPP_DIR).exists())
            {
                staticDeployer.setWebAppDir(MangosteenActivity.__JETTY_DIR+"/"+MangosteenActivity.__WEBAPP_DIR);
                staticDeployer.setDefaultsDescriptor(MangosteenActivity.__JETTY_DIR+"/"+MangosteenActivity.__ETC_DIR+"/webdefault.xml");
                staticDeployer.setContexts(contexts);
                staticDeployer.setAttribute(CONTENT_RESOLVER_ATTRIBUTE, getContentResolver());
                staticDeployer.setAttribute(ANDROID_CONTEXT_ATTRIBUTE, (Context) MangosteenService.this);
                staticDeployer.setConfigurationClasses(__configurationClasses);
                staticDeployer.setAllowDuplicates(false);
            }          
           
            // Use a ContextDeploy so we can hot-deploy webapps and config at startup.
            if (new File(jettyDir, MangosteenActivity.__CONTEXTS_DIR).exists())
            {
                contextDeployer.setScanInterval(10); // Don't eat the battery
                contextDeployer.setConfigurationDir(MangosteenActivity.__JETTY_DIR+"/"+MangosteenActivity.__CONTEXTS_DIR);                
                contextDeployer.setAttribute(CONTENT_RESOLVER_ATTRIBUTE, getContentResolver());
                contextDeployer.setAttribute(ANDROID_CONTEXT_ATTRIBUTE, (Context) MangosteenService.this);             
                contextDeployer.setContexts(contexts);
            }
            
            if (server != null)
            {
                Log.i(TAG, "Adding context deployer: ");
                server.addBean(contextDeployer);
                Log.i(TAG, "Adding webapp deployer: ");
                server.addBean(staticDeployer); 
            }
        }
        else
        {
            Log.w(TAG, "Not loading any webapps - none on SD card.");
        }
    }
    
    public void configureRealm () throws IOException
    {
        File realmProps = new File(MangosteenActivity.__JETTY_DIR+"/"+MangosteenActivity.__ETC_DIR+"/realm.properties");
        if (realmProps.exists())
        {
            HashLoginService realm = new HashLoginService("Console", MangosteenActivity.__JETTY_DIR+"/"+MangosteenActivity.__ETC_DIR+"/realm.properties");
            realm.setRefreshInterval(0);
            if (_consolePassword != null)
                realm.putUser("admin", Credential.getCredential(_consolePassword), new String[]{"admin"}); //set the admin password for console webapp
            server.addBean(realm);
        }
    }
    
    
    protected void startJetty() throws Exception
    {

    	// 제티 홈 디렉토리경로 시스템에 설정
        System.setProperty ("jetty.home", MangosteenActivity.__JETTY_DIR.getAbsolutePath());

        // IPv6 지원 설정
        System.setProperty("java.net.preferIPv6Addresses", "false");
        
        
        server = newServer();
        
        // 설정
        configureConnectors();
        configureHandlers();
        configureDeployers();
        configureRealm ();
    
        server.start();
        
        __isRunning = true;
        
        HttpGenerator.setServerVersion("mangosteen "+pi.versionName);
    }

    protected void stopJetty() throws Exception
    {
        try
        {
            Log.i(TAG, "Jetty stopping");
            server.stop();
            Log.i(TAG, "Jetty server stopped");
            server = null;
            __resources = null;
            __isRunning = false;
        }
        finally
        {
            Log.i(TAG,"Finally stopped");
        }
    }
}
