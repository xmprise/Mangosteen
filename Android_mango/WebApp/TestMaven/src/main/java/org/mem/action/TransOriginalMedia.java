package org.mem.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

/**
 * Servlet implementation class TransOriginalMedia
 */
public class TransOriginalMedia extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ContentResolver resolver = null;
	private Context context = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TransOriginalMedia() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Log.d("TransOriginalMedia.init", "시발 들어왔어");
		this.resolver = (ContentResolver) getServletContext().getAttribute(
				"org.mem.mangosteen.contentResolver");
		this.context = (Context) getServletContext().getAttribute(
				"org.mem.mangosteen.context");

		Log.d("resolver", resolver.toString());
		Log.d("context", context.toString());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Log.d("TransOriginalMedia.doGet", "우아아아아 들어왔어");
		
		// 뒤에 매개변수 URI 정보
				String saveFullPath = request.getPathInfo();
				String contextPath = request.getContextPath();
				String requestURI = request.getRequestURI();
				String command = requestURI.substring(contextPath.length());
				
				int subIndex = command.indexOf(saveFullPath);

				//String type = command.substring(1, subIndex);
				String type = request.getServletPath().substring(1);

				Log.d("TransThumbnail", "PathInfo: " + saveFullPath);
				Log.d("TransThumbnail", "contextPath: " + contextPath);
				Log.d("TransThumbnail", "getQueryString: " + request.getQueryString());
				Log.d("TransThumbnail.Type", "Type: " + type);

				response.setStatus(200);
				response.setHeader("Accept-Ranges","bytes");
				//ServletOutputStream os = null;
				//FileInputStream fileInputStream = null;
				
				OutputStream os = response.getOutputStream();
				File file = new File(saveFullPath);
				InputStream is = new BufferedInputStream( new FileInputStream(file));

				try {
					String range = request.getHeader("Range");
					if( range != null && !range.equals("bytes=0-")) {
						
		                String[] ranges = range.split("=")[1].split("-");
		                int from = Integer.parseInt(ranges[0]);
		                int to = Integer.parseInt(ranges[1]);
		                int len = to - from + 1 ;
		                
		                response.setStatus(206);
		                response.setHeader("Accept-Ranges", "bytes");
		                String responseRange = String.format("bytes %d-%d/%d", from, to, file.length());
		                Log.d("Content-Range","Content-Range:" + responseRange);
		                response.setHeader("Connection", "close");
		                response.setHeader("Content-Range", responseRange);
		                response.setDateHeader("Last-Modified", new Date().getTime());
		                response.setContentLength(len);
		                Log.d("length","length:" + len);
		                
		                byte[] buf = new byte[4096];
		                is.skip(from);
		                while( len != 0) {

		                    int read = is.read(buf, 0, len >= buf.length ? buf.length : len);
		                    if( read != -1) {
		                        os.write(buf, 0, read);
		                        len -= read;
		                    }
		                }
		                
					} else {
	                    response.setStatus(200);
	                    IOUtils.copy(is, os);
	            }

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				} finally {
					try {
						is.close();
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Log.d("TransOriginalMedia.doPost", "우아아아아 들어왔어");
	}

}
