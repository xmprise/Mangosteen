package org.mem.action;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.util.Log;

/**
 * Servlet implementation class FrontController
 */
public class FrontController extends HttpServlet implements javax.servlet.Servlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void doProcess(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
    	
    	Log.d("FrontController","프론트컨트롤러 호출");
    	
    	String RequestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		String command = RequestURI.substring(contextPath.length());
		String userAgent  = request.getHeader( "User-Agent" ).toLowerCase();
		
		boolean result = false;

		System.out.println(RequestURI);
		System.out.println(contextPath);
		System.out.println(command);
		System.out.println("id>>"+request.getParameter("id"));
		System.out.println(userAgent);
		
		if (userAgent.indexOf("Android") > 0) {                                        // 안드로이드로 접속했다면 결과값 true
			  result = true;
			 } else if (userAgent.indexOf("iPhone") > 0) {                               // 아이폰으로 접속했다면 결과값 true
			  result = true;
		}
		if(command.equals("/")){
			 if (result == true) {
				  response.sendRedirect(RequestURI+"mobile/index.html");  
			 }
			 else
				 response.sendRedirect(RequestURI+"index.html");
//			try{
//				forward = action.execute(request, response);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
		}
		
//		if(forward != null){
//			if(forward.isRedirect()){
//				response.sendRedirect(forward.getPath());
//			}else{
//				RequestDispatcher dispatcher = request.getRequestDispatcher(forward.getPath());
//				dispatcher.forward(request, response);
//			}
//		}
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doProcess(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doProcess(request, response);
	}

}
