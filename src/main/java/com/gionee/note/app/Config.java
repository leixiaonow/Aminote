package com.gionee.note.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import com.gionee.aminote.R;
import com.gionee.note.common.NoteUtils;

public final class Config {

    public static class EditPage {
        private static Drawable sDefaultImageDrawable;
        private static EditPage sInstance;
        public int mBillForegroundColor;
        public int mBillWidth;
        public int mImageHeight;
        public int mImageShiftSize;
        public int mImageWidth;
        public int mReminderColor;
        public int mReminderGap;
        public int mReminderSize;
        public int mSignatureColor;
        public int mSignatureSize;
        public int mSoundDurationColor;
        public int mSoundDurationOffsetLeft;
        public int mSoundDurationSize;
        public int mSoundHeight;
        public int mSoundPointColor;
        public int mSoundPointOffsetLeft;
        public int mSoundPointOffsetRight;
        public int mSoundPointRadius;
        public int mSoundWidth = this.mImageWidth;
        public int mTimeColor;
        public int mTimeSize;

        public EditPage(Context context) {
            Resources rs = context.getResources();
            int screenWidth = NoteUtils.sScreenWidth;
            int editMargin = rs.getDimensionPixelSize(R.dimen.edit_note_content_padding_left);
            int cursorWidth = rs.getDimensionPixelSize(R.dimen.edit_note_item_cursor_width);
            this.mImageHeight = rs.getDimensionPixelSize(R.dimen.edit_note_item_image_height);
            this.mImageWidth = (screenWidth - (editMargin * 2)) - cursorWidth;
            this.mImageShiftSize = rs.getDimensionPixelSize(R.dimen.edit_note_content_line_padding_bottom);
            this.mSoundHeight = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_height);
            this.mSoundPointRadius = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_point_radius);
            this.mSoundPointOffsetLeft = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_point_offset_left);
            this.mSoundPointOffsetRight = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_point_offset_right);
            this.mSoundPointColor = ContextCompat.getColor(context, R.color.edit_note_item_sound_point_color);
            this.mSoundDurationOffsetLeft = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_duration_offset_left);
            this.mSoundDurationSize = rs.getDimensionPixelSize(R.dimen.edit_note_item_sound_duration_size);
            this.mSoundDurationColor = ContextCompat.getColor(context, R.color.edit_note_item_sound_duration_color);
            this.mBillWidth = rs.getDimensionPixelSize(R.dimen.edit_note_item_bill_width);
            this.mBillForegroundColor = ContextCompat.getColor(context, R.color.edit_note_bill_foreground_color);
            this.mTimeSize = rs.getDimensionPixelSize(R.dimen.edit_note_item_time_size);
            this.mTimeColor = ContextCompat.getColor(context, R.color.edit_note_item_time_color);
            this.mReminderSize = rs.getDimensionPixelSize(R.dimen.edit_note_item_reminder_size);
            this.mReminderColor = ContextCompat.getColor(context, R.color.edit_note_item_reminder_color);
            this.mReminderGap = rs.getDimensionPixelSize(R.dimen.edit_note_item_reminder_gap);
            this.mSignatureSize = rs.getDimensionPixelSize(R.dimen.edit_note_item_signature_size);
            this.mSignatureColor = ContextCompat.getColor(context, R.color.edit_note_item_signature_color);
        }

        public static synchronized EditPage get(Context context) {
            EditPage editPage;
            synchronized (EditPage.class) {
                if (sInstance == null) {
                    sInstance = new EditPage(context);
                }
                editPage = sInstance;
            }
            return editPage;
        }

        public static synchronized Drawable getDefaultImageDrawable(Context context) {
            Drawable drawable;
            synchronized (EditPage.class) {
                if (sDefaultImageDrawable == null) {
                    sDefaultImageDrawable = ContextCompat.getDrawable(context, R.drawable.image_span_default_drawable);
                }
                drawable = sDefaultImageDrawable;
            }
            return drawable;
        }
    }

    public static class NoteCard {
        private static Drawable sDefaultNoteCardImage;
        private static NoteCard sInstance;
        public int mImageHeight;
        public int mImageWidth = this.mItemWidth;
        public int mItemWidth;

        public NoteCard(Context context) {
            Resources rs = context.getResources();
            this.mItemWidth = ((NoteUtils.sScreenWidth - (rs.getDimensionPixelSize(R.dimen.home_activity_horizontal_margin) * 2)) - ((rs.getInteger(R.integer.home_note_item_column) - 1) * rs.getDimensionPixelSize(R.dimen.home_note_item_gap))) / 2;
            this.mImageHeight = rs.getDimensionPixelSize(R.dimen.home_note_item_image_height);
        }

        public static synchronized NoteCard get(Context context) {
            NoteCard noteCard;
            synchronized (NoteCard.class) {
                if (sInstance == null) {
                    sInstance = new NoteCard(context);
                }
                noteCard = sInstance;
            }
            return noteCard;
        }

        public static synchronized Drawable getDefaultNoteCardImage(Context context) {
            Drawable drawable;
            synchronized (NoteCard.class) {
                if (sDefaultNoteCardImage == null) {
                    sDefaultNoteCardImage = ContextCompat.getDrawable(context, R.drawable.note_card_default_image);
                }
                drawable = sDefaultNoteCardImage;
            }
            return drawable;
        }
    }

    public static class SoundImageSpanConfig {
        private static SoundImageSpanConfig sInstance;
        public int mDotCircleRadius;
        public int mDotCircleX;
        public int mDotCircleY;
        public int mDotColor;
        public int mImageShiftSize;
        public int mTextColor;
        public int mTextLeftMargin;
        public float mTextSize;

        public SoundImageSpanConfig(Context context) {
            this.mImageShiftSize = context.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_bottom);
            this.mDotCircleX = context.getResources().getDimensionPixelSize(R.dimen.red_dot_circle_x);
            this.mDotCircleY = context.getResources().getDimensionPixelSize(R.dimen.red_dot_circle_y);
            this.mDotCircleRadius = context.getResources().getDimensionPixelSize(R.dimen.red_dot_circle_radius);
            this.mTextColor = ContextCompat.getColor(context, R.color.sound_record_time_textcolor);
            this.mDotColor = ContextCompat.getColor(context, R.color.sound_dot_color);
            this.mTextSize = (float) context.getResources().getDimensionPixelSize(R.dimen.record_time_text_size);
            this.mTextLeftMargin = context.getResources().getDimensionPixelSize(R.dimen.record_text_left_margin);
        }

        public static synchronized SoundImageSpanConfig get(Context context) {
            SoundImageSpanConfig soundImageSpanConfig;
            synchronized (SoundImageSpanConfig.class) {
                if (sInstance == null) {
                    sInstance = new SoundImageSpanConfig(context);
                }
                soundImageSpanConfig = sInstance;
            }
            return soundImageSpanConfig;
        }
    }

    public static class WidgetPage {
        private static WidgetPage sWidgetPage;
        public int mHeight;
        public int mWidth;

        private WidgetPage(Context context) {
            this.mWidth = context.getResources().getDimensionPixelOffset(R.dimen.widget_width_4x);
            this.mHeight = context.getResources().getDimensionPixelOffset(R.dimen.widget_photo_height_4x);
        }

        public static WidgetPage getInstance(Context context) {
            if (sWidgetPage == null) {
                sWidgetPage = new WidgetPage(context);
            }
            return sWidgetPage;
        }
    }
}
