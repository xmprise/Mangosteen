package org.mem.action;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Servlet implementation class PhoneInformation
 */
public class PhoneInformation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ContentResolver resolver = null;
	private Context context = null;

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PhoneInformation() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Log.d("PhoneInformation.init", "시발 들어왔어");
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
		Log.d("PhoneInformation.doGet", "시발 들어왔어");
		response.setContentType("application/json; charset=utf-8");

		PrintWriter writer = response.getWriter();

		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		
		PhoneInfoBean phoneInfoBean = new PhoneInfoBean(); // 폰정보 담을 객체
		
		phoneInfoBean.setVersion(System.getProperty("os.name")+" "+System.getProperty("os.version")); // 안드로이드 버전이름과 버전
		phoneInfoBean.setBrand(Build.BRAND); // 제조사
		phoneInfoBean.setModel(Build.MODEL); // 모델명
		phoneInfoBean.setPhoneNum(tm.getLine1Number()); // 폰번호
		
		Intent bat = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));			
		String battery = bat.getIntExtra("level", -1) + "%";
		Log.d("battery", battery);

		Gson gson = new Gson();
		String json = gson.toJson(phoneInfoBean);
		Log.d("phoneInfoBeanJsonMsg", json);
		writer.println(json);
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
