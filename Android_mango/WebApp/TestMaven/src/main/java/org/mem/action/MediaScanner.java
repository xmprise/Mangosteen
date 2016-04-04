package org.mem.action;

import java.net.URI;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

public class MediaScanner
{
  private MediaScannerConnection mediaScanConn = null;
  private UploadFileSannerClient client = null;
  private String filePath = null;
  private String fileType = null;
  private String[] filePaths = null;

  public MediaScanner(Context context)
  {
    if (this.client == null) {
      this.client = new UploadFileSannerClient();
    }
    
    if (this.mediaScanConn == null)
      this.mediaScanConn = new MediaScannerConnection(context, this.client);
  }

  public void scanFile(String filepath, String fileType)
  {
    Log.i("MediaScanner", "scanFile(" + filepath + ", " + fileType + ")");

    this.filePath = filepath;
    this.fileType = fileType;

    this.mediaScanConn.connect(); // MediaScannerConnection.onMediaScannerConnected()함수 호출
  }

  class UploadFileSannerClient
    implements MediaScannerConnection.MediaScannerConnectionClient
  {
    UploadFileSannerClient()
    {
    	
    }

    // MediaScanner 서비스가 연결이 된 경우 호출
    // 스캐닝할 파일들을  알아서 찾아서  정확한 파일명으로 넣어주어야 된다는 것
    public void onMediaScannerConnected()
    {
      Log.i("MediaScanner", "onMediaScannerConnected(" + MediaScanner.this.filePath + ", " + MediaScanner.this.fileType + ")");

      if (MediaScanner.this.filePath != null) {
        MediaScanner.this.mediaScanConn.scanFile(MediaScanner.this.filePath, MediaScanner.this.fileType);
      }

      if (MediaScanner.this.filePaths != null) {
        for (String file : MediaScanner.this.filePaths) {
          MediaScanner.this.mediaScanConn.scanFile(file, MediaScanner.this.fileType);
        }
      }

      //MediaScanner.access$002(MediaScanner.this, null);
      //MediaScanner.access$102(MediaScanner.this, null);
      //MediaScanner.access$302(MediaScanner.this, null);
    }
    
    // 스캔이 성공한 파일에 대한 정보 전달 함수
	public void onScanCompleted(String path, Uri uri) {
		// TODO Auto-generated method stub
		Log.i("MediaScanner", "onScanCompleted(" + path + ", " + uri + ")");
	      Log.i("MediaScanner", "onScanCompleted(" + path + ", " + uri.toString() + ")");

	      
	      MediaScanner.this.mediaScanConn.disconnect();
	}
  }
}