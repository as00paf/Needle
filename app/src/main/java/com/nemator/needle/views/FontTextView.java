package com.nemator.needle.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;

import com.nemator.needle.R;

/**
 * Created by Alex on 09/02/2016.
 */
public class FontTextView extends android.widget.TextView {

    public FontTextView(Context context) {
        super(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initFont(context, attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFont(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFont(context, attrs);
    }

    private void initFont(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FontTextView,
                0, 0);

        String fontPath = "CircularStdMedium.otf";
        try {
            fontPath = a.getString(a.getIndex(R.styleable.FontTextView_font));
        } finally {
            a.recycle();
        }

        Typeface font = Typeface.createFromAsset(context.getAssets(), fontPath);
        setTypeface(font);
    }
}
