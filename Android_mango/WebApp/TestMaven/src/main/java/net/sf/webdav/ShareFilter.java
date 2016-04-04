package net.sf.webdav;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShareFilter
  implements FileFilter
{
  private static final Logger LOG = LoggerFactory.getLogger(ShareFilter.class);
  private static final String TAG = "ShareFilters";
  private static final String UPLOAD = "upload";
  private static final String DNLOAD = "download";
  private static final String MODIFY = "modify";
  private static final String PREFIX4URI = "/console/webdav";
  private List<String> _V4SHARE;
  private boolean _onShareFilter = false;

  private File _root = null;
  private String _path4root = "";
  private long _dbLastModified = 0L;

  public ShareFilter(File root)
  {
    this._V4SHARE = new ArrayList();
    this._root = root;
    this._path4root = this._root.getAbsolutePath();
  }

  public void begin(Principal principal)
  {
    if (principal.toString().compareTo("user") == 0) {
      this._onShareFilter = true;
    }
    else {
      this._onShareFilter = false;
    }

    if (checkShareDB()) LoadShareFolder();
  }

  private boolean checkShareDB()
  {
    String paht4db = "/data/data/com.pantech.app.mws/jetty/db/share.db";
    File file4db = new File(paht4db);
    long dbLastModified = file4db.lastModified();
    boolean updated = false;

    if (this._dbLastModified != dbLastModified) {
      this._dbLastModified = dbLastModified;
      updated = true;
    }

    return updated;
  }

  private void LoadShareFolder()
  {
    FileReader reader = null;
    BufferedReader bufReader = null;
    String file4db = "/data/data/com.pantech.app.mws/jetty/db/share.db";
    String db4share = "";
    JSONObject jobj4share = null;
    JSONArray jarr4share = null;
    try
    {
      reader = new FileReader(file4db);
      bufReader = new BufferedReader(reader);
      StringBuilder sb = new StringBuilder();
      int bufferSize = 1048576;
      char[] readbuf = new char[bufferSize];
      int resultSize = 0;

      while ((resultSize = bufReader.read(readbuf)) != -1) {
        if (resultSize == bufferSize) {
          sb.append(readbuf); continue;
        }

        for (int i = 0; i < resultSize; i++) {
          sb.append(readbuf[i]);
        }
      }

      bufReader.close();
      reader.close();

      db4share = sb.toString();

      jobj4share = new JSONObject(db4share);
      jarr4share = jobj4share.getJSONArray("folder");

      for (int i = 0; (jarr4share != null) && (i < jarr4share.length()); i++) {
        this._V4SHARE.add(((JSONObject)jarr4share.get(i)).getString("folder"));
      }

    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public boolean accept(File file)
  {
    boolean accept = false;

    if (this._onShareFilter)
    {
      if (file.isDirectory())
      {
        for (int i = 0; i < this._V4SHARE.size(); i++) {
          String share = (String)this._V4SHARE.get(i);
          String to = file.getAbsolutePath();

          share = share.replace("/console/webdav", this._path4root);

          if (!share.endsWith("/")) share = share + "/";
          if (!to.endsWith("/")) to = to + "/";

          if ((share.startsWith(to)) || (to.startsWith(share))) {
            accept = true;
            break;
          }

        }

      }
      else
      {
        for (int i = 0; i < this._V4SHARE.size(); i++) {
          String share = (String)this._V4SHARE.get(i);

          share = share.replace("/console/webdav", this._path4root);

          if (file.getParent().startsWith(share)) {
            accept = true;
            break;
          }
        }
      }
    }
    else
    {
      accept = true;
    }

    return accept;
  }
}
/*package net.sf.webdav;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ShareFilter
  implements FileFilter
{
  private static final Logger LOG = LoggerFactory.getLogger(ShareFilter.class);
  private static final String TAG = "ShareFilters";
  private static final String UPLOAD = "upload";
  private static final String DNLOAD = "download";
  private static final String MODIFY = "modify";
  private static final String PREFIX4URI = "/console/webdav";
  private List<String> _V4SHARE;
  private boolean _onShareFilter = false;

  private File _root = null;
  private String _path4root = "";
  private long _dbLastModified = 0L;

  public ShareFilter(File root)
  {
    this._V4SHARE = new ArrayList();
    this._root = root;
    this._path4root = this._root.getAbsolutePath();
  }

  public void begin(Principal principal)
  {
    if (principal.toString().compareTo("user") == 0) {
      this._onShareFilter = true;
    }
    else {
      this._onShareFilter = false;
    }

    if (checkShareDB()) LoadShareFolder();
  }

  private boolean checkShareDB()
  {
    String paht4db = "/data/data/com.pantech.app.mws/jetty/db/share.db";
    File file4db = new File(paht4db);
    long dbLastModified = file4db.lastModified();
    boolean updated = false;

    if (this._dbLastModified != dbLastModified) {
      this._dbLastModified = dbLastModified;
      updated = true;
    }

    return updated;
  }

  private void LoadShareFolder()
  {
    FileReader reader = null;
    BufferedReader bufReader = null;
    
    // 이부분 수정해야할듯????????
    // 공유설정하면 여기 파일에 json 형태로 저장 되는듯....
    String file4db = "/data/data/com.pantech.app.mws/jetty/db/share.db";
    String db4share = "";
    
    
    //JSONObject jobj4share = null;
    //JSONArray jarr4share = null;
    Gson gson = null;
    String json = null;
    JsonParser parser = null;
    JsonArray array = null;
    JsonObject jsonObject = null;
    try
    {
      reader = new FileReader(file4db);
      bufReader = new BufferedReader(reader);
      StringBuilder sb = new StringBuilder();
      int bufferSize = 1048576;
      char[] readbuf = new char[bufferSize];
      int resultSize = 0;

      while ((resultSize = bufReader.read(readbuf)) != -1) {
        if (resultSize == bufferSize) {
          sb.append(readbuf); continue;
        }

        for (int i = 0; i < resultSize; i++) {
          sb.append(readbuf[i]);
        }
      }

      bufReader.close();
      reader.close();

      db4share = sb.toString();

      Log.d("db4share", db4share);
      
      jobj4share = new JSONObject(db4share);
      jarr4share = jobj4share.getJSONArray("folder");
      
      //parser = new JsonParser();
      //jsonObject = parser.parse(db4share).getAsJsonObject();
      
      Log.d("jsonObject", jsonObject.toString());
      
      //array = jsonObject.getAsJsonArray("folder");
      
      Log.d("jsonArray", array.toString());
      
      
      //System.out.println("jobj4share>>"+jobj4share);
      //System.out.println("jarr4share>>"+jarr4share);
      
      for (int i = 0; (array != null) && (i < array.size()); i++) {
    	  //JsonObject getObject = array.get(i).getAsJsonObject();
    	  //Log.d("getObject", getObject.toString());
    	  
    	  this._V4SHARE.add(getObject.get("folder").getAsString());
    	  
    	 // Log.d("getObject.get(folder)", getObject.get("folder").getAsString());
          //this._V4SHARE.add((array.get(i)).getAsJsonObject("folder"));
        }
      
//      for (int i = 0; (jarr4share != null) && (i < jarr4share.length()); i++) {
//        this._V4SHARE.add(((JSONObject)jarr4share.get(i)).getString("folder"));
//      }

    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } //catch (JSONException e) {
//      e.printStackTrace();
//    }
  }

  public boolean accept(File file)
  {
    boolean accept = false;

    if (this._onShareFilter)
    {
      if (file.isDirectory())
      {
        for (int i = 0; i < this._V4SHARE.size(); i++) {
          String share = (String)this._V4SHARE.get(i);
          String to = file.getAbsolutePath();

          share = share.replace("/console/webdav", this._path4root);

          if (!share.endsWith("/")) share = share + "/";
          if (!to.endsWith("/")) to = to + "/";

          if ((share.startsWith(to)) || (to.startsWith(share))) {
            accept = true;
            break;
          }

        }

      }
      else
      {
        for (int i = 0; i < this._V4SHARE.size(); i++) {
          String share = (String)this._V4SHARE.get(i);

          share = share.replace("/console/webdav", this._path4root);

          if (file.getParent().startsWith(share)) {
            accept = true;
            break;
          }
        }
      }
    }
    else
    {
      accept = true;
    }

    return accept;
  }
}*/