package com.nemator.needle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.TypedValue;

public class CropCircleTransformation implements com.squareup.picasso.Transformation {

    private int radius;
    private int strokeWidth;
    private int strokeColor;

    public CropCircleTransformation(Context context, int radius, int strokeWidth, int strokeColor) {
        this.radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, context.getResources().getDisplayMetrics());
        this.strokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, strokeWidth, context.getResources().getDisplayMetrics());
        this.strokeColor = strokeColor;
    }

    @Override
    public Bitmap transform(final Bitmap source) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawCircle((source.getWidth() - strokeWidth)/2, (source.getHeight() - strokeWidth)/2, radius-strokeWidth, paint);

        if (source != output) {
            source.recycle();
        }

        Paint paint1 = new Paint();
        paint1.setColor(strokeColor);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        paint1.setStrokeWidth(strokeWidth);
        canvas.drawCircle((source.getWidth() - strokeWidth)/2, (source.getHeight() - strokeWidth)/2, radius-strokeWidth, paint1);


        return output;
    }

    @Override
    public String key() {
        return "CropCircleTransformation";
    }
}