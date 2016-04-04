package org.mem.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.webdav.LocalFileSystemStore;
import net.sf.webdav.exceptions.WebdavException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class FileUploadServlet extends HttpServlet {
	private static final String TAG = "FileUploadServlet";
	private ContentResolver resolver;
	private String uploadpath;
	private ServletConfig config;
	private static final String ROOTPATH_PARAMETER = "rootpath";
	private File root;
	private Context androidContext;
	private MediaScanner ms;
	private Notification _notification;
	private static final int GNOTIFY_ID = 1010101553;
	private NotificationManager _mNM;

	public FileUploadServlet() {
		this.config = null;

		this.root = null;
		this.androidContext = null;
		this.ms = null;
		this._notification = null;
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.resolver = ((ContentResolver) getServletContext().getAttribute(
				"org.mortbay.ijetty.contentResolver"));
		this.androidContext = ((Context) config.getServletContext()
				.getAttribute("org.mortbay.ijetty.context"));
		this.root = getFileRoot();
	}

	public ContentResolver getContentResolver() {
		return this.resolver;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		FileUploadListener listener = null;
		StringBuffer buffy = new StringBuffer();
		long bytesRead = 0, contentLength = 0;

		// Make sure the session has started
		if (session == null) {
			return;
		} else if (session != null) {
			// Check to see if we've created the listener object yet
			listener = (FileUploadListener) session.getAttribute("LISTENER");

			if (listener == null) {
				return;
			} else {
				// Get the meta information
				bytesRead = listener.getBytesRead();
				contentLength = listener.getContentLength();
			}
		}

		/*
		 * XML Response Code
		 */
		response.setContentType("text/xml");

		buffy.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		buffy.append("<response>\n");
		buffy.append("\t<bytes_read>" + bytesRead + "</bytes_read>\n");
		buffy.append("\t<content_length>" + contentLength
				+ "</content_length>\n");

		// Check to see if we're done
		if (bytesRead == contentLength) {
			buffy.append("\t<finished />\n");

			// No reason to keep listener in session since we're done
			session.setAttribute("LISTENER", null);
		} else {
			// Calculate the percent complete
			long percentComplete = ((100 * bytesRead) / contentLength);

			buffy.append("\t<percent_complete>" + percentComplete
					+ "</percent_complete>\n");
		}

		buffy.append("</response>\n");

		out.println(buffy.toString());
		out.flush();
		out.close();
	}

	protected String getRelativePath(HttpServletRequest request) {
		if (request.getAttribute("javax.servlet.include.request_uri") != null) {
			String result = (String) request
					.getAttribute("javax.servlet.include.path_info");
			if ((result == null) || (result.equals("")))
				result = "/";
			return result;
		}

		String result = request.getPathInfo();
		if ((result == null) || (result.equals(""))) {
			result = "/";
		}
		return result;
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	    	throws ServletException, IOException 
	    {
			// create file upload factory and upload servlet
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload 
				upload = new ServletFileUpload(factory);

			// set file upload progress listener
			FileUploadListener 
				listener = new FileUploadListener();
			
			HttpSession 
				session = request.getSession();
			
			session.setAttribute("LISTENER", listener);
			
			// upload servlet allows to set upload listener
			upload.setProgressListener(listener);

			List 
				uploadedItems = null;
			FileItem 
				fileItem = null;
			String 
				filePath = "c:\\temp";	// Path to store file on local system
			
	 		try 
			{
				// iterate over all uploaded files
				uploadedItems = upload.parseRequest(request);
				
				Iterator i = uploadedItems.iterator();
				
				while (i.hasNext()) 
				{
					fileItem = (FileItem) i.next();
					
					if (fileItem.isFormField() == false) 
					{
						if (fileItem.getSize() > 0) 
						{
							File 
								uploadedFile = null; 
							String
								myFullFileName = fileItem.getName(),
								myFileName = "",
								slashType = (myFullFileName.lastIndexOf("\\") > 0) ? "\\" : "/"; // Windows or UNIX
							int
								startIndex = myFullFileName.lastIndexOf(slashType);

							// Ignore the path and get the filename
							myFileName = myFullFileName.substring(startIndex + 1, myFullFileName.length());

							// Create new File object
							uploadedFile = new File(filePath, myFileName);
							
							// Write the uploaded file to the system
							fileItem.write(uploadedFile);
						}
					}
				}
			} 
			catch (FileUploadException e) 
			{
				e.printStackTrace();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			} 
		} 
	
	/*
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Log.d("FileUploadServlet", "doPost ");

		String path = getRelativePath(request);
		Log.d("path", path);

		this.uploadpath = (this.root + path);
		Log.d("uploadpath", uploadpath);

		if (!this.uploadpath.endsWith("/")) {
			this.uploadpath += "/";
		}
		Log.d("uploadpath2", uploadpath);

		String query = request.getQueryString();
		// Log.d("query", query);

		Context context = this.androidContext.getApplicationContext();
		int imageResource = context.getResources().getIdentifier("icon_upload",
				"drawable", context.getPackageName());
		int StringResource = context.getResources().getIdentifier(
				"upload_complete", "string", context.getPackageName());
		Random myRandom = new Random(System.currentTimeMillis());
		int NOTIFY_ID = myRandom.nextInt();

		if (!ServletFileUpload.isMultipartContent(request)) {
			throw new IllegalArgumentException(
					"Request is not multipart, please 'multipart/form-data' enctype for your form.");
		}

		// 디스크_파일_아이템_공장
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// factory.setSizeThreshold(4096); // 업로드시 사용할 임시 메모리
		// factory.setRepository(new File(path + "/temp")); // 임시 저장 폴더
		// 공장으로 부터 서블릿_파일_업로드 객체 얻기
		ServletFileUpload uploadHandler = new ServletFileUpload(factory);
		// upload.setSizeMax(3 * 1024 * 1024);//최대 업로드 파일 크기 : 3메가

		PrintWriter writer = response.getWriter();
		response.setContentType("text/plain");

		FileUploadListener listener = new FileUploadListener();
		HttpSession session = request.getSession();
		Vector v4Listners = (Vector) session.getAttribute("LISTENERS");
		UploadListener uploadListener = null;

		if (v4Listners == null) {
			session.setAttribute("LISTENERS", v4Listners = new Vector());
		}
		Log.i("FileUploadServlet", "POST ADD Path : " + path + ", query : "
				+ query + ", session : " + session.getId());
		v4Listners.add(uploadListener = new UploadListener(query, listener));

		uploadHandler.setProgressListener(listener);
		try {
			uploadHandler.setHeaderEncoding("utf-8");
			// 아이템 얻기
			List<FileItem> items = uploadHandler.parseRequest(request);
			this.ms = new MediaScanner(this.androidContext);

			for (FileItem item : items) {
				if (!item.isFormField()) { // 파일이 아닌경우
					String fieldName = item.getFieldName(); // 필드명 얻기
					String fileName = item.getName();
					// String value = item.getString("euc-kr"); //한글로 읽어온다.
					String contentType = item.getContentType();
					boolean isInMemory = item.isInMemory();
					long sizeInBytes = item.getSize();
					File uploadedFile = new File(this.uploadpath, fileName);
					// 타입 얻기
					String mime = getServletContext().getMimeType(
							uploadedFile.toString());
					Log.i("FileUploadServlet", "ITEMS :" + fieldName + ", "
							+ fileName + ", " + contentType + ", "
							+ this.uploadpath);

					// 파일이 아니라면..
					if (!uploadedFile.isFile()) {
						// 실제 디렉토리에 파일 저장
						item.write(uploadedFile);
						// 응답..
						writer.write("{\"name\":\"" + item.getName()
								+ "\",\"type\":\"" + item.getContentType()
								+ "\",\"size\":\"" + item.getSize()
								+ "\",\"url\":\"" + uploadedFile.toString()
								+ "\",\"thumbnail_url\":\""
								+ uploadedFile.toString()
								+ "\",\"delete_url\":\""
								+ uploadedFile.toString() + "delete_type:"
								+ "DELETE" + "\"}");
						// temp폴더의 파일 제거
						item.delete();

						// 전체 경로명
						String tmp = this.uploadpath + fileName;
						Log.d("uploadpath + fileName", tmp);
						this.ms.scanFile(tmp, null);
						CharSequence text = "";
						this._mNM = ((NotificationManager) this.androidContext
								.getSystemService("notification"));
						this._notification = new Notification(imageResource,
								text, System.currentTimeMillis());
						CharSequence contentTitle = fileName;
						CharSequence contentText = this.androidContext
								.getString(StringResource);
						Intent notificationit = new Intent(
								"android.intent.action.VIEW");
						Uri playUri = Uri.fromFile(uploadedFile);
						notificationit.setDataAndType(playUri, mime);
						PendingIntent i = PendingIntent.getActivity(context, 0,
								notificationit, 0);
						this._notification.setLatestEventInfo(context,
								contentTitle, contentText, i);
						this._notification.flags |= 16;
						this._mNM.notify(NOTIFY_ID, this._notification);
						break;
					}

					String body = null;
					String ext = null;
					int dot = fileName.lastIndexOf(".");
					Log.d("fileName.lastIndexOf()", Integer.toString(dot));
					if (dot != -1) {
						body = fileName.substring(0, dot);
						ext = fileName.substring(dot);
					} else {
						body = fileName;
						ext = "";
					}

					Log.d("body", body);
					Log.d("ext", ext);
					int count = 0;
					while (true) {
						count++;
						String newName = body + '(' + count + ')' + ext;
						Log.d("newName", newName);
						uploadedFile = new File(uploadedFile.getParent(),
								newName);
						if (!uploadedFile.isFile()) {
							item.write(uploadedFile);
							writer.write("{\"name\":\"" + item.getName()
									+ "\",\"type\":\"" + item.getContentType()
									+ "\",\"size\":\"" + item.getSize()
									+ "\",\"url\":\"" + uploadedFile.toString()
									+ "\",\"thumbnail_url\":\""
									+ uploadedFile.toString()
									+ "\",\"delete_url\":\""
									+ uploadedFile.toString() + "delete_type:"
									+ "DELETE" + "\"}");
							item.delete();
							this.ms.scanFile(this.uploadpath + newName, null);
							CharSequence text = "";
							this._mNM = ((NotificationManager) this.androidContext
									.getSystemService("notification"));
							this._notification = new Notification(
									imageResource, text,
									System.currentTimeMillis());
							CharSequence contentTitle = newName;
							CharSequence contentText = this.androidContext
									.getString(StringResource);
							Intent notificationit = new Intent(
									"android.intent.action.VIEW");
							Uri playUri = Uri.fromFile(uploadedFile);
							notificationit.setDataAndType(playUri, mime);
							PendingIntent i = PendingIntent.getActivity(
									context, 0, notificationit, 0);
							this._notification.setLatestEventInfo(context,
									contentTitle, contentText, i);
							this._notification.flags |= 16;
							this._mNM.notify(NOTIFY_ID, this._notification);

							break;
						}
					}
				}

			}

		} catch (FileUploadException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			writer.close();
		}

		Log.i("FileUploadServlet", "POST REMOVE Path : " + path + "query : "
				+ query);
		v4Listners.removeElement(uploadListener);
	}*/

	public static void DirMake(String DirPath) {
		File f = new File(DirPath);
		Log.d("FileUploadServlet", "DirMake:" + DirPath);
		if (!f.isDirectory()) {
			if (!f.mkdirs()) {
				return;
			}
		}
	}

	private File getFileRoot() {
		String rootPath = getInitParameter("rootpath");
		if (rootPath == null) {
			throw new WebdavException("missing parameter: rootpath");
		}
		if (rootPath.equals("*WAR-FILE-ROOT*")) {
			String file = LocalFileSystemStore.class.getProtectionDomain()
					.getCodeSource().getLocation().getFile().replace('\\', '/');
			if ((file.charAt(0) == '/')
					&& (System.getProperty("os.name").indexOf("Windows") != -1)) {
				file = file.substring(1, file.length());
			}

			int ix = file.indexOf("/WEB-INF/");
			if (ix != -1)
				rootPath = file.substring(0, ix).replace('/',
						File.separatorChar);
			else {
				throw new WebdavException(
						"Could not determine root of war file. Can't extract from path '"
								+ file + "' for this web container");
			}

		}

		return new File(rootPath);
	}

	private FileUploadListener findProgressListener(String id,
			Vector<UploadListener> vListeners) {
		FileUploadListener listner = null;

		for (int i = 0; (i < vListeners.size())
				&& ((listner = ((UploadListener) vListeners.get(i))
						.listener(id)) == null); i++)
			;
		return listner;
	}

	private UploadListener findUploadListener(String id,
			Vector<UploadListener> vListeners) {
		UploadListener upListner = null;

		for (int i = 0; (i < vListeners.size())
				&& (((upListner = (UploadListener) vListeners.get(i)) == null) || (upListner
						.listener(id) == null)); i++) {
			upListner = null;
		}
		return upListner;
	}

	private UploadListener selectListener(Vector<UploadListener> vListeners) {
		UploadListener listener = null;
		int size = vListeners.size();
		int idx = 0;

		if (size > 0) {
			Log.i("FileUploadServlet", "selectListener() size = " + size);
			for (int i = 0; i < size; i++) {
				UploadListener ul = (UploadListener) vListeners.get(i);
				if (ul._justUsed) {
					listener = ul;
					idx = i + 1;
				}
			}

			if (listener != null) {
				listener._justUsed = false;
				if (idx < size) {
					listener = (UploadListener) vListeners.get(idx);
				} else {
					listener = (UploadListener) vListeners.firstElement();
				}
				listener._justUsed = true;
			} else {
				listener = (UploadListener) vListeners.firstElement();
				listener._justUsed = true;
			}
		}
		return listener;
	}

	private class UploadListener {
		String _id;
		FileUploadListener _listener;
		boolean _justUsed;

		public UploadListener(String id, FileUploadListener listener) {
			this._id = id;
			this._listener = listener;
			this._justUsed = false;
		}

		FileUploadListener listener(String id) {
			FileUploadListener uploadListenr = null;

			if (this._id.equals(id)) {
				uploadListenr = this._listener;
			}
			return uploadListenr;
		}
	}
}