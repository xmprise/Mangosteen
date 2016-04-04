package org.mem.action;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Servlet implementation class WallPaperServlet
 */
public class WallPaperServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ContentResolver resolver = null;
	private Context context = null;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WallPaperServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Log.d("WallPaperServlet.init", "시발 들어왔어");
		this.resolver = (ContentResolver) getServletContext().getAttribute(
				"org.mem.mangosteen.contentResolver");
		this.context = (Context) getServletContext().getAttribute(
				"org.mem.mangosteen.context");

		Log.d("resolver", resolver.toString());
		Log.d("context", context.toString());
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Log.d("WallPaperServlet.doGet", "시발 들어왔어");
		OutputStream os = response.getOutputStream();
		response.setStatus(200);
		response.setContentType("image/png");
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inDither = false;
		
		Drawable wallPaper = WallpaperManager.getInstance(context).getDrawable();
		Bitmap wallPaperBitmap = ((BitmapDrawable)wallPaper).getBitmap();
		Bitmap reSizeWallPaperBitmap = Bitmap.createScaledBitmap(wallPaperBitmap, 178, 307, true);
		
		reSizeWallPaperBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		
		os.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
