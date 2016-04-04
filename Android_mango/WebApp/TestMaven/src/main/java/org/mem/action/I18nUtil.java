package org.mem.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class I18nUtil
{
  public static final String DEFAULT_CHARSET = "MS949";
  public static final String KOR_CHARSET = "MS949";
  public static final String ENG_CHARSET = "ISO-8859-1";

  public static String E2K(String english)
  {
    String korean = null;

    if (english == null) {
      return null;
    }
    try
    {
      korean = new String(english.getBytes("ISO-8859-1"), "MS949");
    } catch (UnsupportedEncodingException e) {
      korean = new String(english);
    }

    return korean;
  }

  public static String K2E(String korean)
  {
    String english = null;

    if (korean == null) {
      return null;
    }
    try
    {
      english = new String(korean.getBytes("MS949"), "ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      english = new String(korean);
    }

    return english;
  }

  public static String encode(String s)
  {
    try
    {
      return URLEncoder.encode(s, "MS949"); } catch (UnsupportedEncodingException e) {
    }
    return s;
  }
}
