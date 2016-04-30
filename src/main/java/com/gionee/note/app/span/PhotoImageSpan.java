package com.gionee.note.app.span;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.gionee.note.app.Config.EditPage;
import com.gionee.note.app.DataConvert;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.span.JsonableSpan.Applyer;
import com.gionee.note.common.Constants;
import com.gionee.note.common.DecodeUtils;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.photoview.PhotoViewActivity;
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.senab.photoview.IPhotoView;

public class PhotoImageSpan extends ReplacementSpan implements AbstractClickSpan, JsonableSpan, OnlyImageSpan {
    public static final Applyer<PhotoImageSpan> APPLYER = new Applyer<PhotoImageSpan>() {
        public PhotoImageSpan applyFromJson(JSONObject json, SpannableStringBuilder builder, Context context) throws JSONException {
            int start = json.getInt(DataConvert.SPAN_ITEM_START);
            int end = json.getInt(DataConvert.SPAN_ITEM_END);
            int flag = json.getInt(DataConvert.SPAN_ITEM_FLAG);
            PhotoImageSpan span = new PhotoImageSpan(context, builder, Uri.parse(json.getString(PhotoImageSpan.THUMB_URI)), Uri.parse(json.getString(PhotoImageSpan.ORIGIN_URI)));
            builder.setSpan(span, start, end, flag);
            span.initSpan(start);
            return span;
        }
    };
    private static final boolean DEBUG = false;
    private static final PhotoImageSpan[] EMPTY_ITEM = new PhotoImageSpan[0];
    public static final String ORIGIN_URI = "origin_uri";
    private static final String TAG = "PhotoImageSpan";
    public static final String THUMB_URI = "thumb_uri";
    private Drawable mCacheDrawable;
    private Context mContext;
    private int mImageHeight = 0;
    private int mImageShiftSize = 0;
    private int mImageWidth = 0;
    private OnImageSpanChangeListener mListener;
    private Uri mOriginUri;
    private boolean mOrignalPicExit = true;
    private ImageSpanWatcher mSpanWatcher;
    private SpannableStringBuilder mText;
    private Uri mThumbUri;

    private class ImageSpanWatcher implements SpanWatcher {
        private ImageSpanWatcher() {
        }

        public void onSpanAdded(Spannable text, Object what, int start, int end) {
        }

        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
            if (what == PhotoImageSpan.this) {
                checkDeleteRedundancyPhotoTag(text, start, end);
                if (PhotoImageSpan.this.mSpanWatcher != null) {
                    PhotoImageSpan.this.mText.removeSpan(PhotoImageSpan.this.mSpanWatcher);
                    PhotoImageSpan.this.mSpanWatcher = null;
                }
            }
        }

        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
        }

        private void checkDeleteRedundancyPhotoTag(Spannable spanText, int start, int end) {
            int redundancyPhotoTagLength = Constants.MEDIA_PHOTO.length() - 1;
            if (redundancyPhotoTagLength == end - start) {
                String redundancyPhotoTag = Constants.MEDIA_PHOTO.substring(0, redundancyPhotoTagLength);
                String text = spanText.toString();
                if (!TextUtils.isEmpty(text) && text.length() >= end && redundancyPhotoTag.equals(text.substring(start, end))) {
                    PhotoImageSpan.this.mText.delete(start, end);
                }
            }
        }
    }

    public PhotoImageSpan(Context context, SpannableStringBuilder builder, Uri thumbUri, Uri originUri) {
        initData(context, builder, thumbUri, originUri, null);
        Log.i("shenpc", "thumbUri = " + thumbUri);
        Log.i("shenpc", "originUri = " + originUri);
        prepareCacheDrawableAsync();
    }

    public PhotoImageSpan(Context context, SpannableStringBuilder builder, Uri thumbUri, Uri originUri, Bitmap bitmap) {
        initData(context, builder, thumbUri, originUri, bitmap);
        this.mCacheDrawable = new BitmapDrawable(context.getResources(), bitmap);
        this.mCacheDrawable.setBounds(0, 0, this.mImageWidth, this.mImageHeight);
    }

    public void initSpan(int spanStart) {
        setSpanWatcher(spanStart);
    }

    public void setOnImageSpanChangeListener(OnImageSpanChangeListener listener) {
        this.mListener = listener;
    }

    public void updateSpanEditableText(SpannableStringBuilder stringBuilder) {
        if (this.mText != stringBuilder) {
            this.mText = stringBuilder;
        }
    }

    private void initData(Context context, SpannableStringBuilder builder, Uri thumbUri, Uri originUri, Bitmap bitmap) {
        this.mContext = context;
        this.mText = builder;
        this.mThumbUri = thumbUri;
        this.mOriginUri = originUri;
        this.mSpanWatcher = new ImageSpanWatcher();
        initPhotoSize(context, originUri, bitmap);
    }

    private void initPhotoSize(Context context, Uri originUri, Bitmap bitmap) {
        if (bitmap != null) {
            this.mImageWidth = bitmap.getWidth();
            this.mImageHeight = bitmap.getHeight();
            return;
        }
        EditPage page = EditPage.get(context);
        int[] originSize = DecodeUtils.loadBitmapSize(context, originUri);
        if (isInValidSize(originSize)) {
            this.mOrignalPicExit = false;
            int[] thumbSize = DecodeUtils.loadBitmapSize(context, this.mThumbUri);
            if (isInValidSize(thumbSize)) {
                setDefalutPhotoSize(page);
            } else {
                setScalePhotoSize(page, thumbSize, this.mThumbUri);
            }
        } else {
            setScalePhotoSize(page, originSize, originUri);
        }
        this.mImageShiftSize = page.mImageShiftSize;
    }

    private boolean isInValidSize(int[] size) {
        return size == null || size.length == 0 || size[0] == 0;
    }

    private void setDefalutPhotoSize(EditPage page) {
        this.mImageWidth = page.mImageWidth;
        this.mImageHeight = page.mImageHeight;
    }

    private void setScalePhotoSize(EditPage page, int[] size, Uri originUri) {
        this.mImageWidth = page.mImageWidth;
        int rotate = DecodeUtils.decodeImageRotate(originUri);
        float origWidth = (float) size[0];
        float origHeight = (float) size[1];
        if (rotate == 90 || rotate == 270) {
            this.mImageHeight = (int) ((IPhotoView.DEFAULT_MIN_SCALE / (origHeight / ((float) this.mImageWidth))) * origWidth);
        } else {
            this.mImageHeight = (int) ((IPhotoView.DEFAULT_MIN_SCALE / (origWidth / ((float) this.mImageWidth))) * origHeight);
        }
    }

    private void prepareCacheDrawableAsync() {
        if (this.mCacheDrawable == null) {
            final Handler mainHandler = new Handler(NoteAppImpl.getContext().getMainLooper());
            NoteAppImpl.getContext().getThreadPool().submit(new Job<Object>() {
                public Object run(JobContext jc) {
                    Drawable drawable;
                    Bitmap bitmap = PhotoImageSpan.this.decodeThumbBitmapFromCache(NoteAppImpl.getContext(), PhotoImageSpan.this.mThumbUri);
                    if (bitmap != null) {
                        int w = bitmap.getWidth();
                        int h = bitmap.getHeight();
                        if (PhotoImageSpan.this.mOrignalPicExit && !(w == PhotoImageSpan.this.mImageWidth && h == PhotoImageSpan.this.mImageHeight)) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                    if (bitmap == null) {
                        bitmap = PhotoImageSpan.this.decodeThumbBitmapFromOriginFile(NoteAppImpl.getContext(), PhotoImageSpan.this.mImageWidth, PhotoImageSpan.this.mImageHeight, PhotoImageSpan.this.mOriginUri);
                    }
                    if (bitmap != null) {
                        drawable = new BitmapDrawable(NoteAppImpl.getContext().getResources(), bitmap);
                    } else {
                        PhotoImageSpan.this.setDefalutPhotoSize(EditPage.get(NoteAppImpl.getContext()));
                        drawable = EditPage.getDefaultImageDrawable(NoteAppImpl.getContext());
                    }
                    mainHandler.post(new Runnable() {
                        public void run() {
                            PhotoImageSpan.this.mCacheDrawable = drawable;
                            PhotoImageSpan.this.mCacheDrawable.setBounds(0, 0, PhotoImageSpan.this.mImageWidth, PhotoImageSpan.this.mImageHeight);
                            if (PhotoImageSpan.this.mListener != null) {
                                PhotoImageSpan.this.mListener.onImageChanged();
                            }
                        }
                    });
                    return null;
                }
            });
        }
    }

    private Bitmap decodeThumbBitmapFromCache(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        if (!"file".equals(uri.getScheme())) {
            return DecodeUtils.decodeBitmap(context, uri);
        }
        String filePath = uri.getPath();
        if (new File(filePath).exists()) {
            return DecodeUtils.decodeBitmap(filePath);
        }
        return null;
    }

    private Bitmap decodeThumbBitmapFromOriginFile(Context context, int tw, int th, Uri originUri) {
        if (originUri == null) {
            return null;
        }
        if ("file".equals(originUri.getScheme()) && !new File(originUri.getPath()).exists()) {
            return null;
        }
        return DecodeUtils.decodeThumbnail(context, originUri, tw, th, DecodeUtils.decodeImageRotate(originUri), true);
    }

    public static PhotoImageSpan[] get(SpannableStringBuilder text, int start, int end) {
        PhotoImageSpan[] items = (PhotoImageSpan[]) text.getSpans(start, end, PhotoImageSpan.class);
        if (items.length != 1) {
            return items;
        }
        PhotoImageSpan item = items[0];
        if (text.toString().startsWith(Constants.MEDIA_PHOTO, text.getSpanStart(item))) {
            return items;
        }
        item.removePhotoImageSpan();
        return EMPTY_ITEM;
    }

    public void adjustCursorIfInvalid(int currSelection) {
        if (this.mText != null) {
            int start = this.mText.getSpanStart(this);
            if (currSelection == start) {
                Selection.setSelection(this.mText, start + Constants.MEDIA_PHOTO.length());
            }
        }
    }

    private void removePhotoImageSpan() {
        this.mText.removeSpan(this);
    }

    private void setSpanWatcher(int spanStart) {
        this.mText.setSpan(this.mSpanWatcher, spanStart, Constants.MEDIA_PHOTO.length() + spanStart, 33);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        if (fm != null) {
            fm.ascent = -this.mImageHeight;
            fm.descent = 0;
            fm.top = fm.ascent;
            fm.bottom = 0;
        }
        return this.mImageWidth;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int baseLine, int bottom, Paint paint) {
        int lastLinePos = (baseLine - (bottom - top)) + this.mImageShiftSize;
        int transY = lastLinePos + ((((baseLine + this.mImageShiftSize) - lastLinePos) - this.mImageHeight) / 2);
        canvas.translate(x, (float) transY);
        if (this.mCacheDrawable != null) {
            this.mCacheDrawable.draw(canvas);
        }
        canvas.translate(-x, (float) (-transY));
    }

    public void onClick(View view) {
        if (this.mContext != null) {
            Intent intent = new Intent(this.mContext, PhotoViewActivity.class);
            PhotoImageSpan[] spans = (PhotoImageSpan[]) this.mText.getSpans(0, this.mText.length(), PhotoImageSpan.class);
            String[] uriStr = new String[spans.length];
            int currentUri = 0;
            for (int i = 0; i < spans.length; i++) {
                uriStr[i] = spans[i].mOriginUri.toString();
                if (this.mOriginUri == spans[i].mOriginUri) {
                    currentUri = i;
                }
            }
            intent.putExtra("currentImage", currentUri);
            intent.putExtra("imageUris", uriStr);
            this.mContext.startActivity(intent);
        }
    }

    public boolean isClickValid(TextView widget, MotionEvent event, int lineBottom) {
        int paddingLeft = widget.getTotalPaddingLeft();
        int clickX = (int) event.getX();
        return clickX >= paddingLeft + 1 && clickX <= (this.mImageWidth + paddingLeft) - 1 && ((int) event.getY()) < lineBottom;
    }

    public void writeToJson(JSONObject jsonObject) throws JSONException {
        int start = this.mText.getSpanStart(this);
        int end = this.mText.getSpanEnd(this);
        int flags = this.mText.getSpanFlags(this);
        jsonObject.put(DataConvert.SPAN_ITEM_START, start);
        jsonObject.put(DataConvert.SPAN_ITEM_END, end);
        jsonObject.put(DataConvert.SPAN_ITEM_FLAG, flags);
        jsonObject.put(DataConvert.SPAN_ITEM_TYPE, PhotoImageSpan.class.getName());
        jsonObject.put(ORIGIN_URI, this.mOriginUri.toString());
        jsonObject.put(THUMB_URI, this.mThumbUri.toString());
    }

    public void recycle() {
        if (!(this.mCacheDrawable == null || !(this.mCacheDrawable instanceof BitmapDrawable) || this.mCacheDrawable == EditPage.getDefaultImageDrawable(NoteAppImpl.getContext()))) {
            ((BitmapDrawable) this.mCacheDrawable).getBitmap().recycle();
        }
        if (this.mSpanWatcher != null) {
            this.mText.removeSpan(this.mSpanWatcher);
            this.mSpanWatcher = null;
        }
        this.mCacheDrawable = null;
        this.mListener = null;
        this.mText = null;
        this.mContext = null;
    }
}
