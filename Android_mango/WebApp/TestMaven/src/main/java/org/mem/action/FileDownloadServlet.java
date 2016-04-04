package org.mem.action;

import android.content.ContentResolver;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.webdav.LocalFileSystemStore;
import net.sf.webdav.exceptions.WebdavException;

public class FileDownloadServlet extends HttpServlet
{
  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;
  private static final String TAG = "FileDownloadServlet";
  private String dnloadpath;
  private ContentResolver resolver;
  private ServletConfig config = null;
  private static final String ROOTPATH_PARAMETER = "rootpath";
  private static final int BUF_SIZE = 65536;
  private File root = null;

  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);
    this.resolver = ((ContentResolver)getServletContext().getAttribute("org.mortbay.ijetty.contentResolver"));
    this.config = config;
    this.root = getFileRoot();
  }

  public ContentResolver getContentResolver()
  {
    return this.resolver;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    Log.d("FileDownloadServlet", "doGet ");

    String[] strfn = request.getParameterValues("filename[]");
    String[] strfd = request.getParameterValues("filedir[]");
    String[] strfs = request.getParameterValues("filesize[]");

    for (int i = 0; i < strfn.length; i++)
    {
      Log.d("FileDownloadServlet", "strfn[]: " + strfn[i]);
    }

    File externalDir = Environment.getExternalStorageDirectory();
    File sdcarddir = new File(externalDir, "/MWS/dnload");
    this.dnloadpath = sdcarddir.toString();
    DirMake(this.dnloadpath);

    doProcess(request, response, this.dnloadpath);
  }

  private void setHeader(HttpServletResponse response, File file)
  {
    String mime = getServletContext().getMimeType(file.toString());
    if (mime == null) mime = "application/octet-stream";
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", "attachment; filename=" + I18nUtil.K2E(file.getName()) + ";");

    response.setHeader("Content-Length", "" + file.length());
  }

  private void transport(InputStream in, OutputStream out)
    throws IOException
  {
    BufferedInputStream bin = null;
    BufferedOutputStream bos = null;
    try
    {
      bin = new BufferedInputStream(in);
      bos = new BufferedOutputStream(out);

      byte[] buf = new byte[2048];
      int read = 0;
      while ((read = bin.read(buf)) != -1)
      {
        bos.write(buf, 0, read);
      }
    }
    finally
    {
      bos.close();
      bin.close();
    }
  }

  protected String getDownloadPath(HttpServletRequest request)
  {
    String[] strfn = request.getParameterValues("filename[]");

    String[] strfd = request.getParameterValues("filedir[]");

    String[] strtlen = strfd[0].split("console/webdav/res");
    this.dnloadpath = (this.root.toString() + strtlen[1]);

    return this.dnloadpath;
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    Log.d("FileDownloadServlet", "doPost ");
    String path = getRelativePath(request);

    doProcess(request, response, getDownloadPath(request));
  }

  protected void doProcess(HttpServletRequest request, HttpServletResponse response, String DNPath)
    throws ServletException, IOException
  {
    String[] strfn = request.getParameterValues("filename[]");

    File dnloadedFile = new File(DNPath, strfn[0]);

    response.setContentType("application/octet-stream");
    response.setHeader("Content-Length", "" + dnloadedFile.length());
    response.setHeader("Content-Disposition", "attachment; filename=" + I18nUtil.K2E(dnloadedFile.getName()) + ";");

    OutputStream out = response.getOutputStream();
    InputStream in = new BufferedInputStream(new FileInputStream(dnloadedFile));
    try
    {
      int read = -1;
      byte[] copyBuffer = new byte[65536];

      while ((read = in.read(copyBuffer, 0, copyBuffer.length)) != -1)
        out.write(copyBuffer, 0, read);
    }
    finally
    {
      try
      {
        in.close();
      } catch (Exception e) {
        Log.w("FileDownloadServlet", "Closing InputStream causes Exception!\n " + e.toString());
      }
      try {
        out.flush();
        out.close();
      } catch (Exception e) {
        Log.w("FileDownloadServlet", "Flushing OutputStream causes Exception!\n" + e.toString());
      }
    }
  }

  public static void DirMake(String DirPath)
  {
    File f = new File(DirPath);
    Log.d("FileDownloadServlet", "DirMake:" + DirPath);
    if (!f.isDirectory())
    {
      if (!f.mkdirs())
      {
        return;
      }
    }
  }

  protected String getRelativePath(HttpServletRequest request)
  {
    if (request.getAttribute("javax.servlet.include.request_uri") != null) {
      String result = (String)request.getAttribute("javax.servlet.include.path_info");

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

  private File getFileRoot()
  {
    String rootPath = getInitParameter("rootpath");
    if (rootPath == null) {
      throw new WebdavException("missing parameter: rootpath");
    }

    if (rootPath.equals("*WAR-FILE-ROOT*")) {
      String file = LocalFileSystemStore.class.getProtectionDomain().getCodeSource().getLocation().getFile().replace('\\', '/');

      if ((file.charAt(0) == '/') && (System.getProperty("os.name").indexOf("Windows") != -1))
      {
        file = file.substring(1, file.length());
      }

      int ix = file.indexOf("/WEB-INF/");
      if (ix != -1) {
        rootPath = file.substring(0, ix).replace('/', File.separatorChar);
      }
      else {
        throw new WebdavException("Could not determine root of war file. Can't extract from path '" + file + "' for this web container");
      }

    }

    return new File(rootPath);
  }
}