package net.sf.webdav;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.exceptions.UnauthenticatedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.fromcatalina.MD5Encoder;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.methods.DoCopy;
import net.sf.webdav.methods.DoDelete;
import net.sf.webdav.methods.DoGet;
import net.sf.webdav.methods.DoHead;
import net.sf.webdav.methods.DoLock;
import net.sf.webdav.methods.DoMkcol;
import net.sf.webdav.methods.DoMove;
import net.sf.webdav.methods.DoNotImplemented;
import net.sf.webdav.methods.DoOptions;
import net.sf.webdav.methods.DoPropfind;
import net.sf.webdav.methods.DoProppatch;
import net.sf.webdav.methods.DoPut;
import net.sf.webdav.methods.DoRename;
import net.sf.webdav.methods.DoUnlock;
import android.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDavServletBean extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static org.slf4j.Logger LOG = LoggerFactory.getLogger(WebDavServletBean.class);
    /**
     * MD5 message digest provider.
     */
    protected static MessageDigest MD5_HELPER;

    /**
     * The MD5 helper object for this class.
     */
    protected static final MD5Encoder MD5_ENCODER = new MD5Encoder();

    private static final boolean READ_ONLY = false;
    private ResourceLocks _resLocks;
    private IWebdavStore _store;
    private HashMap<String, IMethodExecutor> _methodMap = new HashMap<String, IMethodExecutor>();

    public WebDavServletBean() {
    	Log.d("WebDavServletBean","WebDavServletBean");
        _resLocks = new ResourceLocks();

        try {
            MD5_HELPER = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
    }

    public void init(IWebdavStore store, String dftIndexFile,
            String insteadOf404, int nocontentLenghHeaders,
            boolean lazyFolderCreationOnPut) throws ServletException {

    	Log.d("WebDavServletBean_init","WebDavServletBean_init");
        _store = store;

        IMimeTyper mimeTyper = new IMimeTyper() {
            public String getMimeType(String path) {
                return getServletContext().getMimeType(path);
            }
        };

        register("GET", new DoGet(store, dftIndexFile, insteadOf404, _resLocks,
                mimeTyper, nocontentLenghHeaders));
        register("HEAD", new DoHead(store, dftIndexFile, insteadOf404,
                _resLocks, mimeTyper, nocontentLenghHeaders));
        DoDelete doDelete = (DoDelete) register("DELETE", new DoDelete(store,
                _resLocks, READ_ONLY));
        DoCopy doCopy = (DoCopy) register("COPY", new DoCopy(store, _resLocks,
                doDelete, READ_ONLY));
        register("LOCK", new DoLock(store, _resLocks, READ_ONLY));
        register("UNLOCK", new DoUnlock(store, _resLocks, READ_ONLY));
        register("MOVE", new DoMove(_resLocks, doDelete, doCopy, READ_ONLY));
        register("MKCOL", new DoMkcol(store, _resLocks, READ_ONLY));
        register("OPTIONS", new DoOptions(store, _resLocks));
        register("PUT", new DoPut(store, _resLocks, READ_ONLY,
                lazyFolderCreationOnPut));
        register("PROPFIND", new DoPropfind(store, _resLocks, mimeTyper));
        register("PROPPATCH", new DoProppatch(store, _resLocks, READ_ONLY));
        register("RENAME", new DoRename(store, this._resLocks, false));
        register("*NO*IMPL*", new DoNotImplemented(READ_ONLY));
    }

    private IMethodExecutor register(String methodName, IMethodExecutor method) {
        _methodMap.put(methodName, method);
        return method;
    }

    /**
     * Handles the special WebDAV methods.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

    	Log.d("webdavReq","webdavReqPost");
    	
        String methodName = req.getMethod();
        
        Log.d("req.getMethod()",methodName);
        
        ITransaction transaction = null;
        boolean needRollback = false;
                        
        if ("POST".equals(methodName))
        {
          String queryStr = req.getQueryString();
          
          Log.d("queryStr", queryStr);
          
          if ("webdav-method=DELETE".equals(queryStr))
          {
            methodName = "DELETE"; // 폴더 또는 파일 삭제 메소드
          }
          else if ("webdav-method=MKCOL".equals(queryStr))
          {
            methodName = "MKCOL";  // 폴더 생성 메소드
          }
          else if ("webdav-method=PUT".equals(queryStr))
          {
            methodName = "PUT"; // 업로드 메소드
          }
          else if ("webdav-method=COPY".equals(queryStr))
          {
            methodName = "COPY";
          }
          else if ("webdav-method=MOVE".equals(queryStr))
          {
            methodName = "MOVE"; 
          }
          else if ("webdav-method=RENAME".equals(queryStr))
          {
            methodName = "RENAME"; 
          }
          else if ("webdav-method=PROPFIND".equals(queryStr))
          {
            methodName = "PROPFIND";  //프로퍼티 명으로 검색하는 메소드
          }
          else if ("webdav-method=LOCK".equals(queryStr))
          {
            methodName = "LOCK";
          }
          else if ("webdav-method=UNLOCK".equals(queryStr))
          {
            methodName = "UNLOCK";
          }
          else if(queryStr.equals("fileExplorer"))  // 요청이 파일 탐색기라면
          {
        	  
          }
        }
        
        Log.d("methodName",methodName);
        
        if (LOG.isTraceEnabled())
            debugRequest(methodName, req);

        try {
            Principal userPrincipal = req.getUserPrincipal();
            transaction = _store.begin(userPrincipal);
            needRollback = true;
            _store.checkAuthentication(transaction);
            resp.setStatus(WebdavStatus.SC_OK);

            try {
                IMethodExecutor methodExecutor = (IMethodExecutor) _methodMap
                        .get(methodName);
                if (methodExecutor == null) {
                    methodExecutor = (IMethodExecutor) _methodMap
                            .get("*NO*IMPL*");
                }
                
                if (methodExecutor.overlapped(transaction, req, resp, true)) {
                    while (methodExecutor.overlapped(transaction, req, resp, false));
                    methodExecutor.executeOverlapped(transaction, req, resp);
                  }
                  else {
                	  // ImethodExecutor 인터페이스 상속 받은 메소드 execute 실행
                    methodExecutor.execute(transaction, req, resp);
                  }
                
                // ImethodExecutor 인터페이스 상속 받은 메소드 execute 실행
                //methodExecutor.execute(transaction, req, resp);
                
                Log.d("methodExecutor.execute","methodExecutor.execute");
                
                // 트랜잭션 commit이라.... ㅋㅋ
                _store.commit(transaction);
                
                needRollback = false;
            } catch (IOException e) {
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                e.printStackTrace(pw);
                LOG.error("IOException: " + sw.toString());
                resp.sendError(WebdavStatus.SC_INTERNAL_SERVER_ERROR);
                _store.rollback(transaction);
                throw new ServletException(e);
            }

        } catch (UnauthenticatedException e) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
        } catch (WebdavException e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            LOG.error("WebdavException: " + sw.toString());
            throw new ServletException(e);
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            LOG.error("Exception: " + sw.toString());
        } finally {
            if (needRollback)
                _store.rollback(transaction);
        }

    }

    private void debugRequest(String methodName, HttpServletRequest req) {
        LOG.trace("-----------");
        LOG.trace("WebdavServlet\n request: methodName = " + methodName);
        LOG.trace("time: " + System.currentTimeMillis());
        LOG.trace("path: " + req.getRequestURI());
        LOG.trace("-----------");
        Enumeration<?> e = req.getHeaderNames();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            LOG.trace("header: " + s + " " + req.getHeader(s));
        }
        e = req.getAttributeNames();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            LOG.trace("attribute: " + s + " " + req.getAttribute(s));
        }
        e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            LOG.trace("parameter: " + s + " " + req.getParameter(s));
        }
    }

}
