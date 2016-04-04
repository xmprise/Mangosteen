package org.mem.action;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MediaRestServlet extends HttpServlet
{
  private static final String TAG = "MediaRstSrvlt";
  public static final String __PG_START_PARAM = "pgStart";
  public static final String __PG_SIZE_PARAM = "pgSize";
  public static final int __DEFAULT_PG_START = 0;
  public static final int __DEFAULT_PG_SIZE = 10;
  private ContentResolver resolver=null;
  private Context context=null;
  private String sdcardState = null;
  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);
    Log.d("MediaRestServlet.init","시발 들어왔어");
    this.resolver = (ContentResolver)getServletContext().getAttribute("org.mem.mangosteen.contentResolver");
    this.context = (Context)getServletContext().getAttribute("org.mem.mangosteen.context");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
	  Log.d("MediaRestServlet.doGet","시발 들어왔어");  
	  
	 // 요청 Path 정보 가져 오기 => 어떤 타입의 요청이 들어 왔는지 확인
    String pathInfo = request.getPathInfo();
    
    //sd카드가 마운트 됐는지 안됐는지 확인...
    sdcardState = android.os.Environment.getExternalStorageState();
    if (sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED)) {
    	
    	
    }
    
    if (pathInfo == null)
    {
      Log.w("MediaRstSrvlt", "pathInfo was null, returning 404");
      response.setStatus(404);
      return;
    }

    Log.d("MediaRstSrvlt.doGet", "PathInfo: " + pathInfo);

    String type = null;  // 요청 미디어 타입 image / video / music / doc
    String location = null; //?????
    String id = null; // ?????
    String action = null; //?????

    StringTokenizer strtok = new StringTokenizer(pathInfo, "/");
    if (strtok.hasMoreElements())
    {
      type = strtok.nextToken();
    }

    if (strtok.hasMoreElements())
    {
      location = strtok.nextToken();
    }

    if (strtok.hasMoreElements())
    {
      id = strtok.nextToken();
    }

    Log.d("MediaRestServlet.doGet","type = "+type);
    Log.d("MediaRestServlet.doGet","location = "+location);
    Log.d("MediaRestServlet.doGet","id = "+id);
    
    doGetJson(request, response, type, location, id);
  }

  public void doGetJson(HttpServletRequest request, HttpServletResponse response, String type, String location, String id)
    throws ServletException, IOException
  {
	 Log.d("MediaRestServlet.doGetJson","시발 들어왔어");
    response.setContentType("application/json; charset=utf-8");
    PrintWriter writer = response.getWriter();


    if (id == null)
    {
      String tmp = request.getParameter("pgStart");
      int pgStart = tmp == null ? -1 : Integer.parseInt(tmp.trim());
      tmp = request.getParameter("pgSize");
      int pgSize = tmp == null ? -1 : Integer.parseInt(tmp.trim());

      Uri mediaUri = MediaType.getContentUriByType(type, location);
      StringBuilder builder = new StringBuilder();

      MediaCollection collection = null;

      writer.println("{");
      try
      {
        collection = new MediaCollection(this.resolver.query(mediaUri, null, null, null, "title ASC"), pgStart, pgSize);
        writer.println("\"total\": " + collection.getTotal() + ", "); // 커서의 수 반환...
        writer.println("\"media\": ");
        writer.print("[ ");

        ContentValues media = null;
        int count = pgSize;

        while (((pgSize <= 0) || (count-- > 0)) && ((media = collection.next()) != null))
        {
          builder.setLength(0);
          toJson(media, mediaUri, builder, type, location);
          Log.d("MediaRstSrvlt", builder.toString());
          writer.print(builder.toString());

          if (!collection.hasNext())
            continue;
          writer.print(",");
        }

        writer.print(" ]");
      }
      finally
      {
        writer.println("}");
        collection.close();
      }
    }
  }

  private void toJson(ContentValues media, Uri contenturi, StringBuilder builder, String type, String location)
  {
	  Log.d("MediaRestServlet.toJson","시발 들어왔어");
    builder.append("{");
    builder.append(" \"type\":").append(safeJson(type));
    builder.append(",\"location\":").append(safeJson(location));
    builder.append(",\"id\":").append(safeJson(media.getAsInteger("_id")));
    builder.append(",\"title\":").append(safeJson(media.getAsString("title")));
    builder.append(",\"displayname\":").append(safeJson(media.getAsString("_display_name")));
    builder.append(",\"mimetype\":").append(safeJson(media.getAsString("mime_type")));
    builder.append(",\"size\":").append(safeJson(media.getAsString("_size")));

    if ((contenturi == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) || (contenturi == MediaStore.Audio.Media.INTERNAL_CONTENT_URI))
    {
      String tmp = media.getAsString("artist");
      if (tmp != null) {
        builder.append(",\"artist\":").append(safeJson(tmp));
      }
      tmp = media.getAsString("album");
      if (tmp != null) {
        builder.append(",\"album\":").append(safeJson(tmp));
      }
    }
    builder.append("}");
  }

  private String safeJson(Number num)
  {
    if (num == null)
    {
      return "";
    }
    return num.toString();
  }

  private String safeJson(String str)
  {
    if (str == null)
    {
      return "\"\"";
    }
    return "\"" + str.replaceAll("'", "\\'") + "\"";
  }

  public class MediaCollection extends DatabaseCollection
  {
    public MediaCollection(Cursor cursor)
    {
    	
      super(cursor);
    }

    public MediaCollection(Cursor cursor, int startPos, int limit)
    {
      super(cursor, startPos, limit);
    }

    public ContentValues cursorToValues(Cursor cursor)
    {
    	Log.d("cursorToValues","시발 들어왔어");
      ContentValues values = new ContentValues();

      String val = cursor.getString(cursor.getColumnIndex("_id"));
      values.put("_id", val);

      int idx = -1;
      idx = cursor.getColumnIndex("title");
      if (idx > -1) {
        values.put("title", cursor.getString(idx));
      }
      idx = cursor.getColumnIndex("_display_name");
      if (idx > -1) {
        values.put("_display_name", cursor.getString(idx));
      }
      idx = cursor.getColumnIndex("mime_type");
      if (idx > -1) {
        values.put("mime_type", cursor.getString(idx));
      }
      idx = cursor.getColumnIndex("_size");
      if (idx > -1) {
        values.put("_size", cursor.getString(idx));
      }
      idx = cursor.getColumnIndex("is_music");
      if (idx > -1)
      {
        int music = cursor.getInt(idx);
        if (music > 0)
        {
          idx = cursor.getColumnIndex("artist");
          if (idx > -1)
            values.put("artist", cursor.getString(idx));
          idx = cursor.getColumnIndex("album");
          if (idx > -1) {
            values.put("album", cursor.getString(idx));
          }
        }
      }
      return values;
    }
  }
}