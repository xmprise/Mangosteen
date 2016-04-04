package org.mem.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Servlet implementation class ImgThumbnailList
 */
public class PerFolderList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ContentResolver resolver = null;
	private Context context = null;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Log.d("MediaReqServlet.init", "시발 들어왔어");
		this.resolver = (ContentResolver) getServletContext().getAttribute(
				"org.mem.mangosteen.contentResolver");
		this.context = (Context) getServletContext().getAttribute(
				"org.mem.mangosteen.context");
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PerFolderList() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		response.setContentType("application/json; charset=utf-8");
	    PrintWriter writer = response.getWriter();
	   
		ArrayList<PerFolderBean> perFolderBeanList = null;
		String type = request.getParameter("id"); // 어떤 타입 요청인지...
		
		MediaDB mediaDB = new MediaDB(this.resolver);
		
		perFolderBeanList = mediaDB.getPerFolderBeanList(type); // 타입에 맞는 폴더별 HashMap 받아오고..
		
		Gson gson = new Gson();
		String json = gson.toJson(perFolderBeanList);
		
		Log.d("PerFolderListJsonMsg", json);
		
		writer.println(json);
		writer.close();
	}
}
