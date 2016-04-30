package com.gionee.note.app.attachment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import com.gionee.note.app.ImageCache;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.SlidingWindow.NoteEntry;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.common.ThumbnailDecodeProcess;
import com.gionee.note.common.ThumbnailDecodeProcess.ThumbnailDecodeMode;
import java.lang.ref.WeakReference;

public class LocalImageLoader {
    private Context mContext;
    private boolean mExitTasksEarly = false;
    private ImageCache mImageCache;
    private Bitmap mLoadingBitmap;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    private Resources mResources;

    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            this.bitmapWorkerTaskReference = new WeakReference(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return (BitmapWorkerTask) this.bitmapWorkerTaskReference.get();
        }
    }

    private class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private Object mData;
        private int mTargetHeight;
        private int mTargetWith;
        private ThumbnailDecodeMode mThumbnailDecodeMode;

        public BitmapWorkerTask(Object data, ImageView imageView, ThumbnailDecodeMode thumbnailDecodeMode) {
            this.mData = data;
            LayoutParams params = imageView.getLayoutParams();
            this.mTargetWith = params.width;
            this.mTargetHeight = params.height;
            this.mThumbnailDecodeMode = thumbnailDecodeMode;
            this.imageViewReference = new WeakReference(imageView);
        }

        protected Bitmap doInBackground(Void... params) {
            String picUri = "";
            if (this.mData instanceof String) {
                picUri = String.valueOf(this.mData);
            } else if (this.mData instanceof NoteEntry) {
                picUri = ((NoteEntry) this.mData).thumbnailUri.toString();
            }
            Bitmap bitmap = null;
            synchronized (LocalImageLoader.this.mPauseWorkLock) {
                while (LocalImageLoader.this.mPauseWork && !isCancelled()) {
                    try {
                        LocalImageLoader.this.mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (!(null != null || isCancelled() || getAttachedImageView() == null || LocalImageLoader.this.mExitTasksEarly)) {
                bitmap = processBitmap(this.mData, this.mTargetWith, this.mTargetHeight, this.mThumbnailDecodeMode);
            }
            if (!(bitmap == null || LocalImageLoader.this.mImageCache == null)) {
                LocalImageLoader.this.mImageCache.addBitmapToCache(picUri, bitmap);
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap value) {
            if (isCancelled() || LocalImageLoader.this.mExitTasksEarly) {
                value = null;
            }
            ImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                LocalImageLoader.this.setImageDrawable(imageView, value, this.mThumbnailDecodeMode);
            }
        }

        protected void onCancelled(Bitmap value) {
            super.onCancelled(value);
            synchronized (LocalImageLoader.this.mPauseWorkLock) {
                LocalImageLoader.this.mPauseWorkLock.notifyAll();
            }
        }

        private ImageView getAttachedImageView() {
            ImageView imageView = (ImageView) this.imageViewReference.get();
            return this == LocalImageLoader.getBitmapWorkerTask(imageView) ? imageView : null;
        }

        private Bitmap processBitmap(Object data, int targetWith, int targetHeight, ThumbnailDecodeMode thumbnailDecodeMode) {
            if (data instanceof String) {
                return new ThumbnailDecodeProcess(LocalImageLoader.this.mContext, Uri.parse(String.valueOf(data)), targetWith, targetHeight, thumbnailDecodeMode).getThumbnail();
            } else if (!(data instanceof NoteEntry)) {
                return null;
            } else {
                NoteEntry entry = (NoteEntry) data;
                Bitmap bitmap = entry.item.requestImage(entry.mediaType, entry.thumbnailUri);
                if (bitmap == null) {
                    return entry.item.requestImage(entry.mediaType, entry.originUri);
                }
                return bitmap;
            }
        }
    }

    private class PreLoadPicTask implements Job {
        int picHeight;
        String picUri;
        int picWith;

        PreLoadPicTask(String picUri, int picWith, int picHeight) {
            this.picUri = picUri;
            this.picWith = picWith;
            this.picHeight = picHeight;
        }

        public Object run(JobContext jc) {
            Bitmap bitmap = new ThumbnailDecodeProcess(LocalImageLoader.this.mContext, Uri.parse(this.picUri), this.picWith, this.picHeight, ThumbnailDecodeMode.HEIGHT_FIXED_WIDTH_SCALE).getThumbnail();
            if (bitmap != null) {
                LocalImageLoader.this.mImageCache.addBitmapToCache(this.picUri, bitmap);
            }
            return null;
        }
    }

    public LocalImageLoader(Context context) {
        this.mContext = context.getApplicationContext();
        this.mImageCache = ImageCache.getInstance(this.mContext);
        this.mResources = context.getResources();
    }

    public void loadImage(Object data, ImageView imageView, ThumbnailDecodeMode thumbnailDecodeMode) {
        if (data != null) {
            Bitmap value = null;
            String picUri = "";
            if (data instanceof String) {
                picUri = String.valueOf(data);
            } else if (data instanceof NoteEntry) {
                picUri = ((NoteEntry) data).thumbnailUri.toString();
            }
            if (this.mImageCache != null) {
                value = this.mImageCache.getBitmapFromMemCache(picUri);
            }
            if (value != null) {
                setImageDrawable(imageView, value, thumbnailDecodeMode);
            } else if (cancelPotentialWork(data, imageView)) {
                BitmapWorkerTask task = new BitmapWorkerTask(data, imageView, thumbnailDecodeMode);
                imageView.setImageDrawable(new AsyncDrawable(this.mResources, this.mLoadingBitmap, task));
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        }
    }

    public void preLoadImage(String picUrl, int picWith, int picHeight) {
        if (!TextUtils.isEmpty(picUrl) && this.mImageCache.getBitmapFromMemCache(picUrl) == null) {
            NoteAppImpl.getContext().getThreadPool().submit(new PreLoadPicTask(picUrl, picWith, picHeight));
        }
    }

    public void setLoadingImage(Bitmap bitmap) {
        this.mLoadingBitmap = bitmap;
    }

    public void setLoadingImage(int resId) {
        this.mLoadingBitmap = BitmapFactory.decodeResource(this.mResources, resId);
    }

    public static void cancelWork(ImageView imageView) {
        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
        }
    }

    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask == null) {
            return true;
        }
        Object bitmapData = bitmapWorkerTask.mData;
        if (bitmapData != null && bitmapData.equals(data)) {
            return false;
        }
        bitmapWorkerTask.cancel(true);
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                return ((AsyncDrawable) drawable).getBitmapWorkerTask();
            }
        }
        return null;
    }

    private void setImageDrawable(ImageView imageView, Bitmap bitmap, ThumbnailDecodeMode thumbnailDecodeMode) {
        if (thumbnailDecodeMode != ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT) {
            LayoutParams params = imageView.getLayoutParams();
            params.width = bitmap.getWidth();
            imageView.setLayoutParams(params);
        }
        if (bitmap == null) {
            imageView.setImageBitmap(this.mLoadingBitmap);
        }
        imageView.setImageBitmap(bitmap);
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (this.mPauseWorkLock) {
            this.mPauseWork = pauseWork;
            if (!this.mPauseWork) {
                this.mPauseWorkLock.notifyAll();
            }
        }
    }
}
