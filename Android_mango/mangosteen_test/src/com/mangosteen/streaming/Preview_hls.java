package com.mangosteen.streaming;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.util.List;

import com.mangosteen.streaming.HlsActivity.Encoding;

import android.R;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Directory;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class Preview_hls extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "Preview";
	private String Rootdir = "/mnt/sdcard/jetty/webapps/Mangosteen/hls/temp";
	SurfaceView mSurfaceView;
	public Thread thread;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;
	Encoding EncodingThread;
	double NextTime;
	int plustime = 2000;
	private static int quality, resolution_width, resolution_height;

	Preview_hls(Context context, int quality, int resolution_width,
			int resolution_height, long time, Encoding encodingThread) {
		super(context);
		this.quality = quality;
		this.resolution_width = resolution_width;
		this.resolution_height = resolution_height;
		NextTime = time - time % 1000 + 2000;
		mSurfaceView = new SurfaceView(context);
		EncodingThread = encodingThread;
		addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		File dir = new File(Rootdir + "/image_0");
		dir.mkdir();
	}

	public void switchCamera(Camera camera) {
		// setCamera(camera);
		// try {
		// camera.setPreviewDisplay(mHolder);
		// } catch (IOException exception) {
		// Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		// }
		// Camera.Parameters parameters = camera.getParameters();
		// parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		// requestLayout();
		//
		// camera.setParameters(parameters);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		Log.e("Preview_hls", "onMeasure");
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
//		Log.e("Preview_hls", "onLayout");
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
	int image_um = -1;
	int directory_num = 0;
	int w = 352;
	int h = 288;
	boolean backuse = false;
	Rect area = new Rect(0, 0, w, h);

	public void surfaceCreated(SurfaceHolder holder) {
//		Log.e("Preview_hls", "surfaceCreated");
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
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
			}
		}
		try {
			if (mCamera != null) {
				mCamera.setPreviewCallback(new PreviewCallback() {
					public void onPreviewFrame(final byte[] data,
							final Camera camera) {
						// TODO Auto-generated method stub
						if (!backuse) {
							backuse = true;
							return;
						}
						backuse = false;
						if (NextTime + plustime <= System.currentTimeMillis()) {
							final Message msg = new Message();
							msg.arg1 = 2;
							msg.arg2 = image_um;
							Thread thd = new Thread(new Runnable() {
								public void run() {
									EncodingThread.hhandler.sendMessage(msg);
								}
							});
							thd.start();
							image_um = -1;
							NextTime += plustime;
							directory_num++;
							File dir = new File(Rootdir + "/image_"
									+ directory_num);
							dir.mkdir();
						}
						if (NextTime <= System.currentTimeMillis()) {
							image_um += 2;
							final int imagenum = image_um;
							final int dir_num = directory_num;
							thread = new Thread(new Runnable() {
								public void run() {
									Camera.Parameters params = camera
											.getParameters();
									// int w = params.getPreviewSize().width;
									// int h = params.getPreviewSize().height;

									int format = params.getPreviewFormat();
									YuvImage image = new YuvImage(data, format,
											w, h, null);
									ByteArrayOutputStream out = new ByteArrayOutputStream();
									image.compressToJpeg(area, 100, out);
									FileOutputStream fs;
									try {
										fs = new FileOutputStream(Rootdir
												+ "/image_" + dir_num
												+ "/image" + imagenum + ".jpg");
										fs.write(out.toByteArray());
										fs.close();
										fs = new FileOutputStream(Rootdir
												+ "/image_" + dir_num
												+ "/image" + (imagenum + 1)
												+ ".jpg");
										fs.write(out.toByteArray());
										fs.close();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

							});
							thread.start();
						}
						// Log.e(TAG, "preview");
					}
				});
			}
		} catch (Exception exception) {
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
//		Log.e("Preview_hls", "surfaceDestroyed");
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
    		mCamera.setPreviewCallback(null);
    		mCamera.release();	
    		mCamera=null;
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
//		Log.e("Preview_hls", "surfaceChanged");
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(resolution_width, resolution_height);
		requestLayout();

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}
}