/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.smartelectronics.note.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.smartelectronics.note.R;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class StorageHelper {

  public static boolean checkStorage () {
    boolean mExternalStorageAvailable;
    boolean mExternalStorageWriteable;
    String state = Environment.getExternalStorageState();

    switch (state) {
      case Environment.MEDIA_MOUNTED:
        // We can read and write the media
        mExternalStorageAvailable = mExternalStorageWriteable = true;
        break;
      case Environment.MEDIA_MOUNTED_READ_ONLY:
        // We can only read the media
        mExternalStorageAvailable = true;
        mExternalStorageWriteable = false;
        break;
      default:
        // Something else is wrong. It may be one of many other states, but
        // all we need
        // to know is we can neither read nor write
        mExternalStorageAvailable = mExternalStorageWriteable = false;
        break;
    }
    return mExternalStorageAvailable && mExternalStorageWriteable;
  }


  public static String getStorageDir () {
    // return Environment.getExternalStorageDirectory() + File.separator +
    // Constants.TAG + File.separator;
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
  }

  /**
   * Create a path where we will place our private file on external
   */
  public static String createExternalStoragePrivateFile (Context mContext, Uri uri, String extension) {

    if (!checkStorage()) {
      Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
      return null;
    }
    File file = createNewAttachmentFile(mContext, extension);

    InputStream contentResolverInputStream = null;
    OutputStream contentResolverOutputStream = null;
    try {
      contentResolverInputStream = mContext.getContentResolver().openInputStream(uri);
      contentResolverOutputStream = new FileOutputStream(file);
      copyFile(contentResolverInputStream, contentResolverOutputStream);
    } catch (IOException e) {
      try {
        FileUtils.copyFile(new File(uri.getPath()), file);
        // It's a path!!
      } catch (NullPointerException e1) {
        try {
          FileUtils.copyFile(new File(uri.getPath()), file);
        } catch (IOException e2) {
          file = null;
        }
      } catch (IOException e2) {
        file = null;
      }
    } finally {
      try {
        if (contentResolverInputStream != null) {
          contentResolverInputStream.close();
        }
        if (contentResolverOutputStream != null) {
          contentResolverOutputStream.close();
        }
      } catch (IOException e) {
      }

    }
    return file.getPath();
  }

  public static boolean copyFile (File source, File destination) {
    FileInputStream is = null;
    FileOutputStream os = null;
    try {
      is = new FileInputStream(source);
      os = new FileOutputStream(destination);
      return copyFile(is, os);
    } catch (FileNotFoundException e) {
      return false;
    } finally {
      try {
        if (is != null) {
          is.close();
        }
        if (os != null) {
          os.close();
        }
      } catch (IOException e) {
      }
    }
  }


  /**
   * Generic file copy method
   *
   * @param is Input
   * @param os Output
   * @return True if copy is done, false otherwise
   */
  public static boolean copyFile (InputStream is, OutputStream os) {
    try {
      IOUtils.copy(is, os);
      return true;
    } catch (IOException e) {

      return false;
    }
  }


  public static boolean deleteExternalStoragePrivateFile (Context mContext, String name) {
    // Checks for external storage availability
    if (!checkStorage()) {
      Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
      return false;
    }
    File file = new File(mContext.getExternalFilesDir(null), name);
    return file.delete();
  }


  public static boolean delete (Context mContext, String path) {
    if (!checkStorage()) {
      Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
      return false;
    }
    try {
      FileUtils.forceDelete(new File(path));
    } catch (IOException e) {
      return false;
    }
    return true;
  }


  public static String getRealPathFromURI (Context mContext, Uri contentUri) {
    String[] proj = {MediaStore.Images.Media.DATA};
    Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
    if (cursor == null) {
      return null;
    }
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    String path = cursor.getString(column_index);
    cursor.close();
    return path;
  }


  public static File createNewAttachmentFile (Context mContext, String extension) {
    File f = null;
    if (checkStorage()) {
      f = new File(mContext.getExternalFilesDir(null), createNewAttachmentName(extension));
    }
    return f;
  }


  private static synchronized String createNewAttachmentName (String extension) {
    Calendar now = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT_SORTABLE);
    String name = sdf.format(now.getTime());
    name += extension != null ? extension : "";
    return name;
  }


  public static File createNewAttachmentFile (Context mContext) {
    return createNewAttachmentFile(mContext, null);
  }

  /**
   * Create a path where we will place our private file on external
   */
  public static File copyToBackupDir (File backupDir, String fileName, InputStream fileInputStream) {
    if (!checkStorage()) {
      return null;
    }
    if (!backupDir.exists()) {
      backupDir.mkdirs();
    }
    File destination = new File(backupDir, fileName);
    try {
      copyFile(fileInputStream, new FileOutputStream(destination));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return destination;
  }


  public static File getCacheDir (Context mContext) {
    File dir = mContext.getExternalCacheDir();
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }


  public static File getExternalStoragePublicDir () {
    File dir = new File(
        Environment.getExternalStorageDirectory() + File.separator + Constant.EXTERNAL_STORAGE_FOLDER + File
            .separator);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }


  public static File getBackupDir (String backupName) {
    File backupDir = new File(getExternalStoragePublicDir(), backupName);
    if (!backupDir.exists() && backupDir.mkdirs()) {
      createNoMediaFile(backupDir);
    }
    return backupDir;
  }


  private static void createNoMediaFile (File folder) {
    try {
      new File(folder, ".nomedia").createNewFile();
    } catch (IOException e) {

    }
  }


  public static File getSharedPreferencesFile (Context mContext) {
    File appData = mContext.getFilesDir().getParentFile();
    String packageName = mContext.getApplicationContext().getPackageName();
    return new File(appData
        + System.getProperty("file.separator")
        + "shared_prefs"
        + System.getProperty("file.separator")
        + packageName
        + "_preferences.xml");
  }


  /**
   * Returns a directory size in bytes
   */
  @SuppressWarnings("deprecation")
  public static long getSize (File directory) {
    StatFs statFs = new StatFs(directory.getAbsolutePath());
    long blockSize = 0;
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        blockSize = statFs.getBlockSizeLong();
      } else {
        blockSize = statFs.getBlockSize();
      }
      // Can't understand why on some devices this fails
    } catch (NoSuchMethodError e) {
    }
    return getSize(directory, blockSize);
  }


  private static long getSize (File directory, long blockSize) {
    if (blockSize == 0) {
      throw new InvalidParameterException("Blocksize can't be 0");
    }
    File[] files = directory.listFiles();
    if (files != null) {

      // space used by directory itself
      long size = directory.length();

      for (File file : files) {
        if (file.isDirectory()) {
          // space used by subdirectory
          size += getSize(file, blockSize);
        } else {
          // file size need to rounded up to full block sizes
          // (not a perfect function, it adds additional block to 0 sized files
          // and file who perfectly fill their blocks)
          size += (file.length() / blockSize + 1) * blockSize;
        }
      }
      return size;
    } else {
      return 0;
    }
  }


  public static boolean copyDirectory (File sourceLocation, File targetLocation) {
    boolean res = true;

    // If target is a directory the method will be iterated
    if (sourceLocation.isDirectory()) {
      if (!targetLocation.exists()) {
        targetLocation.mkdirs();
      }

      String[] children = sourceLocation.list();
      for (int i = 0; i < sourceLocation.listFiles().length; i++) {
        res = res && copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation,
            children[i]));
      }

      // Otherwise a file copy will be performed
    } else {
      res = copyFile(sourceLocation, targetLocation);
    }
    return res;
  }


  /**
   * Retrieves uri mime-type using ContentResolver
   */
  public static String getMimeType (Context mContext, Uri uri) {
    ContentResolver cR = mContext.getContentResolver();
    String mimeType = cR.getType(uri);
    if (mimeType == null) {
      mimeType = getMimeType(uri.toString());
    }
    return mimeType;
  }


  public static String getMimeType (String url) {
    String type = null;
    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
    if (extension != null) {
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      type = mime.getMimeTypeFromExtension(extension);
    }
    return type;
  }


  /**
   * Retrieves uri mime-type between the ones managed by application
   */
  public static String getMimeTypeInternal (Context mContext, Uri uri) {
    String mimeType = getMimeType(mContext, uri);
    mimeType = getMimeTypeInternal(mContext, mimeType);
    return mimeType;
  }


  /**
   * Retrieves mime-type between the ones managed by application from given string
   */
  public static String getMimeTypeInternal (Context mContext, String mimeType) {
    if (mimeType != null) {
      if (mimeType.contains("image/")) {
        mimeType = Constant.MIME_TYPE_IMAGE;
      } else if (mimeType.contains("audio/")) {
        mimeType = Constant.MIME_TYPE_AUDIO;
      } else if (mimeType.contains("video/")) {
        mimeType = Constant.MIME_TYPE_VIDEO;
      } else {
        mimeType = Constant.MIME_TYPE_FILES;
      }
    }
    return mimeType;
  }


  /**
   * Creates new attachment from web content
   */
  public static File createNewAttachmentFileFromHttp (Context mContext, String url)
      throws IOException {
    if (TextUtils.isEmpty(url)) {
      return null;
    }



    return getFromHttp(url, createNewAttachmentFile(mContext, getFileExtension(url)));
  }


  /**
   * Retrieves a file from its web url
   */
  public static File getFromHttp (String url, File file) throws IOException {
    URL imageUrl = new URL(url);
    FileUtils.copyURLToFile(imageUrl, file);
    return file;
  }


  public static String getFileExtension (String fileName) {
    if (TextUtils.isEmpty(fileName)) {
      return "";
    }
    String extension = "";
    int index = fileName.lastIndexOf('.');
    if (index != -1) {
      extension = fileName.substring(index);
    }
    return extension;
  }

  /**
   * Trying to retrieve file name from content resolver
   */
  public static String getNameFromUri (Context mContext, Uri uri) {
    String fileName = "";
    Cursor cursor = null;
    try {
      cursor = mContext.getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null);
      if (cursor != null) {
        try {
          if (cursor.moveToFirst()) {
            fileName = cursor.getString(0);
          }

        } catch (Exception e) {
        }
      } else {
        fileName = uri.getLastPathSegment();
      }
    } catch (SecurityException e) {
      return null;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return fileName;
  }

}