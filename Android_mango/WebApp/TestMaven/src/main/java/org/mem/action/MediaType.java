package org.mem.action;

import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import java.util.HashMap;
import java.util.Map;

public final class MediaType
{
  private static Map<String, Uri[]> mediaMap = new HashMap();
  public static final String TYPE_VIDEO = "video";
  public static final String TYPE_AUDIO = "audio";
  public static final String TYPE_IMAGES = "image";
  public static final String LOCATION_EXTERNAL = "external";
  public static final String LOCATION_INTERNAL = "internal";

  static
  {
    mediaMap.put("image", new Uri[] { MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.INTERNAL_CONTENT_URI });

    mediaMap.put("audio", new Uri[] { MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.INTERNAL_CONTENT_URI });

    mediaMap.put("video", new Uri[] { MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.INTERNAL_CONTENT_URI });
  }
  
  public static Uri getContentUriByType(String mediatype, String location)
  {
    Uri[] uris = (Uri[])mediaMap.get(mediatype);
    if (uris == null) {
      return null;
    }
    if ("external".equalsIgnoreCase(location.trim()))
      return uris[0];
    if ("internal".equalsIgnoreCase(location.trim())) {
      return uris[1];
    }
    return null;
  }

/*  public static Uri[] getContentUrisByType(String mediaType)
  {
    return (Uri[])mediaMap.get(mediaType);
  }*/

  public static Uri getContentUrisByType(String mediaType)
  {
	 Uri[] uris = (Uri[])mediaMap.get(mediaType);
    return uris[0];
  }

}
