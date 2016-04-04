package org.mem.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

/**
 * Reads an <code>application/octet-stream</code> and writes it to a file.
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @author Allan O'Driscoll
 * @version 1.0
 */
public class FileUploadServlet2 extends HttpServlet {

    private static final long serialVersionUID = 6748857432950840322L;
    private static final String DESTINATION_DIR_PATH = "files";
    private static String realPath;
    private ContentResolver resolver;
    private Context androidContext;
    /**
     * {@inheritDoc}
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Log.d("FileUploadServlet2", "시발 들어왔어");
        this.resolver = (ContentResolver) getServletContext().getAttribute(
				"org.mem.mangosteen.contentResolver");
		this.androidContext = (Context) getServletContext().getAttribute(
				"org.mem.mangosteen.context");
        //realPath = getServletContext().getRealPath(DESTINATION_DIR_PATH) + "/";
		
		// 기본 저장 경로 설정
		realPath = "/mnt/sdcard/";
        Log.d("realPath",realPath);
    }

    public ContentResolver getContentResolver() {
		return this.resolver;
	}
    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
    	Log.d("FileUploadServlet_Post","FileUploadServlet_Post");
    	
    	// 한글 설정..
    	response.setContentType("text/html;charset=utf-8");
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 스트림 선언
        PrintWriter writer = null;
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            writer = response.getWriter();
        } catch (IOException ex) {
            log(FileUploadServlet2.class.getName() + "has thrown an exception: " + ex.getMessage());
        }

        // 헤더에서 파일에 대한 정보 얻어 옴
        // 파일이름
        String filename = request.getHeader("X-File-Name");
        // 파일타입
        String type = request.getHeader("X-File-Type");
        // 저장 경로
        realPath = request.getHeader("X-File-UploadPath");
        
        Log.d("filename",filename);
        Log.d("realPath",realPath);
        
        try {
            is = request.getInputStream();
            fos = new FileOutputStream(new File(realPath + filename));
            IOUtils.copy(is, fos);
            response.setStatus(response.SC_OK);
            PerImageBean perImageBean = new PerImageBean();
            perImageBean.setName(filename);
            perImageBean.setPath(realPath);
            perImageBean.setType(type);
            
            Gson gson = new Gson();
    		String json = gson.toJson(perImageBean);
    		Log.d("perImageBean",json);
    		writer.print(json);
            //writer.print("{\"success\": true}");
        } catch (FileNotFoundException ex) {
            response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
            writer.print("{\"success\": false}");
            log(FileUploadServlet2.class.getName() + "has thrown an exception: " + ex.getMessage());
        } catch (IOException ex) {
            response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
            writer.print("{\"success\": false}");
            log(FileUploadServlet2.class.getName() + "has thrown an exception: " + ex.getMessage());
        } finally {
            try {
                fos.close();
                is.close();
            } catch (IOException ignored) {
            }
        }

        writer.flush();
        writer.close();
    }
}