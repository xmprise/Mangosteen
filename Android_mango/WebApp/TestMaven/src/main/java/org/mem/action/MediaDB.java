package org.mem.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

public class MediaDB {

	private Cursor entireImgCursor = null;

	private Cursor dirImgCursor = null;
	private ContentResolver resolver;
	private Context context;
	private ArrayList<String> imageDataList = null; // 모든 이미지 절대경로 저장
	private ArrayList<String> perFolderPathList = null; // 대표 폴더명만 저장
	private HashMap<String, String[]> perFolderImgMap = null;
	private String type= null;
	public MediaDB(ContentResolver resolver) {
		this.resolver = resolver;
	}

	public void getEntireImageCursor(String mediaType) {

		Uri uri = MediaType.getContentUrisByType(mediaType);
		
		Log.d("uriType", uri.toString());
		
		if(mediaType.equals("image"))
		{
			String[] proj = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE,MediaStore.Images.Media.DISPLAY_NAME };
			perTypeCursor(proj, uri);
		}
		else if(mediaType.equals("video"))
		{
			String[] proj = {
					MediaStore.Video.VideoColumns._ID,  
					  MediaStore.Video.VideoColumns.DATA,  
					  MediaStore.Video.VideoColumns.DISPLAY_NAME};
			perTypeCursor(proj, uri);
		}
		else if(mediaType.equals("audio"))
		{
			// MediaStore.Audio.Media로 먼저 컨텐츠 검색후
			String[]  proj = {
					MediaStore.Audio.AudioColumns._ID,
					MediaStore.Audio.AudioColumns.DATA,
					MediaStore.Audio.AudioColumns.SIZE,
					MediaStore.Audio.AudioColumns.DISPLAY_NAME};
			
			perTypeCursor(proj, uri);
		}
			
		getImageDir();
		// entireThumbCursor =
		// resolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
		// proj2, "_ID = ?", (String[])imgIDList.toArray(new
		// String[imgIDList.size()]), null);

		// entireThumbCursor = resolver.query(
		// MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, proj2,
		// MediaStore.Images.Thumbnails.IMAGE_ID + "=?", null, null);
		//
		// entireThumbCursor.moveToFirst();
		//
		// if (entireThumbCursor != null && entireThumbCursor.moveToFirst()) {
		// int imageDataCol = entireThumbCursor
		// .getColumnIndex(MediaStore.Images.Thumbnails.DATA);
		// String imageData;
		//
		// imageDataList = new ArrayList<String>(); // 각 이미지의 전체 경로를 담는 리스트
		//
		// do {
		// imageData = entireThumbCursor.getString(imageDataCol); // 각 이미지의
		// // 전체 경로
		// // 가져 오고
		//
		// Log.d("imageData", imageData);
		//
		// imageDataList.add(imageData);
		//
		// } while (entireThumbCursor.moveToNext());
		//
		// // getImageDir();
		// }
		// entireThumbCursor.close();
		//
		// Log.d("entireThumbCursor.getCount()",
		// Integer.toString(entireThumbCursor.getCount()));

		// makeThumbnail();
	}

	public void perTypeCursor(String[] proj, Uri uri) {
		String orderBy = proj[1]+" desc";
		entireImgCursor = resolver.query(uri, proj, null, null, orderBy); // 경로별로 정렬했는데 ㅠㅠ,,

		entireImgCursor.moveToFirst();

		if (entireImgCursor != null && entireImgCursor.moveToFirst()) {
			int imgDataCol = entireImgCursor.getColumnIndex(proj[1]);

			String imgPath; //

			imageDataList = new ArrayList<String>(); // 각 이미지의 전체 경로를 담는 리스트

			do {
				imgPath = entireImgCursor.getString(imgDataCol);

				// Log.d("imageData", imgID);
				imageDataList.add(imgPath);

			} while (entireImgCursor.moveToNext());
		}
		Log.d("imageDataList.Count",
				Integer.toString(imageDataList.size()));
		entireImgCursor.close();
	}

	public ArrayList<String> getImageDir() {
		String imageParentPath = null;
		String tempPath = null;
		perFolderPathList = new ArrayList<String>();

		for (String imageData : imageDataList) {
			File file = new File(imageData);
			imageParentPath = file.getParent() + "/"; // 이미지 파일명 빼고 폴더명까지의 절대경로
														// 얻어오고
			// Log.d("ParentPath", imageParentPath);

			if (perFolderPathList.size() == 0) // 구분 디렉토리 없으면
			{
				perFolderPathList.add(imageParentPath); // /sdcard/ 기본으로 삽입
				tempPath = imageParentPath;
				Log.d("pathSize0", imageParentPath);
			} else if (!imageParentPath.equals(tempPath)) {
				perFolderPathList.add(imageParentPath);
				tempPath = imageParentPath;
				Log.d("UniquePath", imageParentPath);
			}
		}
		Log.d("UniquePath", imageParentPath);
		getImgHash();

		return perFolderPathList;
	}

	// HashMap에 폴더 별 이미지 파일 리스트 관리
	public HashMap<String, String[]> getImgHash(String mediaType) {

		getEntireImageCursor(mediaType);
		getImageDir();

		perFolderImgMap = new HashMap<String, String[]>();

		for (String path : perFolderPathList) {
			File imgListFile = new File(path); // 폴더안의 

			String[] imgList = imgListFile.list(new FilenameFilter() {

				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					Boolean bOK = false;
					if (filename.toLowerCase().endsWith(".png"))
						bOK = true;
					if (filename.toLowerCase().endsWith(".9.png"))
						bOK = true;
					if (filename.toLowerCase().endsWith(".gif"))
						bOK = true;
					if (filename.toLowerCase().endsWith(".jpg"))
						bOK = true;

					return bOK;
				}
			});

			perFolderImgMap.put(path, imgList);

			Log.d("MediaRestServlet.imgList", imgList.toString());
		}

		return perFolderImgMap;
	}

	// HashMap에 폴더 별 이미지 파일 리스트 관리
	public HashMap<String, String[]> getImgHash() {

		perFolderImgMap = new HashMap<String, String[]>();
		// 음악 파일들이 있는 각 폴더리스트 가져와서
		for (String path : perFolderPathList) {
			File imgListFile = new File(path); // 폴더안의 파일목록 가져오고
			String[] imgList = null; // 폴더안의 파일들의 확장자를 보고, 필요한 파일의 경로를 리스트에 저장
			
			if(type.equals("image"))
			{
				 imgList = imgListFile.list(new FilenameFilter() {

						public boolean accept(File dir, String filename) {
							// TODO Auto-generated method stub
							Boolean bOK = false;
							if (filename.toLowerCase().endsWith(".png"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".9.png"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".gif"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".jpg"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".jpeg"))
								bOK = true;
							return bOK;
						}
					});
			}
			else if(type.equals("video"))
			{
				 imgList = imgListFile.list(new FilenameFilter() {

						public boolean accept(File dir, String filename) {
							// TODO Auto-generated method stub
							Boolean bOK = false;
							if (filename.toLowerCase().endsWith(".mp4"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".avi"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".wma"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".3gp"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".ts"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".mkv"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".aac"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".flv"))
								bOK = true;
							return bOK;
						}
					});
			}
			else if(type.equals("audio"))
			{
				 imgList = imgListFile.list(new FilenameFilter() {

						public boolean accept(File dir, String filename) {
							// TODO Auto-generated method stub
							Boolean bOK = false;
							if (filename.toLowerCase().endsWith(".mp3"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".flac"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".ogg"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".wave"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".dcf"))
								bOK = true;
							if (filename.toLowerCase().endsWith(".amr"))
								bOK = true;
							return bOK;
						}
					});
			}
			Log.d("perFolder_Path", path);
			perFolderImgMap.put(path, imgList);
		}
		return perFolderImgMap;
	}

	public ArrayList<PerFolderBean> getPerFolderBeanList(String mediaType) {
		
		type = mediaType;
		getEntireImageCursor(mediaType);

		String[] imgList = null;

		// 폴더 별 폴더명, 파일개수, 썸네일이미지 주소 저장 객체
		ArrayList<PerFolderBean> perFolderBeanList = new ArrayList<PerFolderBean>();

		for (int i = 0; i < perFolderImgMap.size(); i++) {
			PerFolderBean perFolderBean = new PerFolderBean();
			Log.d("perFolderPathList_Size", Integer.toString(perFolderPathList.size()));
			Log.d("perFolderImgMap_Size", Integer.toString(perFolderImgMap.size()));
			imgList = perFolderImgMap.get(perFolderPathList.get(i)); // 각 폴더안
																		// 이미지
																		// 파일
																		// 리스트

			perFolderBean.setFileOfNum(imgList.length); // 각 폴더안 이미지 파일 개수
			Log.d("imgList.length", Integer.toString(imgList.length));

			String folderName[] = perFolderPathList.get(i).split("/", 0);

			perFolderBean.setFolderName(folderName[folderName.length - 1]);
			Log.d("perFolderPathList", folderName[folderName.length - 1]);

			perFolderBean.setFolderPath(perFolderPathList.get(i)); // 각 폴더명까지 path저장
			
			perFolderBean.setThumbPath(perFolderPathList.get(i) + imgList[0]);
			Log.d("imgList[0]", perFolderPathList.get(i) + imgList[0]);
			
			perFolderBean.setType(type);
			Log.d("type", type);
			/*
			 * for(String imgPath : imgList) {
			 * 
			 * }
			 */

			perFolderBeanList.add(perFolderBean);
		}

		return perFolderBeanList;
	}

	public void getPerDirImageCursor() {
		String[] proj = {
				MediaStore.Images.Media._ID, // 아이디와
				MediaStore.Images.Media.DATA, // 위치와
				MediaStore.Images.Media.SIZE,
				MediaStore.Images.Media.DISPLAY_NAME };
		String[] aa = { "/mnt/sdcard/external_sd/%" };

		// dirImgCursor =
		// resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
		// MediaStore.Images.Media.DATA + " like ? ",(String[]) path.toArray(new
		// String[path.size()]) , null);
		// dirImgCursor =
		// resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
		// MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?",aa, null);
		// dirImgCursor =
		// resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		// ,null,null , null);
		dirImgCursor = resolver.query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
				MediaStore.Images.Media.DATA + " like ? ", aa, null);
		Log.d("dirImgCursorCount", Integer.toString(dirImgCursor.getCount()));
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
			String saveFullPath;
			Bitmap bitmap = null;

			for (String path : imageDataList) {
				String[] pathTmp = path.split("/");
				String fname = pathTmp[(pathTmp.length - 1)];

				Log.d("fname", fname);

				saveFolderPath = "/sdcard/jetty/webapps/Mangosteen/Thumb/";

				File directory = new File(saveFolderPath);

				if (directory.exists() == false) {
					directory.mkdirs();
				}

				saveFullPath = saveFolderPath + fname;

				Log.d("saveFullPath", saveFullPath);

				bitmap = BitmapFactory.decodeFile(path, options);

				Log.d("넓이", Integer.toString(bitmap.getWidth()));
				Log.d("높이", Integer.toString(bitmap.getHeight()));

				File fileCacheItem = new File(saveFullPath);
				OutputStream out = null;

				fileCacheItem.createNewFile();

				out = new FileOutputStream(fileCacheItem);

				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

			}

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
