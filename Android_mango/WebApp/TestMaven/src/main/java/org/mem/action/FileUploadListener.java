package org.mem.action;

import org.apache.commons.fileupload.ProgressListener;

public class FileUploadListener
  implements ProgressListener
{
  private volatile long bytesRead = 0L; private volatile long contentLength = 0L; private volatile long item = 0L; private volatile long megaBytes = -1L;

  public void update(long aBytesRead, long aContentLength, int anItem)
  {
    this.bytesRead = aBytesRead;
    this.contentLength = aContentLength;
    this.item = anItem;
  }

  public long getBytesRead()
  {
    return this.bytesRead;
  }

  public long getContentLength()
  {
    return this.contentLength;
  }

  public long getItem()
  {
    return this.item;
  }
}
