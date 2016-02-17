package com.nemator.needle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.TypedValue;

public class CropCircleTransformation implements com.squareup.picasso.Transformation {

    private int strokeWidth;
    private int strokeColor;

    public CropCircleTransformation(Context context, int strokeWidth, int strokeColor) {
        this.strokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, strokeWidth, context.getResources().getDisplayMetrics());
        this.strokeColor = strokeColor;
    }

    @Override
    public Bitmap transform(final Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size/2f;
        int s = strokeWidth * ((size/2)/100);
        canvas.drawCircle(r , r , r - strokeWidth, paint);

        Paint stroke = new Paint();
        stroke.setColor(strokeColor);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setAntiAlias(true);
        stroke.setStrokeWidth(s);
        canvas.drawCircle(r, r, r - s, stroke);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "CropCircleTransformation";
    }
}