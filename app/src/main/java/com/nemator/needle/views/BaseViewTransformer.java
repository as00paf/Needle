package com.nemator.needle.views;

import android.view.View;

public abstract class BaseViewTransformer implements ViewTransformer {

    public static final float MAX_DIM_ALPHA = 0.7f;

    @Override
    public float getDimAlpha(float translation, float maxTranslation, float peekedTranslation, CustomBottomSheetLayout parent, View view) {
        float progress = translation / maxTranslation;
        return progress * MAX_DIM_ALPHA;
    }

}