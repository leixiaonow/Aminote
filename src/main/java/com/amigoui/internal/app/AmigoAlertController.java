package com.amigoui.internal.app;

import com.gionee.aminote.R;
import amigoui.changecolors.ChameleonColorManager;
import amigoui.widget.AmigoButton;
import amigoui.widget.AmigoListView;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import uk.co.senab.photoview.IPhotoView;

public class AmigoAlertController {
    private static boolean mIsGnWidget3Style = false;
    private ListAdapter mAdapter;
    private int mAlertDialogLayout;
    private int mAlertDialogMaxHeight;
    private ImageButton mButtonCancel = null;
    OnClickListener mButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            Message m = null;
            if (v == AmigoAlertController.this.mButtonPositive && AmigoAlertController.this.mButtonPositiveMessage != null) {
                m = Message.obtain(AmigoAlertController.this.mButtonPositiveMessage);
            } else if (v == AmigoAlertController.this.mButtonNegative && AmigoAlertController.this.mButtonNegativeMessage != null) {
                m = Message.obtain(AmigoAlertController.this.mButtonNegativeMessage);
            } else if (v == AmigoAlertController.this.mButtonNeutral && AmigoAlertController.this.mButtonNeutralMessage != null) {
                m = Message.obtain(AmigoAlertController.this.mButtonNeutralMessage);
            } else if (v == AmigoAlertController.this.mButtonCancel && AmigoAlertController.this.mDialogInterface != null) {
                AmigoAlertController.this.mDialogInterface.cancel();
            }
            if (m != null) {
                m.sendToTarget();
            }
            AmigoAlertController.this.mHandler.sendMessageDelayed(AmigoAlertController.this.mHandler.obtainMessage(1, AmigoAlertController.this.mDialogInterface), 50);
        }
    };
    private AmigoButton mButtonNegative;
    private Message mButtonNegativeMessage;
    private CharSequence mButtonNegativeText;
    private AmigoButton mButtonNeutral;
    private Message mButtonNeutralMessage;
    private int mButtonNeutralStyle;
    private CharSequence mButtonNeutralText;
    private AmigoButton mButtonPositive;
    private Message mButtonPositiveMessage;
    private int mButtonPositiveStyle;
    private CharSequence mButtonPositiveText;
    private int mCheckedItem = -1;
    private final Context mContext;
    private int mContextMenuDialogMaxHeight;
    private View mCustomTitleView;
    private final DialogInterface mDialogInterface;
    private boolean mForceInverseBackground;
    private Handler mHandler;
    private boolean mHasCancelIconButton = true;
    private Drawable mIcon;
    private int mIconId = 0;
    private ImageView mIconView;
    private boolean mIsStrongHint;
    private int mListItemLayout;
    private int mListLayout;
    private ListView mListView;
    private CharSequence mMessage;
    private TextView mMessageView;
    private int mMultiChoiceItemLayout;
    private int mPositiveColor = 0;
    private ScrollView mScrollView;
    private int mSingleChoiceItemLayout;
    private int mStrongHintLayout;
    private CharSequence mTitle;
    private TextView mTitleView;
    private View mView;
    private int mViewSpacingBottom;
    private int mViewSpacingLeft;
    private int mViewSpacingRight;
    private boolean mViewSpacingSpecified = false;
    private int mViewSpacingTop;
    private final Window mWindow;

    public static class AlertParams {
        public ListAdapter mAdapter;
        public Drawable mCancelIcon = null;
        public boolean mCancelable;
        public int mCheckedItem = -1;
        public boolean[] mCheckedItems;
        public final Context mContext;
        public Cursor mCursor;
        public View mCustomTitleView;
        public boolean mForceInverseBackground;
        public boolean mHasCancelIcon = true;
        public Drawable mIcon;
        public int mIconAttrId = 0;
        public int mIconId = 0;
        public final LayoutInflater mInflater;
        public String mIsCheckedColumn;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public CharSequence[] mItems;
        public String mLabelColumn;
        public CharSequence mMessage;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNegativeButtonText;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public int mNeutralButtonStyle;
        public CharSequence mNeutralButtonText;
        public OnCancelListener mOnCancelListener;
        public OnMultiChoiceClickListener mOnCheckboxClickListener;
        public DialogInterface.OnClickListener mOnClickListener;
        public OnDismissListener mOnDismissListener;
        public OnItemSelectedListener mOnItemSelectedListener;
        public OnKeyListener mOnKeyListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public int mPositiveButtonStyle;
        public CharSequence mPositiveButtonText;
        public boolean mRecycleOnMeasure = true;
        public CharSequence mTitle;
        public View mView;
        public int mViewSpacingBottom;
        public int mViewSpacingLeft;
        public int mViewSpacingRight;
        public boolean mViewSpacingSpecified = false;
        public int mViewSpacingTop;

        public interface OnPrepareListViewListener {
            void onPrepareListView(ListView listView);
        }

        public AlertParams(Context context) {
            this.mContext = context;
            this.mCancelable = true;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public void apply(AmigoAlertController dialog) {
            if (this.mCustomTitleView != null) {
                dialog.setCustomTitle(this.mCustomTitleView);
            } else {
                if (this.mTitle != null) {
                    dialog.setTitle(this.mTitle);
                }
                if (this.mIcon != null) {
                    dialog.setIcon(this.mIcon);
                }
                if (this.mIconId >= 0) {
                    dialog.setIcon(this.mIconId);
                }
                if (this.mIconAttrId > 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(this.mIconAttrId));
                }
            }
            if (this.mMessage != null) {
                dialog.setMessage(this.mMessage);
            }
            if (this.mPositiveButtonText != null) {
                dialog.setButton(-1, this.mPositiveButtonText, this.mPositiveButtonListener, null);
            }
            if (this.mNegativeButtonText != null) {
                dialog.setButton(-2, this.mNegativeButtonText, this.mNegativeButtonListener, null);
            }
            if (this.mNeutralButtonText != null) {
                dialog.setButton(-3, this.mNeutralButtonText, this.mNeutralButtonListener, null);
            }
            if (this.mForceInverseBackground) {
                dialog.setInverseBackgroundForced(true);
            }
            if (!(this.mItems == null && this.mCursor == null && this.mAdapter == null)) {
                createListView(dialog);
            }
            if (this.mView != null) {
                if (this.mViewSpacingSpecified) {
                    dialog.setView(this.mView, this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
                } else {
                    dialog.setView(this.mView);
                }
            }
            if (this.mHasCancelIcon) {
                if (!AmigoAlertController.mIsGnWidget3Style || (this.mPositiveButtonText == null && this.mNegativeButtonText == null && this.mNeutralButtonText == null)) {
                    dialog.setHasCancelIcon(true);
                } else {
                    dialog.setHasCancelIcon(false);
                }
                if (this.mCancelIcon != null) {
                    dialog.setCancelIcon(this.mCancelIcon);
                }
            }
            if (AmigoAlertController.mIsGnWidget3Style) {
                dialog.setButtonStyle(-1, this.mPositiveButtonStyle);
                dialog.setButtonStyle(-3, this.mNeutralButtonStyle);
            }
        }

        private void createListView(final AmigoAlertController dialog) {
            ListAdapter adapter;
            final RecycleListView listView = (RecycleListView) this.mInflater.inflate(dialog.mListLayout, null);
            listView.setVerticalFadingEdgeEnabled(true);
            int textViewId = AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_text1");
            ListAdapter simpleCursorAdapter;
            if (!this.mIsMultiChoice) {
                int layout = this.mIsSingleChoice ? dialog.mSingleChoiceItemLayout : dialog.mListItemLayout;
                if (this.mCursor == null) {
                    adapter = this.mAdapter != null ? this.mAdapter : new ArrayAdapter(this.mContext, layout, textViewId, this.mItems);
                } else {
                    simpleCursorAdapter = new SimpleCursorAdapter(this.mContext, layout, this.mCursor, new String[]{this.mLabelColumn}, new int[]{textViewId});
                }
            } else if (this.mCursor == null) {
                adapter = new ArrayAdapter<CharSequence>(this.mContext, dialog.mMultiChoiceItemLayout, textViewId, this.mItems) {
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        if (AlertParams.this.mCheckedItems != null && AlertParams.this.mCheckedItems[position]) {
                            listView.setItemChecked(position, true);
                        }
                        return view;
                    }
                };
            } else {
                final RecycleListView recycleListView = listView;
                final AmigoAlertController amigoAlertController = dialog;
                simpleCursorAdapter = new CursorAdapter(this.mContext, this.mCursor, false) {
                    private final int mIsCheckedIndex;
                    private final int mLabelIndex;

                    public void bindView(View view, Context context, Cursor cursor) {
                        boolean z = true;
                        ((CheckedTextView) view.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_text1"))).setText(cursor.getString(this.mLabelIndex));
                        RecycleListView recycleListView = recycleListView;
                        int position = cursor.getPosition();
                        if (cursor.getInt(this.mIsCheckedIndex) != 1) {
                            z = false;
                        }
                        recycleListView.setItemChecked(position, z);
                    }

                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                        return AlertParams.this.mInflater.inflate(amigoAlertController.mMultiChoiceItemLayout, parent, false);
                    }
                };
            }
            if (this.mOnPrepareListViewListener != null) {
                this.mOnPrepareListViewListener.onPrepareListView(listView);
            }
            dialog.mAdapter = adapter;
            dialog.mCheckedItem = this.mCheckedItem;
            if (this.mOnClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        AlertParams.this.mOnClickListener.onClick(dialog.mDialogInterface, position);
                        if (!AlertParams.this.mIsSingleChoice) {
                            dialog.mDialogInterface.dismiss();
                        }
                    }
                });
            } else if (this.mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        if (AlertParams.this.mCheckedItems != null) {
                            AlertParams.this.mCheckedItems[position] = listView.isItemChecked(position);
                        }
                        AlertParams.this.mOnCheckboxClickListener.onClick(dialog.mDialogInterface, position, listView.isItemChecked(position));
                    }
                });
            }
            if (this.mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(this.mOnItemSelectedListener);
            }
            if (this.mIsSingleChoice) {
                listView.setChoiceMode(1);
            } else if (this.mIsMultiChoice) {
                listView.setChoiceMode(2);
            }
            listView.mRecycleOnMeasure = this.mRecycleOnMeasure;
            dialog.mListView = listView;
        }
    }

    private static final class ButtonHandler extends Handler {
        private static final int MSG_DISMISS_DIALOG = 1;
        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            this.mDialog = new WeakReference(dialog);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -3:
                case -2:
                case -1:
                    ((DialogInterface.OnClickListener) msg.obj).onClick((DialogInterface) this.mDialog.get(), msg.what);
                    return;
                case 1:
                    ((DialogInterface) msg.obj).dismiss();
                    return;
                default:
                    return;
            }
        }
    }

    public static class RecycleListView extends AmigoListView {
        boolean mRecycleOnMeasure = true;

        public RecycleListView(Context context) {
            super(context);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RecycleListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }
    }

    private static boolean shouldCenterSingleButton(Context context) {
        return false;
    }

    public AmigoAlertController(Context context, DialogInterface di, Window window) {
        this.mContext = context;
        this.mDialogInterface = di;
        this.mWindow = window;
        this.mHandler = new ButtonHandler(di);
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.AmigoAlertDialog, AmigoWidgetResource.getIdentifierByAttr(context, "amigoalertDialogStyle"), 0);
        this.mAlertDialogLayout = a.getResourceId(R.styleable.AmigoAlertDialog_amigolayout, AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_alert_dialog_light"));
        this.mListLayout = a.getResourceId(R.styleable.AmigoAlertDialog_amigolistLayout, AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_select_dialog"));
        this.mMultiChoiceItemLayout = a.getResourceId(R.styleable.AmigoAlertDialog_amigomultiChoiceItemLayout, AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_select_dialog_multichoice"));
        this.mSingleChoiceItemLayout = a.getResourceId(R.styleable.AmigoAlertDialog_amigosingleChoiceItemLayout, AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_select_dialog_singlechoice"));
        this.mListItemLayout = a.getResourceId(R.styleable.AmigoAlertDialog_amigolistItemLayout, AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_select_dialog_item"));
        this.mStrongHintLayout = a.getResourceId(R.styleable.AmigoAlertDialog_amigoStrongHintDialogLayout, AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_strong_hint_dialog"));
        this.mAlertDialogMaxHeight = (int) this.mContext.getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_alert_dialog_list_maxheight"));
        this.mContextMenuDialogMaxHeight = (int) this.mContext.getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_context_menu_list_maxheight"));
        this.mPositiveColor = a.getResourceId(R.styleable.AmigoAlertDialog_amigoDialogPostiveBtnColor, 17170444);
        a.recycle();
    }

    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            if (canTextInput(vg.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    public void setStrongHint(boolean strongHint) {
        this.mIsStrongHint = strongHint;
    }

    public void installContent() {
        this.mWindow.requestFeature(1);
        if (this.mView == null || !canTextInput(this.mView)) {
            this.mWindow.setFlags(131072, 131072);
        }
        if (this.mIsStrongHint) {
            this.mWindow.setContentView(this.mStrongHintLayout);
        } else {
            this.mWindow.setContentView(this.mAlertDialogLayout);
        }
        if (this.mContext.getThemeResId() != AmigoWidgetResource.getIdentifierByStyle(this.mContext, "Theme.Amigo.Dialog.Alert.FullScreen")) {
            this.mWindow.getAttributes().width = -1;
        }
        setupView();
        if (this.mIsStrongHint) {
            strongHintDialogWindow();
        }
    }

    private void strongHintDialogWindow() {
        LayoutParams params = this.mWindow.getAttributes();
        params.width = -1;
        params.height = -1;
        params.alpha = 0.8f;
        this.mWindow.setAttributes(params);
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
        }
    }

    public void setCustomTitle(View customTitleView) {
        this.mCustomTitleView = customTitleView;
    }

    public void setMessage(CharSequence message) {
        this.mMessage = message;
        if (this.mMessageView != null) {
            this.mMessageView.setText(message);
        }
    }

    public void setView(View view) {
        this.mView = view;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        this.mView = view;
        this.mViewSpacingSpecified = true;
        this.mViewSpacingLeft = viewSpacingLeft;
        this.mViewSpacingTop = viewSpacingTop;
        this.mViewSpacingRight = viewSpacingRight;
        this.mViewSpacingBottom = viewSpacingBottom;
    }

    public void setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener, Message msg) {
        if (mIsGnWidget3Style) {
            setHasCancelIcon(false);
        }
        if (msg == null && listener != null) {
            msg = this.mHandler.obtainMessage(whichButton, listener);
        }
        switch (whichButton) {
            case -3:
                this.mButtonNeutralText = text;
                this.mButtonNeutralMessage = msg;
                return;
            case -2:
                this.mButtonNegativeText = text;
                this.mButtonNegativeMessage = msg;
                return;
            case -1:
                this.mButtonPositiveText = text;
                this.mButtonPositiveMessage = msg;
                return;
            default:
                throw new IllegalArgumentException("AmigoButton does not exist");
        }
    }

    public void setIcon(int resId) {
        this.mIcon = null;
        this.mIconId = resId;
        if (this.mIconView == null) {
            return;
        }
        if (resId != 0) {
            this.mIconView.setImageResource(this.mIconId);
        } else {
            this.mIconView.setVisibility(8);
        }
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        this.mIconId = 0;
        if (this.mIconView == null) {
            return;
        }
        if (this.mIcon != null) {
            this.mIconView.setImageDrawable(icon);
        } else {
            this.mIconView.setVisibility(8);
        }
    }

    public int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        this.mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        this.mForceInverseBackground = forceInverseBackground;
    }

    public ListView getListView() {
        return this.mListView;
    }

    public AmigoButton getButton(int whichButton) {
        switch (whichButton) {
            case -3:
                return this.mButtonNeutral;
            case -2:
                return this.mButtonNegative;
            case -1:
                return this.mButtonPositive;
            default:
                return null;
        }
    }

    private boolean holdByStrongHint(int keyCode) {
        return this.mIsStrongHint;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return (this.mScrollView != null && this.mScrollView.executeKeyEvent(event)) || holdByStrongHint(keyCode);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return (this.mScrollView != null && this.mScrollView.executeKeyEvent(event)) || holdByStrongHint(keyCode);
    }

    private void setupView() {
        LinearLayout contentPanel = (LinearLayout) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_contentPanel"));
        setupContent(contentPanel);
        boolean hasButtons = setupButtons();
        LinearLayout topPanel = (LinearLayout) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_topPanel"));
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.AmigoAlertDialog, AmigoWidgetResource.getIdentifierByAttr(this.mContext, "amigoalertDialogStyle"), 0);
        boolean hasTitle = setupTitle(topPanel);
        View buttonPanel = this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_buttonPanel"));
        if (!hasButtons) {
            buttonPanel.setVisibility(8);
        }
        FrameLayout customPanel = null;
        if (this.mView != null) {
            customPanel = (FrameLayout) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_customPanel"));
            FrameLayout custom = (FrameLayout) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_custom"));
            custom.addView(this.mView, new ViewGroup.LayoutParams(-1, -1));
            if (this.mViewSpacingSpecified) {
                custom.setPadding(this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
            }
            if (this.mListView != null) {
                ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0.0f;
            }
        } else {
            this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_customPanel")).setVisibility(8);
        }
        if (hasTitle) {
            View divider;
            if (this.mMessage == null && this.mView == null && this.mListView == null) {
                divider = this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_titleDividerTop"));
            } else {
                divider = this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_titleDivider"));
            }
            this.mButtonCancel = (ImageButton) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_cancel"));
        }
        if (!this.mIsStrongHint) {
            setBackground(topPanel, contentPanel, customPanel, hasButtons, a, hasTitle, buttonPanel);
        }
        a.recycle();
        changeColor(topPanel, contentPanel, customPanel, buttonPanel);
    }

    public void changeColor(LinearLayout topPanel, LinearLayout contentPanel, FrameLayout customPanel, View buttonPanel) {
        if (ChameleonColorManager.isNeedChangeColor()) {
            if (this.mCustomTitleView == null && !TextUtils.isEmpty(this.mTitle)) {
                this.mTitleView.setTextColor(ChameleonColorManager.getAccentColor_G1());
            }
            if (!(this.mScrollView == null || this.mMessageView == null)) {
                this.mMessageView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            }
            topPanel.setBackgroundColor(ChameleonColorManager.getPopupBackgroudColor_B2());
            contentPanel.setBackgroundColor(ChameleonColorManager.getPopupBackgroudColor_B2());
            buttonPanel.setBackgroundColor(ChameleonColorManager.getPopupBackgroudColor_B2());
            if (customPanel != null) {
                customPanel.setBackgroundColor(ChameleonColorManager.getPopupBackgroudColor_B2());
            }
            if (!TextUtils.isEmpty(this.mButtonPositiveText)) {
                this.mButtonPositive.setTextColor(ChameleonColorManager.getAccentColor_G1());
            }
        }
    }

    private boolean setupTitle(LinearLayout topPanel) {
        boolean hasTextTitle = false;
        if (this.mCustomTitleView != null) {
            topPanel.addView(this.mCustomTitleView, 0, new LinearLayout.LayoutParams(-1, -2));
            this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_title_template")).setVisibility(8);
            return true;
        }
        if (!TextUtils.isEmpty(this.mTitle)) {
            hasTextTitle = true;
        }
        this.mIconView = (ImageView) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_icon"));
        if (hasTextTitle) {
            this.mTitleView = (TextView) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_alertTitle"));
            this.mTitleView.setText(this.mTitle);
            if (this.mIconId != 0) {
                this.mIconView.setImageResource(this.mIconId);
                return true;
            } else if (this.mIcon != null) {
                this.mIconView.setImageDrawable(this.mIcon);
                return true;
            } else {
                this.mIconView.setVisibility(8);
                return true;
            }
        }
        this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_title_template")).setVisibility(8);
        this.mIconView.setVisibility(8);
        topPanel.setVisibility(8);
        return false;
    }

    private void setupContent(LinearLayout contentPanel) {
        this.mScrollView = (ScrollView) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_scrollView"));
        if (this.mScrollView != null) {
            this.mScrollView.setFocusable(false);
            this.mMessageView = (TextView) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_message"));
            if (this.mMessageView == null) {
                return;
            }
            if (this.mMessage != null) {
                this.mMessageView.setText(this.mMessage);
                return;
            }
            this.mMessageView.setVisibility(8);
            this.mScrollView.removeView(this.mMessageView);
            if (this.mListView != null) {
                contentPanel.removeView(this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_scrollView")));
                contentPanel.addView(this.mListView, new LinearLayout.LayoutParams(-1, -1));
                if (this.mAdapter.getCount() > 5) {
                    int listHeight;
                    if (this.mListView.getChoiceMode() == 0) {
                        listHeight = this.mContextMenuDialogMaxHeight;
                    } else {
                        listHeight = this.mAlertDialogMaxHeight;
                    }
                    contentPanel.setLayoutParams(new LinearLayout.LayoutParams(-1, listHeight, IPhotoView.DEFAULT_MIN_SCALE));
                    return;
                }
                contentPanel.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, IPhotoView.DEFAULT_MIN_SCALE));
                return;
            }
            contentPanel.setVisibility(8);
        }
    }

    private boolean setupButtons() {
        int whichButtons = 0;
        this.mButtonPositive = (AmigoButton) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_button1"));
        this.mButtonPositive.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonPositiveText)) {
            this.mButtonPositive.setVisibility(8);
        } else {
            this.mButtonPositive.setTextColor(this.mContext.getResources().getColor(this.mPositiveColor));
            this.mButtonPositive.setText(this.mButtonPositiveText);
            this.mButtonPositive.setVisibility(0);
            whichButtons = 0 | 1;
        }
        this.mButtonNegative = (AmigoButton) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_button2"));
        this.mButtonNegative.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonNegativeText)) {
            this.mButtonNegative.setVisibility(8);
        } else {
            this.mButtonNegative.setText(this.mButtonNegativeText);
            this.mButtonNegative.setVisibility(0);
            whichButtons |= 2;
        }
        this.mButtonNeutral = (AmigoButton) this.mWindow.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_button3"));
        this.mButtonNeutral.setOnClickListener(this.mButtonHandler);
        if (TextUtils.isEmpty(this.mButtonNeutralText)) {
            this.mButtonNeutral.setVisibility(8);
        } else {
            this.mButtonNeutral.setText(this.mButtonNeutralText);
            this.mButtonNeutral.setVisibility(0);
            whichButtons |= 4;
        }
        if (mIsGnWidget3Style) {
            setupButtonStyle();
        }
        if (shouldCenterSingleButton(this.mContext)) {
            if (whichButtons == 1) {
                centerButton(this.mButtonPositive);
            } else if (whichButtons == 2) {
                centerButton(this.mButtonNeutral);
            } else if (whichButtons == 4) {
                centerButton(this.mButtonNeutral);
            }
        }
        if (whichButtons != 0) {
            return true;
        }
        return false;
    }

    private void centerButton(AmigoButton button) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.gravity = 1;
        params.weight = 0.5f;
        button.setLayoutParams(params);
    }

    private void setBackground(LinearLayout topPanel, LinearLayout contentPanel, View customPanel, boolean hasButtons, TypedArray a, boolean hasTitle, View buttonPanel) {
        int fullDark = a.getResourceId(R.styleable.AmigoAlertDialog_amigofullDark, 17170443);
        int topDark = a.getResourceId(R.styleable.AmigoAlertDialog_amigotopDark, 17170443);
        int centerDark = a.getResourceId(R.styleable.AmigoAlertDialog_amigocenterDark, 17170443);
        int bottomDark = a.getResourceId(R.styleable.AmigoAlertDialog_amigobottomDark, 17170443);
        int fullBright = a.getResourceId(R.styleable.AmigoAlertDialog_amigofullBright, 17170443);
        int topBright = a.getResourceId(R.styleable.AmigoAlertDialog_amigotopBright, 17170443);
        int centerBright = a.getResourceId(R.styleable.AmigoAlertDialog_amigocenterBright, 17170443);
        int bottomBright = a.getResourceId(R.styleable.AmigoAlertDialog_amigobottomBright, 17170443);
        int bottomMedium = a.getResourceId(R.styleable.AmigoAlertDialog_amigobottomMedium, 17170443);
        View[] views = new View[4];
        boolean[] light = new boolean[4];
        View lastView = null;
        boolean lastLight = false;
        int pos = 0;
        if (hasTitle) {
            views[0] = topPanel;
            light[0] = false;
            pos = 0 + 1;
        }
        if (contentPanel.getVisibility() == 8) {
            contentPanel = null;
        }
        views[pos] = contentPanel;
        light[pos] = this.mListView != null;
        pos++;
        if (customPanel != null) {
            views[pos] = customPanel;
            light[pos] = this.mForceInverseBackground;
            pos++;
        }
        if (hasButtons) {
            views[pos] = buttonPanel;
            light[pos] = true;
        }
        boolean setView = false;
        for (pos = 0; pos < views.length; pos++) {
            View v = views[pos];
            if (v != null) {
                if (lastView != null) {
                    if (setView) {
                        lastView.setBackgroundResource(lastLight ? centerBright : centerDark);
                    } else {
                        lastView.setBackgroundResource(lastLight ? topBright : topDark);
                    }
                    setView = true;
                }
                lastView = v;
                lastLight = light[pos];
            }
        }
        if (lastView != null) {
            if (setView) {
                if (!lastLight) {
                    bottomMedium = bottomDark;
                } else if (!hasButtons) {
                    bottomMedium = bottomBright;
                }
                lastView.setBackgroundResource(bottomMedium);
            } else {
                if (!lastLight) {
                    fullBright = fullDark;
                }
                lastView.setBackgroundResource(fullBright);
            }
        }
        if (this.mListView != null && this.mAdapter != null) {
            this.mListView.setAdapter(this.mAdapter);
            if (this.mCheckedItem > -1) {
                this.mListView.setItemChecked(this.mCheckedItem, true);
                this.mListView.setSelection(this.mCheckedItem);
            }
        }
    }

    public void setHasCancelIcon(boolean hasCancelIcon) {
        this.mHasCancelIconButton = hasCancelIcon;
    }

    public void setCancelIcon(Drawable cancelIcon) {
        if (this.mButtonCancel != null) {
            this.mButtonCancel.setImageDrawable(cancelIcon);
        }
    }

    private void setupButtonStyle() {
        if (this.mButtonPositiveStyle == 1) {
            this.mButtonPositive.setTextColor(this.mContext.getResources().getColorStateList(AmigoWidgetResource.getIdentifierByColor(this.mContext, "amigo_alert_dialog_text_light")));
        }
        if (this.mButtonNeutralStyle == 1) {
            this.mButtonNeutral.setTextColor(this.mContext.getResources().getColorStateList(AmigoWidgetResource.getIdentifierByColor(this.mContext, "amigo_alert_dialog_text_light")));
        }
    }

    public void setButtonStyle(int whichButton, int buttonStyle) {
        if (whichButton == -1) {
            this.mButtonPositiveStyle = buttonStyle;
        } else if (whichButton == -3) {
            this.mButtonNeutralStyle = buttonStyle;
        }
    }

    public void setGnWidget3Style(boolean isGnWidget3Style) {
        mIsGnWidget3Style = isGnWidget3Style;
    }
}
