package com.example.acoustics.vocproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Acoustics on 7/12/2015.
 */
public class OpenActivity extends Activity {
    private ImageView imageView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent i = new Intent(OpenActivity.this, StartActivity.class);
                OpenActivity.this.startActivity(i);
                OpenActivity.this.finish();
            }
        }, 1000);
    }

    private float getBitmapScalingFactor(Bitmap bm) {
        // Get display width from device
        int displayWidth = getWindowManager().getDefaultDisplay().getWidth();

        // Get margin to use it for calculating to max width of the ImageView
        LinearLayout.LayoutParams layoutParams =
                (LinearLayout.LayoutParams)this.imageView.getLayoutParams();
        int leftMargin = layoutParams.leftMargin;
        int rightMargin = layoutParams.rightMargin;

        // Calculate the max width of the imageView
        int imageViewWidth = displayWidth - (leftMargin + rightMargin);

        // Calculate scaling factor and return it
        return ( (float) imageViewWidth / (float) bm.getWidth() );
    }
}
