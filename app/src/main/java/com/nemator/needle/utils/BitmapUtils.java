package com.nemator.needle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Alex on 19/03/2016.
 */
public class BitmapUtils {

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Bitmap scaleBitmap(Bitmap source, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / source.getWidth(),
                (float) maxImageSize / source.getHeight());

        int width = Math.round((float) ratio * source.getWidth());
        int height = Math.round((float) ratio * source.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(source, width,
                height, filter);
        return newBitmap;
    }

    public static Bitmap addCircularBorderToBitmap(Bitmap bitmap, int borderSize, int borderColor){
        Bitmap result = null;
        if( bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            int radius = Math.min(height / 2, width / 2);
            result = Bitmap.createBitmap(width + 8, height + 8, Bitmap.Config.ARGB_8888);

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            Canvas canvas = new Canvas(result);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setStyle(Paint.Style.FILL);

            canvas.drawCircle((width / 2) + 4, (height / 2) + 4, radius, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(bitmap, 4, 4, paint);
            paint.setXfermode(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(borderColor);
            paint.setStrokeWidth(borderSize);
            canvas.drawCircle((width / 2) + 4, (height / 2) + 4, radius, paint);
        }
        return result;
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2){
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }

    public static Bitmap overlay(Bitmap backgroundBitmap, Bitmap overlayBitmap, float left, float top) {
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(), backgroundBitmap.getConfig());
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, new Matrix(), null);
        canvas.drawBitmap(overlayBitmap, left, top, null);
        return resultBitmap;
    }

    public static class BitmapToBase64 extends AsyncTask<BitmapDecoderParams, Void, Void> {
        public static final String TAG = "BitmapToBase64";

        @Override
        protected Void doInBackground(BitmapDecoderParams... params) {
            Uri file = params[0].filePath;
            if(file == null)
                return null;

            InputStream inputStream = null;//You can get an inputStream using any IO API
            try {
                inputStream = getSourceStream(params[0].context, file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if(inputStream == null){
                try {
                    File fileFile = new File(file.getPath());
                    inputStream = new FileInputStream(fileFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            byte[] bytes;
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            bytes = output.toByteArray();
            String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);

            params[0].delegate.onBitmapDecoded(encodedString);

            return null;
        }
    };

    public static FileInputStream getSourceStream(Context mContext, Uri u) throws FileNotFoundException {
        FileInputStream out = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ParcelFileDescriptor parcelFileDescriptor =
                    mContext.getContentResolver().openFileDescriptor(u, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            out = new FileInputStream(fileDescriptor);
        } else {
            out = (FileInputStream) mContext.getContentResolver().openInputStream(u);
        }
        return out;
    }

    public static abstract class BitmapDecoderDelegate{
        public abstract void onBitmapDecoded(String result);
    }

    public static class BitmapDecoderParams{

        public BitmapDecoderDelegate delegate;
        public Uri filePath;
        public Context context;

        public BitmapDecoderParams(Context context, Uri filePath, BitmapDecoderDelegate delegate) {
            this.context = context;
            this.filePath = filePath;
            this.delegate = delegate;
        }
    }
}
