package org.mem.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;


/**
 * Servlet implementation class TransThumbnail
 */
public class TransThumbnail extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ContentResolver resolver = null;
	private Context context = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TransThumbnail() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Log.d("TransThumbnail.init", "시발 들어왔어");
		this.resolver = (ContentResolver) getServletContext().getAttribute(
				"org.mem.mangosteen.contentResolver");
		this.context = (Context) getServletContext().getAttribute(
				"org.mem.mangosteen.context");

		Log.d("resolver", resolver.toString());
		Log.d("context", context.toString());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		Log.d("TransThumbnail.doGet", "우아아아아 들어왔어");

		// 뒤에 매개변수 URI 정보
		String saveFullPath = request.getPathInfo();
		String contextPath = request.getContextPath();
		String requestURI = request.getRequestURI();
		String command = requestURI.substring(contextPath.length());
		
		int subIndex = command.indexOf(saveFullPath);

		//String type = command.substring(1, subIndex);
		String type = request.getServletPath().substring(1);

		Log.d("TransThumbnail", "PathInfo: " + saveFullPath);
		Log.d("TransThumbnail", "contextPath: " + contextPath);
		Log.d("TransThumbnail", "requestURI: " + requestURI);
		Log.d("TransThumbnail", "command: " + command);
		Log.d("TransThumbnail", "getQueryString: " + request.getQueryString());
		Log.d("TransThumbnail.Type", "Type: " + type);

		response.setStatus(200);
		response.setContentType("image/png");

		Bitmap bitmap = null;
		OutputStream os = null;
		try {
			os = response.getOutputStream();

			if (type.equals("video") || type.equals("mobile/video")) {
				//File file = new File(saveFullPath);
				bitmap = getVideoThumbBitmap(saveFullPath);
			} else if (type.equals("image")  || type.equals("mobile/image")) {
				bitmap = getImageThumbBitmap(saveFullPath);
			}else if(type.equals("audio")  || type.equals("mobile/audio"))
			{
				bitmap = getAudioThumbBitmap(saveFullPath);
			}
			
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

			// BufferedInputStream bufferedInput;
			// bufferedInput = new BufferedInputStream(new
			// FileInputStream(path));

			// bitmap = BitmapFactory.decodeStream(bufferedInput, null,
			// options);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*
		 * File f = new File(path); FileInputStream fis = new
		 * FileInputStream(f);
		 * 
		 * IO.copy(fis, os);
		 * 
		 * mediaCursor.close();
		 */
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		Log.d("TransThumbnail.doPost", "우아아아아 들어왔어");

		//
		String pathInfo = request.getPathInfo();

		Log.d("TransThumbnail", "PathInfo: " + pathInfo);

	}

	private Bitmap getImageThumbBitmap(String saveFullPath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inDither = false;
		options.inJustDecodeBounds = true; // 이게 존나 중요.. true면 원본 이미지를 로딩하지는 않고
											// 크기 정보만 얻어올 수 있음.

		BitmapFactory.decodeFile(saveFullPath, options); // 여기서 로딩안하고 options에
															// 원본 크기 정보 얻어오고

		int width = options.outWidth; // 원본 이미지 넓이 구해오고
		int height = options.outHeight; // 원본 이미지 높이 구해오고

		// 이거 두개는 썸네일 기준크기
		float maxResX = 100;

		// 사이즈 감소 비율
		int ratio = 1;

		if (width >= height) {
			// 만약 기준크기 보다 작다면 ratio는 1값 들어오고 , 기준크기 보다 크다면 감소할 비율이 들어오고..
			ratio = (int) Math.ceil(width / maxResX); // 매개 변수 x보다 크거나 같으면서 가장 가까운 정수반환
		} else if (width < height) {
			ratio = (int) Math.ceil(height / maxResX);
		}

		options.inDither = false;
		options.inJustDecodeBounds = false;
		options.inSampleSize = ratio;

		Log.d("saveFullPath", saveFullPath);

		Bitmap bitmap = BitmapFactory.decodeFile(saveFullPath, options);

		Log.d("넓이", Integer.toString(bitmap.getWidth()));
		Log.d("높이", Integer.toString(bitmap.getHeight()));

		return bitmap;
	}

	private Bitmap getVideoThumbBitmap(String videoPath) {
		/*
		ContentValues localContentValues = new ContentValues();
		localContentValues.put("_data", paramFile.getPath());
		localContentValues.put("title", paramFile.getName());
		localContentValues.put(
				"mime_type",
				"video/"
						+ paramFile.getPath().substring(
								1 + paramFile.getPath().lastIndexOf(".")));
		resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				localContentValues);
		
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		Cursor videoCursor;

		String[] proj = { MediaStore.Video.Media._ID };
		String selectionArg[] = { paramFile.getPath() };
		Log.d("videoFullPath",  paramFile.getPath());
		videoCursor = resolver.query(uri, proj, "_data = ?", selectionArg,
				null);

		videoCursor.moveToFirst();
		int videoIDColIndex = videoCursor.getColumnIndexOrThrow("_id");
		int videoID = videoCursor.getInt(videoIDColIndex);
		Log.d("videoID", Integer.toString(videoID));
		 */
		Bitmap videoBitmap = ThumbnailUtils.createVideoThumbnail(videoPath, android.provider.MediaStore.Video.Thumbnails.MICRO_KIND);
		
		/*
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inDither = false;
		options.inJustDecodeBounds = true;
		
		Bitmap videoBitmap = MediaStore.Video.Thumbnails.getThumbnail(resolver,
				videoID, 1, options);
		*/
		return videoBitmap;
	}

	private Bitmap getAudioThumbBitmap(String audioPath)
	{
		String[] audioPaths = { audioPath };
		Bitmap audioBitmap = null;
		//audioPaths[0] = audioPath;
		String[]  proj = {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.SIZE};
		
		String[]  proj2 = {
				MediaStore.Audio.AudioColumns._ID,
				MediaStore.Audio.AudioColumns.ALBUM_ID,
				MediaStore.Audio.AudioColumns.ALBUM_ART};
		
		//MediaStore.Audio.Media로 먼저 컨텐츠 추출...
		Cursor cursor = context.getContentResolver().query( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, "_data = ?", audioPaths, null);
		//String albumIDArray[] = null;
		//ArrayList<String> albumIDArray = new ArrayList<String>();
		//String albumArtArray[] = null;
		// 추출한 컨텐츠에서 앨범 ID 값들만 또 다시 추출
		String albumID = null;
		
		if (cursor != null && cursor.moveToFirst()) {
			//int id = cursor.getColumnIndex(proj[0]);
			int albumIDNum = cursor.getColumnIndex(proj[1]); // 앨범 아이디의 열의 순번값을 추출
			// 각 이미지의 전체 경로를 담는 리스트
			do {
				albumID = cursor.getString(albumIDNum);
				Log.d("albumID", albumID);
				// 앨범 아이디 값들만 다시 추출
			} while (cursor.moveToNext());
		}
		
		//직접 불러올 수 있는 메쏘드들을 제공해 주는데, 이를 사용하면 쿼리의 수도 줄고 편리하다. 앨범 id만 알고 있으면 바로 uri를 통해 이미지를 생성할 수 있다.
		//앨범아트는 uri를 제공하지 않으므로, 별도로 생성한다
		Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
		
		//앨범아트 uri에 앨범id를 붙여 해당 컨텐츠의 uri 생성( /albumart/앰범 아이디 )
		Uri contentsUri = ContentUris.withAppendedId( albumArtUri, Integer.parseInt(albumID));

		//URI 에서 파일로 읽어 오기
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inDither = false;
			
			ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor( contentsUri, "r" );
			audioBitmap = BitmapFactory.decodeFileDescriptor( fd.getFileDescriptor(), null , options );

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(audioBitmap == null)
		{
			return getDefaultThumbBitmap();
		}
		else{
			Bitmap resizeAudioBitmap =  Bitmap.createScaledBitmap(audioBitmap, 100, 100, true);
			return resizeAudioBitmap;
		}
			
	}
	
	private Bitmap getDefaultThumbBitmap()
	{	
		String URL_ROOT = getServletContext().getRealPath(".");
		String defaultThumbPath = URL_ROOT + "/images/default_thumb.png";
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inDither = false;

		Bitmap bitmap = BitmapFactory.decodeFile(defaultThumbPath, options);

		Log.d("넓이", Integer.toString(bitmap.getWidth()));
		Log.d("높이", Integer.toString(bitmap.getHeight()));

		return bitmap;
	}
	
	
	private boolean loadCachedVideoThumnail() {

		return true;
	}

	public void makeThumbnail() {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inDither = false;
			// options.inJustDecodeBounds = true;
			options.inSampleSize = 8;

			// String path = "/sdcard/DCIM/camera/1313597316775.jpg";
			String saveFolderPath;
			String saveFullPath = null;
			Bitmap bitmap = null;

			Log.d("saveFullPath", saveFullPath);

			bitmap = BitmapFactory.decodeFile(saveFullPath, options);

			Log.d("넓이", Integer.toString(bitmap.getWidth()));
			Log.d("높이", Integer.toString(bitmap.getHeight()));

			File fileCacheItem = new File(saveFullPath);
			OutputStream out = null;

			fileCacheItem.createNewFile();

			out = new FileOutputStream(fileCacheItem);

			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

			// BufferedInputStream bufferedInput;
			// bufferedInput = new BufferedInputStream(new
			// FileInputStream(path));

			// bitmap = BitmapFactory.decodeStream(bufferedInput, null,
			// options);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

	}
}
