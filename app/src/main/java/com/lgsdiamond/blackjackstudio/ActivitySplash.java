package com.lgsdiamond.blackjackstudio;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;
import com.lgsdiamond.outsource.photoview.PhotoViewAttacher;

public class ActivitySplash extends Activity {
    private final int NUM_SPLASH_IMAGES = 7;

    private int[] mSplashImageId = new int[NUM_SPLASH_IMAGES];

    int mSplashImageIndex;
    LinearLayout layoutSplash;
    ImageView ivSplash;
    PhotoViewAttacher mAttacher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final String SPLASH_IMAGE_HEADER = "splash_image_";

        super.onCreate(savedInstanceState);
        //Fix to portrait display
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_splash);

        layoutSplash = (LinearLayout) findViewById(R.id.loSplash);

        ivSplash = (ImageView) findViewById(R.id.ivSplash);

        for (int index = 0; index < NUM_SPLASH_IMAGES; index++) {
            mSplashImageId[index] = UtilityStudio.getResourceId(getResources(),
                    SPLASH_IMAGE_HEADER + String.valueOf(index), "drawable",
                    getPackageName());
        }
        mSplashImageIndex = 0;
        ivSplash.setImageResource(mSplashImageId[mSplashImageIndex]);

        mAttacher = new PhotoViewAttacher(ivSplash);
        mAttacher.update();
    }
}