package com.mangosteen.streaming;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.R.bool;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class HlsActivity extends Activity {
	private Preview_hls mPreview;
	private String Rootdir = "/mnt/sdcard/jetty/webapps/Mangosteen/hls";
	private String tsFilename = "video_";
	private String m3u8Filename = "video.m3u8";	
	int plustime = 2000;
	Encoding encoding;
	AudioPackage audiopackage;
	Thread EncodingThread, AudioThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("HlsActivity", "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		File dir = new File(Rootdir + "/temp");
		if (dir.isDirectory()) {
			DeleteDir(dir.getPath());
		}
		dir.mkdir();
		encoding = new Encoding();
		EncodingThread = new Thread(encoding);
		EncodingThread.start();
		m3u8_make();
		long time = System.currentTimeMillis();
		mPreview = new Preview_hls(this, 20, 352, 288, time, encoding);
		audiopackage = new AudioPackage(time, encoding);
		AudioThread = new Thread(audiopackage);
		AudioThread.start();
		setContentView(mPreview);
	}
	

	public class Encoding extends Thread {
		public void run() {
			// while (true) {
			// try {
			// Thread.sleep(1000);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }
			argv = new String[24];
			argv[0] = "ffmpeg";
			argv[1] = "-r";

			argv[3] = "-vb";
			argv[4] = "5";
			argv[5] = "-qscale";
			argv[6] = "4";
			argv[7] = "-f";
			argv[8] = "image2";
			argv[9] = "-i";
			argv[11] = "-i";
			argv[13] = "-acodec";
			argv[14] = "aac";
			argv[15] = "-strict";
			argv[16] = "experimental";
			argv[17] = "-vcodec";
			argv[18] = "libx264";
			argv[19] = "-preset";
			argv[20] = "ultrafast";
			argv[21] = "-f";
			argv[22] = "mpegts";
			frame = new int[100];
		}

		Boolean audio_video = false;
		int directory_num = 0;
		int audio = -1, video = -1;
		int before = 0;
		String[] argv;
		int[] frame;
		Handler hhandler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					if (msg.arg1 == 1) {
//						Log.e("HlsActivity", "audio " + msg.arg2);
						audio++;

					} else if (msg.arg1 == 2) {
//						Log.e("HlsActivity", "video");
						video++;
						frame[video] = (msg.arg2 + 1) / (plustime/1000);
						// frame[video] = 25;
					}
					if (before <= audio && before <= video)
						audio_video = true;

					if (audio_video) {

						audio_video = false;
//						Log.e("HlsActivity", "encoding " + before);

						final int dir = before;
						before++;
						argv[2] = "" + frame[dir];
						argv[10] = Rootdir + "/temp/image_" + (dir)
								+ "/image%d.jpg";
						argv[12] = Rootdir + "/temp/audio_" + (dir) + ".wav";
						argv[23] = Rootdir +"/temp/video_"
								+ (dir) + ".ts";
						final String[] str = argv;
						final int filenum = dir;
						Thread thread = new Thread(new Runnable() {
							public void run() {
								synchronized ("sad") {
									try {
										Log.e("HlsActivity", "encoding start");
										TSencoding(str);
										m3u8_addfile(dir);
										if(dir>2)
											m3u8_delfile(dir-3);
										Log.e("HlsActivity", "encoding end");
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
//								Log.e("HlsActivity", "del start");
								DeleteDir(Rootdir + "/temp/image_" + dir);
								File file = new File(Rootdir + "/temp/audio_" + dir +".wav");
								file.delete();
								file = new File(Rootdir + "/temp/audio_temp_" + dir +".temp");
								file.delete();
//								Log.e("HlsActivity", "del end");
							}
						});
						thread.setPriority(MAX_PRIORITY);
						thread.start();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
	}

	void m3u8_make()
	{
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(Rootdir + "/" + m3u8Filename));
			out.write("#EXTM3U\n#EXT-X-TARGETDURATION:5\n#EXT-X-MEDIA-SEQUENCE:0\n");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void m3u8_addfile(int filenum)
	{
		BufferedWriter out;
			try {
				out = new BufferedWriter(new FileWriter(Rootdir + "/" + m3u8Filename, true));
				out.write("#EXTINF:2,\ntemp/" + tsFilename + filenum + ".ts\n");
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	void m3u8_delfile(int filenum)
	{
		StringBuffer strbf = new StringBuffer();
		try {
			BufferedReader inp = new BufferedReader(new FileReader(Rootdir +  "/" + m3u8Filename));
			String str;
			while ((str = inp.readLine()) != null) {
				if(str.contains("#EXT-X-MEDIA-SEQUENCE:"))
				{
					str = "#EXT-X-MEDIA-SEQUENCE:" + (filenum+1);
					strbf.append(str + "\n");
					inp.readLine();
					inp.readLine();
				}
				else
					strbf.append(str + "\n");
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(Rootdir + "/" + m3u8Filename));
			out.write(strbf.toString());
			out.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void DeleteDir(String path) {
		File file = new File(path);
		File[] childFileList = file.listFiles();
		for (File childFile : childFileList) {
			if (childFile.isDirectory()) {
				DeleteDir(childFile.getAbsolutePath()); // ���� ���丮 ����
			} else {
				childFile.delete(); // ���� ���ϻ���
			}
		}

		file.delete(); // root ����
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e("HlsActivity", "onResume");
		// Open the default i.e. the first rear facing camera.
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e("HlsActivity", "onPause");
		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		
		if (mPreview.thread != null) {
			mPreview.thread.interrupt();
		}
		if (EncodingThread != null) {
			EncodingThread.interrupt();
		}

		if (AudioThread != null) {
			if (audiopackage != null) {
				audiopackage.stoprecording();
			}
			AudioThread.interrupt();
		}
	}
	@Override
    protected void onStop() {
		super.onStop();
		Log.e("HlsActivity", "onStop");
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
       
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate our menu which can gather user input for switching camera
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.camera_menu, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("HlsActivity", "onDestroy");
	}

	static {
		System.loadLibrary("segmenter");
	}

	public static native int TSh264encoder(String[] argv);

	public static native int TSsegmenter(String[] argv);

	public static native void TSencoding(String[] argv);

}
