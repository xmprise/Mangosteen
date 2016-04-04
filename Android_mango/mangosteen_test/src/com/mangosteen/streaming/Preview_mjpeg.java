package com.mangosteen.streaming;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class Preview_mjpeg extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "Preview";
	public HTTPThread HttpThread;
	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;
	int port;
	private static int quality, resolution_width, resolution_height;
	Thread thread;
	Preview_mjpeg(Context context, int quality, int resolution_width,
			int resolution_height, int PORT) {
		super(context);
		this.quality = quality;
		this.resolution_width = resolution_width;
		this.resolution_height = resolution_height;
		port = PORT;
		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}



	public void switchCamera(Camera camera) {
//		setCamera(camera);
//		try {
//			camera.setPreviewDisplay(mHolder);
//		} catch (IOException exception) {
//			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
//		}
//		Camera.Parameters parameters = camera.getParameters();
//		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//		requestLayout();
//
//		camera.setParameters(parameters);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.
		final int width = resolveSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		setMeasuredDimension(width, height);
		
		 if (mSupportedPreviewSizes != null) {
		 mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width,
		 height);
		 }
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height
						/ previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0,
						(width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width
						/ previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2, width,
						(height + scaledChildHeight) / 2);
			}
		}
	}

	int use = 0;
	int image_um = 0;

	public void surfaceCreated(SurfaceHolder holder) {
//		Log.e("Preview_hls", "surfaceCreated");
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		HttpThread = new HTTPThread();
		HttpThread.start();
		synchronized ("CameraOpen") {
			try {
				mCamera = Camera.open();
			} catch (Exception e) {
				Log.e(TAG, "Camera open Exception", e);
			}
			mSupportedPreviewSizes = mCamera.getParameters()
					.getSupportedPreviewSizes();
			requestLayout();
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e1) {
				Log.e(TAG, "IOException caused by setPreviewDisplay()", e1);
				mCamera.release();
				mCamera = null;
				HttpThread.interrupt();
			}
		}

		try {
			if (mCamera != null) {
				mCamera.setPreviewCallback(new PreviewCallback() {
					public void onPreviewFrame(final byte[] data,
							final Camera camera) {
						// TODO Auto-generated method stub
						image_um++;
						final int imagenum = image_um;
						if(!server.socket.isEmpty() || !server.exsocket.isEmpty())
						{
							thread = new Thread(new Runnable() {
								public void run() {
									Camera.Parameters params = camera
											.getParameters();
									int w = params.getPreviewSize().width;
									int h = params.getPreviewSize().height;
									// int w = resolution_width;
									// int h = resolution_height;
									int format = params.getPreviewFormat();
									YuvImage image = new YuvImage(data, format,
											w, h, null);
									ByteArrayOutputStream out = new ByteArrayOutputStream();
									Rect area = new Rect(0, 0, w, h);
									image.compressToJpeg(area, quality, out);
									Message msg = new Message();
									msg.obj = out.toByteArray();
									msg.arg1 = imagenum;
//									Log.e("http Server", " " + msg.arg1);
									
									HttpThread.hhandler.sendMessage(msg);
								}
							});
							thread.start();
						}
					}
				});
			}
		} catch (Exception exception) {
			thread.interrupt();
			HttpThread.interrupt();
//			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	private HttpServer server;

	class HTTPThread extends Thread {
		public void run() {
			server = new HttpServer(port);
		}

		int sendimg_num = -1;
		Handler hhandler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					if (server.sockon == true && sendimg_num <= msg.arg1) {
						//synchronized (this)
						{
							sendimg_num = msg.arg1;
							if(msg.arg1 > 10000000)
							{
								image_um = image_um = 0;
							}
						}
//						Log.e("http Server", " " + msg.arg1);
						server.Send((byte[]) msg.obj);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
	}


	public void surfaceDestroyed(SurfaceHolder holder) {
//		Log.e("surfaceDestroyed","surfaceDestroyed");
		// Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
//        	Log.e("Destoryed mCamera","Destoryed mCamera not null");
        	mCamera.stopPreview();
    		mCamera.setPreviewCallback(null);
    		mCamera.release();	
    		mCamera=null;
        }
    	if(thread !=null)
    	{
//    		Log.e("onStop thread","onStop thread not null");
    		thread.interrupt();
    	}
    	if(HttpThread !=null)
    	{
//    		Log.e("onStop HttpThread","onStop HttpThread not null");
			if (server != null)
				server.Stop();
        	HttpThread.interrupt();
    	}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		 Camera.Parameters parameters = mCamera.getParameters();
		 parameters.setPreviewSize(resolution_width, resolution_height);
		 requestLayout();
		
		 mCamera.setParameters(parameters);
		mCamera.startPreview();
	}
}