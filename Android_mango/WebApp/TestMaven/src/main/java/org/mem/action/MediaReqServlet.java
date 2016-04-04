package org.mem.action;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

/**
 * Servlet implementation class MediaReqServlet
 */
public class MediaReqServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String TAG = "MediaReqServlet";

	public static final String __PG_START_PARAM = "pgStart";
	public static final String __PG_SIZE_PARAM = "pgSize";

	public static final int __DEFAULT_PG_START = 0;
	public static final int __DEFAULT_PG_SIZE = 10;

		private ContentResolver resolver = null;
		private Context context = null;
	private String sdcardState = null;

	private ArrayList<String> folderPathList = null;
	private ArrayList<String[]> perFolderImgList = null;
	private HashMap<String, String[]> perFolderImgMap = null;

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
	public MediaReqServlet() {
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

		Log.d("MediaRestServlet.doGet", "시발 들어왔어");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		Log.d("MediaRestServlet.doPost", "시발 들어왔어");

		// 요청 Path 정보 가져 오기 => 어떤 타입의 요청이 들어 왔는지 확인
		String pathInfo = request.getParameter("id");
		Log.d("MediaReqServlet.pathInfo", pathInfo);

		// sd카드가 마운트 됐는지 안됐는지 확인...
		sdcardState = android.os.Environment.getExternalStorageState();
		if (sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED)) {

		}

		if (pathInfo == null) {
			Log.w("MediaReqServlet", "pathInfo was null, returning 404");
			response.setStatus(404);
			return;
		}

		String type = null; // 요청 미디어 타입 image / video / music / doc
		String location = null; // ?????
		String id = null; // ?????
		String action = null; // ?????

		StringTokenizer strtok = new StringTokenizer(pathInfo, "/");
		if (strtok.hasMoreElements()) {
			type = strtok.nextToken();
		}

		if (strtok.hasMoreElements()) {
			location = strtok.nextToken();
		}

		if (strtok.hasMoreElements()) {
			id = strtok.nextToken();
		}

		Log.d("MediaRestServlet.doGet", "type = " + type);
		Log.d("MediaRestServlet.doGet", "location = " + location);
		Log.d("MediaRestServlet.doGet", "id = " + id);
		
		// 이미지가 들어있는 폴더별 리스트
		//folderPathList = thumbnails.getImageDir();
		
		getImgHash();
	}

	
	// HashMap에  폴더 별  이미지 파일 리스트 관리
	public void getImgHash()
	{
		perFolderImgMap = new HashMap<String, String[]>();
		
		for(String path : folderPathList)
		{
			File imgListFile = new File(path);
			
			String[] imgList = imgListFile.list(new FilenameFilter() {
				
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					Boolean bOK = false;
					if(filename.toLowerCase().endsWith(".png"))
						bOK = true;					
					if(filename.toLowerCase().endsWith(".9.png"))				
						bOK = true;
					if(filename.toLowerCase().endsWith(".gif"))
						bOK = true;
					if(filename.toLowerCase().endsWith(".jpg"))
						bOK = true;
					
					return bOK;
				}
			});
			
			perFolderImgMap.put(path, imgList);
			
			Log.d("MediaRestServlet.imgList", imgList.toString());
		}
	}
	
	// 썸네일 Bitmap 전송....
	public void send(HttpServletRequest request, HttpServletResponse response)
	{
		String type = "image";
		//String imgName = 
		
	}

	// 썸네일 Bitmap 생성
	// 이미지 크기 비율적 줄여야 하나?
	public void makeThumbnail()
	{
		String[] imgList = null;
		int imgListCount=0;
		
		for(int i=0;i<perFolderImgMap.size();i++)
		{
			imgList = perFolderImgMap.get(folderPathList.get(i)); // 각 폴더안 이미지 파일 리스트
			
			imgListCount = imgList.length;  // 각 폴더안 이미지 파일 개수
			
			for(String imgPath : imgList)
			{
				
			}
		}
	}
	
	
}
