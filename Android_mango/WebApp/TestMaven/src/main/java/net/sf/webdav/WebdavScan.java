package net.sf.webdav;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import org.mem.action.MediaScanner;
import java.io.File;

public class WebdavScan
{
  private static Context _context = null;

  public static void init(Context context) {
    _context = context;
  }

  public static void scan(File file) {
    MediaScanner scanner = null;

    Log.i("WebdavScan", "+scan( " + file + " )");

    scanner = new MediaScanner(_context);
    scanner.scanFile(file.toString(), null);
  }

  public static void scan() {
    Log.i("WebdavScan", "+scan(all)");
    _context.sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://" + Environment.getExternalStorageDirectory())));
  }
}