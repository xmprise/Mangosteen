/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.webdav;

import java.io.File;
import java.lang.reflect.Constructor;

import javax.servlet.ServletException;

import net.sf.webdav.exceptions.WebdavException;
import android.content.Context;
import android.util.Log;

/**
 * Servlet which provides support for WebDAV level 2.
 * 
 * the original class is org.apache.catalina.servlets.WebdavServlet by Remy
 * Maucherat, which was heavily changed
 * 
 * @author Remy Maucherat
 */

public class WebdavServlet extends WebDavServletBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String ROOTPATH_PARAMETER = "rootpath";

    @Override
    public void init() throws ServletException {
    	
    	Log.d("WebdavServlet","WebdavServlet_init");
        // Web.xml 에 정의된 초기 파라미터 값 설정
    	// ResourceHandlerImplementation ==> "net.sf.webdav.LocalFileSystemStore"
    	
        String clazzName = getServletConfig().getInitParameter(
                "ResourceHandlerImplementation");
        if (clazzName == null || clazzName.equals("")) {
            clazzName = LocalFileSystemStore.class.getName();
        }
        
        //web.xml 에 초기 파라미터 값으로 루트 경로 읽어옴..
        // /mnt/sdcard 경로 파일 객체 받아옴
        File root = getFileRoot();

        
        //이게 뭘까ㅠㅠ?
        IWebdavStore webdavStore = constructStore(clazzName, root);

        //이건 또 뭐니?
        boolean lazyFolderCreationOnPut = getInitParameter("lazyFolderCreationOnPut") != null
                && getInitParameter("lazyFolderCreationOnPut").equals("1");
        String dftIndexFile = getInitParameter("default-index-file");
        String insteadOf404 = getInitParameter("instead-of-404");
        int noContentLengthHeader = getIntInitParameter("no-content-length-headers");

       Log.d("lazyFolderCreationOnPut", String.valueOf(lazyFolderCreationOnPut));
       Log.d("default-index-file",dftIndexFile);
       Log.d("instead-of-404",insteadOf404);
       Log.d("no-content-length-headers", Integer.toString(noContentLengthHeader));
        
        // getAttribute 설정 맞게 바꾸기...
        WebdavScan.init((Context)getServletContext().getAttribute("org.mem.mangosteen.context"));
        
        super.init(webdavStore, dftIndexFile, insteadOf404,
                noContentLengthHeader, lazyFolderCreationOnPut);
    }

    private int getIntInitParameter(String key) {
        return getInitParameter(key) == null ? -1 : Integer
                .parseInt(getInitParameter(key));
    }

    protected IWebdavStore constructStore(String clazzName, File root) {
        IWebdavStore webdavStore;
        try {
            Class<?> clazz = WebdavServlet.class.getClassLoader().loadClass(
                    clazzName);
            

            Constructor<?> ctor = clazz
                    .getConstructor(new Class[] { File.class });

            webdavStore = (IWebdavStore) ctor
                    .newInstance(new Object[] { root });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("some problem making store component", e);
        }
        return webdavStore;
    }

    private File getFileRoot() {
    	
    	// rootPath Web.xml에 /mnt/sdcard 로  기본 설정..
        String rootPath = getInitParameter(ROOTPATH_PARAMETER);
        if (rootPath == null) {
            throw new WebdavException("missing parameter: "
                    + ROOTPATH_PARAMETER);
        }
        if (rootPath.equals("*WAR-FILE-ROOT*")) {
            String file = LocalFileSystemStore.class.getProtectionDomain()
                    .getCodeSource().getLocation().getFile().replace('\\', '/');
            if (file.charAt(0) == '/'
                    && System.getProperty("os.name").indexOf("Windows") != -1) {
                file = file.substring(1, file.length());
            }

            int ix = file.indexOf("/WEB-INF/");
            if (ix != -1) {
                rootPath = file.substring(0, ix).replace('/',
                        File.separatorChar);
            } else {
                throw new WebdavException(
                        "Could not determine root of war file. Can't extract from path '"
                                + file + "' for this web container");
            }
        }
        return new File(rootPath);
    }

}
