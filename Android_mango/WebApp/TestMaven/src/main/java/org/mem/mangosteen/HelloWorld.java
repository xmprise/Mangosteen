package org.mem.mangosteen;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import android.util.Log;

public class HelloWorld extends HttpServlet
{
  String proofOfLife = null;

  
  public void init(ServletConfig config)
    throws ServletException
  {
	Log.d("init", "¾ÈµÇ¹¦???");
    super.init(config);

    Object o = config.getServletContext().getAttribute("org.mem.mangosteen.contentResolver");
    ContentResolver resolver = (ContentResolver)o;
    Context androidContext = (Context)config.getServletContext().getAttribute("org.mem.mangosteen.context");
    this.proofOfLife = androidContext.getApplicationInfo().packageName;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    doGet(request, response);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    response.setContentType("text/html");
    ServletOutputStream out = response.getOutputStream();
    out.println("<html>");
    out.println("<h1>Hello From Servlet Land!</h1>");
    out.println("Brought to you by: " + this.proofOfLife);
    out.println("</html>");
    out.flush();
  }
}