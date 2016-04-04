package org.mem.action;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;


/**
 * Servlet implementation class fileList
 */
public class FileList implements Action {
	private static final long serialVersionUID = 1L;

	public ActionForward execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ActionForward forward = null;

		String rootDir = "D:/jetty";
		// rootDir = new String(rootDir.getBytes("8859_1"), "euc-kr");

		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("UTF-8");
		System.out.println(">" + rootDir);
		String id = request.getParameter("id");
		System.out.println(id);
		
		
		PrintWriter pw = response.getWriter();

		if (id.equals("music")) {
			File dir = new File(rootDir);
			
			Gson gson = new Gson();
			
			String json = gson.toJson(dir.listFiles());
			
			System.out.println(dir);
			System.out.println(json);
			
			for(File file:dir.listFiles())
			{
				System.out.println(file.getName());
			}
			
			if (dir.exists() && dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					
				}
			}
			
			pw.write(json);
		}
		return null;
	}
}
