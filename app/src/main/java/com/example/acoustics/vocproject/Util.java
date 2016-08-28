package com.example.acoustics.vocproject;

import android.graphics.Bitmap;

/**
 * Created by Acoustics on 7/12/2015.
 */
public class Util {

    public static Bitmap ScaleBitmap(Bitmap bm, float scalingFactor) {
        int scaleHeight = (int) (bm.getHeight() * scalingFactor);
        int scaleWidth = (int) (bm.getWidth() * scalingFactor);

        return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
    }

}