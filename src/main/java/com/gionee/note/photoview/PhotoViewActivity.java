package com.gionee.note.photoview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.gionee.aminote.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

public class PhotoViewActivity extends Activity {
    private static final boolean DEBUG = false;
    private static final String ISLOCKED_ARG = "isLocked";
    private static final String TAG = "PhotoViewActivity";
    private ViewPager mViewPager;

    private class SamplePagerAdapter extends PagerAdapter {
        static final /* synthetic */ boolean $assertionsDisabled = (!PhotoViewActivity.class.desiredAssertionStatus());
        private LayoutInflater mInflater;
        private DisplayImageOptions mOptions = new Builder().showImageForEmptyUri((int) R.drawable.ic_launcher).showImageOnFail((int) R.drawable.photo_view_loading_fail).resetViewBeforeLoading(true).cacheOnDisk(false).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Config.ARGB_8888).considerExifParams(true).displayer(new FadeInBitmapDisplayer(300)).build();
        private String[] mUriStrs;

        public SamplePagerAdapter(Context context, String[] uris) {
            this.mInflater = LayoutInflater.from(context);
            this.mUriStrs = uris;
        }

        public int getCount() {
            return this.mUriStrs.length;
        }

        public View instantiateItem(ViewGroup container, int position) {
            View imageLayout = this.mInflater.inflate(R.layout.photeview_pager_image, container, false);
            if ($assertionsDisabled || imageLayout != null) {
                ImageView photoView = (PhotoView) imageLayout.findViewById(R.id.image);
                final ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.loading);
                ImageLoader.getInstance().displayImage(this.mUriStrs[position], photoView, this.mOptions, new SimpleImageLoadingListener() {
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        String message;
                        switch (failReason.getType()) {
                            case IO_ERROR:
                                message = "Input/Output error";
                                break;
                            case DECODING_ERROR:
                                message = "Image can't be decoded";
                                break;
                            case NETWORK_DENIED:
                                message = "Downloads are denied";
                                break;
                            case OUT_OF_MEMORY:
                                message = "Out Of Memory error";
                                break;
                            case UNKNOWN:
                                message = "Unknown error";
                                break;
                        }
                        progressBar.setVisibility(8);
                    }

                    public void onLoadingStarted(String imageUri, View view) {
                        progressBar.setVisibility(0);
                    }

                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        progressBar.setVisibility(8);
                    }
                });
                photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
                    public void onPhotoTap(View view, float v, float v1) {
                        PhotoViewActivity.this.goBack();
                    }
                });
                container.addView(imageLayout, -1, -1);
                return imageLayout;
            }
            throw new AssertionError();
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoview_activity_layout);
        this.mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        Intent intent = getIntent();
        String[] uriStrs = intent.getStringArrayExtra("imageUris");
        int currentImage = intent.getIntExtra("currentImage", 0);
        for (int i = 0; i < uriStrs.length; i++) {
            uriStrs[i] = Uri.parse(Uri.decode(uriStrs[i])).toString();
        }
        this.mViewPager.setAdapter(new SamplePagerAdapter(this, uriStrs));
        this.mViewPager.setCurrentItem(currentImage);
        if (savedInstanceState != null) {
            ((HackyViewPager) this.mViewPager).setLocked(savedInstanceState.getBoolean(ISLOCKED_ARG, false));
        }
    }

    private void goBack() {
        finish();
    }

    private void toggleViewPagerScrolling() {
        if (isViewPagerActive()) {
            ((HackyViewPager) this.mViewPager).toggleLock();
        }
    }

    private boolean isViewPagerActive() {
        return this.mViewPager != null && (this.mViewPager instanceof HackyViewPager);
    }

    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (isViewPagerActive()) {
            outState.putBoolean(ISLOCKED_ARG, ((HackyViewPager) this.mViewPager).isLocked());
        }
        super.onSaveInstanceState(outState);
    }
}
