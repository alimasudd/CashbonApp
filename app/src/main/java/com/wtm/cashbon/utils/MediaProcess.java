package com.wtm.cashbon.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import com.wtm.cashbon.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by @alimasudd on 9/12/2019.
 */

public class MediaProcess {

    public static final int CAMERA_REQUEST_CODE = 1313;
    public static final int GALLERY_REQUEST_CODE = 1133;
    public static final int SELECT_PHOTO = 1100;
    public static final int SELECT_FILE = 1200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final String FILE_DIRECTORY_NAME = "ImageProcessing";
    private static final String assets_file = "assets_file";



    private static Context context;
    public MediaProcess(Context context){
        this.context = context;

    }


    public static String createTransactionID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase() + System.currentTimeMillis();
    }

    public static Bitmap scaledBitmap(String imgPath) {

//        BitmapFactory.Options bounds = new BitmapFactory.Options();
//        bounds.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(imgPath, bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inDither = true;
        Bitmap bm = BitmapFactory.decodeFile(imgPath, opts);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imgPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;


        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
//        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, opts.outWidth, opts.outHeight, matrix, true);


        // Resize

        float newWidth = rotatedBitmap.getWidth();
        float newHeight = rotatedBitmap.getHeight();


        if (rotatedBitmap.getHeight() > rotatedBitmap.getWidth()) {

            //orientation = "portrait";
            if (rotatedBitmap.getHeight() > 1280) {
                newHeight = 1280;
                newWidth = newHeight / rotatedBitmap.getHeight() * rotatedBitmap.getWidth();
            }

        } else {

            //orientation = "lanscape";
            if (rotatedBitmap.getWidth() > 1280) {
                newWidth = 1280;
                newHeight = newWidth / rotatedBitmap.getWidth() * rotatedBitmap.getHeight();
            }

        }


        int width = rotatedBitmap.getWidth();
        int height = rotatedBitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrixs = new Matrix();
        // RESIZE THE BIT MAP
        matrixs.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                rotatedBitmap, 0, 0, width, height, matrixs, false);
//        rotatedBitmap.recycle();

        bitmapToFile(resizedBitmap, imgPath, 55);

        return resizedBitmap;

    }


    public static Bitmap scaledBitmapV2(String filePath) {

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        int maxHeightWidth = 1280;


        if (actualHeight > actualWidth) {
            if (actualHeight > maxHeightWidth) {
                float newHeight = maxHeightWidth;
                float newWidth = newHeight / actualHeight * actualWidth;


                actualHeight = (int) newHeight;
                actualWidth = (int) newWidth;
            }
        } else {
            if (actualWidth > maxHeightWidth) {
                float newWidth = maxHeightWidth;
                float newHeight = newWidth / actualWidth * actualHeight;

                actualHeight = (int) newHeight;
                actualWidth = (int) newWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = filePath;
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return scaledBitmap;

    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }


    public static boolean CopyAndClose(InputStream sourceLocation, FileOutputStream targetLocation) {

        InputStream inStream = null;
        OutputStream outStream = null;

        boolean success = false;

        try {

            inStream = sourceLocation;
            outStream = targetLocation;

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {

                outStream.write(buffer, 0, length);

            }

            inStream.close();
            outStream.close();

            success = true;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;

    }

    public static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStorageDirectory(),
                FILE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

                return null;
            }
        }
        // Create a media file name
        String nametimeStamp = generateFileName("IMG", "jpg");

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + nametimeStamp);
        } else {
            return null;
        }

        return mediaFile;
    }

    public static String generateFileName(String front, String type) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());

        return front + "_" + timeStamp + "_" + System.currentTimeMillis() + "." + type;
    }

    public static String getFilenameFromUri(Context context, Uri uri) {

        String fileName = null;

        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        } else if (scheme.equals("content")) {
            String[] proj = {MediaStore.Video.Media.TITLE};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                cursor.moveToFirst();
                fileName = cursor.getString(columnIndex);
            }
        }
        return fileName;
    }

    public static String getFilenameFromUriV2(Context context, Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();

            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    // Its Work Well
    public static String getFilePathFromUri(Context context, Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                }
            } finally {
                cursor.close();
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
        }
        return fileName;
    }

    public static String getFilenameFromPath(String path) {
        String fileName = "";
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }

        return fileName;
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static String getExtentionFromFile(String fileName) {
        String extension = null;
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }

        if (extension == null) {

            extension = "UNKNOWN";
        }
        return extension + "";
    }

    public static boolean bitmapCompress(String path, int level) {

        boolean isSuccess = false;
        File newFile = new File(path);
        OutputStream outStream = null;

        try {
            // make a new bitmap from your file
            Bitmap bitmap = BitmapFactory.decodeFile(newFile.getAbsolutePath());
            outStream = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, level, outStream);
            outStream.flush();
            outStream.close();
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isSuccess;

    }

    public static boolean bitmapToFile(Bitmap bitmap, String path, int level) {

        boolean isSuccess = false;

        try {

            if (!bitmap.isRecycled()) {
                File f = new File(path);
                if (f.exists()) {
                    f.delete();
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, level, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
                bos.close();

            }

            isSuccess = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            isSuccess = true;
        }

        return isSuccess;
    }

    public static String getMediaPath() {

        File mediaStorageDir = new File(
                Environment
                        .getExternalStorageDirectory(),
                FILE_DIRECTORY_NAME);

        return mediaStorageDir.getAbsolutePath();
    }

    public static String getMimeTypeV2(File file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        return mimetype;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    public static File generateTimeStampPhotoFile() {

        File outputDir = getPhotoDirectory();
        File photoFile = null;
        if (outputDir != null) {
            photoFile = new File(outputDir, System.currentTimeMillis()
                    + ".jpg");
        }

        return photoFile;
    }

    public static Uri generateTimeStampPhotoUri(File photoFile) {

        Uri photoFileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);

        return photoFileUri;
    }

    public static File getPhotoDirectory() {

        File outputDir = new File(context.getFilesDir(), assets_file);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        return outputDir;
    }

    public static byte[] getFileToArray(String path) {

        byte[] filecontent = null;
        try {
            File fff = new File(path);
            FileInputStream fileInputStream = new FileInputStream(fff);

            long byteLength = fff.length(); // byte count of the file-content

            filecontent = new byte[(int) byteLength];
            fileInputStream.read(filecontent, 0, (int) byteLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filecontent;
    }



}
