package org.mem.action;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

/**
 * Servlet implementation class TransPerFolderThumbnail
 */


// 각 폴더 별 썸네일 전송 서블릿
public class SelectFolderList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ContentResolver resolver = null;
	private Context context = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SelectFolderList() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Log.d("SelectFolderList.init", "시발 들어왔어");
		this.resolver = (ContentResolver) getServletContext().getAttribute(
				"org.mem.mangosteen.contentResolver");
		this.context = (Context) getServletContext().getAttribute(
				"org.mem.mangosteen.context");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		response.setContentType("application/json; charset=utf-8");
	    PrintWriter writer = response.getWriter();
		
		String path = request.getParameter("path");
		String type = request.getParameter("type");
		
		Log.d("SelectFolderList.path",path);
		Log.d("SelectFolderList.type",path);
		
		File ListFile = new File(path);
		String[] fileStringList = null;
		
		if(type.equals("image"))
		{
			fileStringList = ListFile.list(new FilenameFilter() {

					public boolean accept(File dir, String filename) {
						// TODO Auto-generated method stub
						Boolean bOK = false;
						if (filename.toLowerCase().endsWith(".png"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".9.png"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".gif"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".jpg"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".jpeg"))
							bOK = true;
						return bOK;
					}
				});
		}
		else if(type.equals("video"))
		{
			fileStringList = ListFile.list(new FilenameFilter() {

					public boolean accept(File dir, String filename) {
						// TODO Auto-generated method stub
						Boolean bOK = false;
						if (filename.toLowerCase().endsWith(".mp4"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".avi"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".wma"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".3gp"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".ts"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".mkv"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".aac"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".flv"))
							bOK = true;
						return bOK;
					}
				});
		}
		else if(type.equals("audio"))
		{
			fileStringList = ListFile.list(new FilenameFilter() {

					public boolean accept(File dir, String filename) {
						// TODO Auto-generated method stub
						Boolean bOK = false;
						if (filename.toLowerCase().endsWith(".mp3"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".flac"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".ogg"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".wave"))
							bOK = true;
						if (filename.toLowerCase().endsWith(".dcf"))
							bOK = true;
						return bOK;
					}
				});
		}
		
		
		ArrayList<PerImageBean> PerImageBeanList = new ArrayList<PerImageBean>();
		for(int i=0; i< fileStringList.length;i++)
		{
			PerImageBean perImageBean = new PerImageBean();
			perImageBean.setType(type);
			perImageBean.setPath(path + fileStringList[i]);
			perImageBean.setName(fileStringList[i]);
			PerImageBeanList.add(perImageBean);
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(PerImageBeanList);
		
		Log.d("PerImageBeanListJsonMsg", json);
		
		writer.println(json);
		writer.close();
	}
	
}
