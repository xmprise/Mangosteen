package org.mem.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.content.ContentResolver;
import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.util.Log;

/**
 * Servlet implementation class MjpegStreamming
 */
public class MjpegStreamming extends HttpServlet {
    private static final long serialVersionUID = 6748857432950840322L;
    private ContentResolver resolver;
    private Context androidContext;
    private Handler mHandler;
    private LocalSocket receiver;
    private InputStream inputStream = null;
    private OutputStream ouputStream = null;
    byte[] buffer;
    private String Boundary = "--boundary";
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Log.d("MjpegStreamming", "MjpegStreamming Access!!");
        this.resolver = (ContentResolver) getServletContext().getAttribute(
				"org.mem.mangosteen.contentResolver");
		this.androidContext = (Context) getServletContext().getAttribute(
				"org.mem.mangosteen.context");
    }

    public ContentResolver getContentResolver() {
		return this.resolver;
	}
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MjpegStreamming() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		Log.d("MjpegStreaming", "MjpegStreaming들어옴");
		buffer = new byte[1000000];
		receiver = new LocalSocket();
		response.setStatus(200);
		response.setContentType("multipart/x-mixed-replace; boundary=" +
                this.Boundary);	
		ouputStream = response.getOutputStream(); 
	
		//ouputStream.write(Header.getBytes());

		try {
			receiver.connect(new LocalSocketAddress("Mangosteen"));
			receiver.setReceiveBufferSize(1000000);
			receiver.setSendBufferSize(1000000);

			inputStream = receiver.getInputStream();
			Log.d("MjpegStreaming", "쓰레드직전");
			
			Thread thread = new Thread(new Runnable() {
				int i;
				public void run() {
					while (true) {
						try {
							i = inputStream.read(buffer);
							Log.d("시봘확인좀해보자", Integer.toString(i));
							StringBuilder sb = new StringBuilder();
							int length = i;
							sb.append("\r\n");
							sb.append("--boundary");
							sb.append("\r\n");
							sb.append("Content-Type: image/jpeg\r\n");
							sb.append("Content-Length: "
									+ Integer.toString(length) + "\r\n");
							sb.append("\r\n");
							ouputStream.write(sb.toString().getBytes());
							ouputStream.write(buffer);
							ouputStream.write("\r\n\r\n".toString().getBytes());
							ouputStream.flush();
							
							if(receiver == null || i == -1)
							{
								ouputStream.close();
								receiver.close();
								break;
							}

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			thread.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
