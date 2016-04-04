package org.mem.action;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class DatabaseCollection
{
  protected Cursor cursor;
  protected int startPos = 0;
  protected int limit = -1;
  protected int count = 0;

  protected boolean inited = false;

  public DatabaseCollection(Cursor cursor)
  {
    this.cursor = cursor;
  }

  public DatabaseCollection(Cursor cursor, int startPos, int limit)
  {
    this(cursor);
    this.startPos = startPos;
    this.limit = limit;
  }

  public void close()
  {
    if (this.cursor != null)
      this.cursor.close();
  }

  public abstract ContentValues cursorToValues(Cursor paramCursor);

  public long getTotal()
  {
    if (this.cursor != null) {
      return this.cursor.getCount();
    }
    return -1L;
  }

  public int getStartPos()
  {
    return this.startPos;
  }

  public int getLimit()
  {
    return this.limit;
  }

  public boolean hasNext()
  {
    init();
    return (this.cursor != null) && ((this.limit < 0) || (this.count < this.limit)) && (!this.cursor.isLast());
  }

  public ContentValues next()
  {
    ContentValues values = null;

    init();
    if (((this.limit < 0) || (this.count < this.limit)) && (this.cursor.moveToNext()))
    {
      this.count += 1;
      return cursorToValues(this.cursor);
    }

    return values;
  }

  private void init()
  {
    if (!this.inited)
    {
      this.count = 0;
      if (this.cursor != null)
      {
        if (this.startPos > 0)
        {
          if (this.startPos < this.cursor.getCount()) {
            this.cursor.move(this.startPos);
          }
          else {
            this.cursor.moveToLast();
          }
        }
        else if (this.startPos < 0) {
          this.cursor.moveToLast();
        }
      }
      this.inited = true;
    }
  }
}