package amigoui.widget;

import com.gionee.aminote.R;
import amigoui.changecolors.ChameleonColorManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class AmigoEditModeView extends RelativeLayout {
    private int mBackgroundColor;
    private EditModeClickListener mClickListener;
    private Context mCxt;
    private AmigoButton mLeftBtn;
    private String mLeftBtnTxt;
    private AmigoButton mRightBtn;
    private String mRightBtnTxt;
    private ColorStateList mTxtColor;

    public interface EditModeClickListener {
        void leftBtnClick();

        void rightBtnClick();
    }

    public AmigoEditModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCxt = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoEditModeView);
        this.mLeftBtnTxt = a.getString(R.styleable.AmigoEditModeView_amigoEditModeLeftBtnTxt);
        this.mRightBtnTxt = a.getString(R.styleable.AmigoEditModeView_amigoEditModeRightBtnTxt);
        this.mTxtColor = getResources().getColorStateList(a.getColor(R.styleable.AmigoEditModeView_amigoEditModeBtnTxtColor, AmigoWidgetResource.getIdentifierByColor(this.mCxt, "white")));
        TypedArray bar = context.obtainStyledAttributes(null, R.styleable.AmigoActionBar, AmigoWidgetResource.getIdentifierByAttr(this.mCxt, "amigoactionBarStyle"), 0);
        this.mBackgroundColor = a.getColor(R.styleable.AmigoEditModeView_amigoEditModeBackground, bar.getColor(R.styleable.AmigoActionBar_amigobackground, AmigoWidgetResource.getIdentifierByColor(this.mCxt, "amigo_actionbar_background_color_light_normal")));
        if (ChameleonColorManager.isNeedChangeColor()) {
            this.mBackgroundColor = ChameleonColorManager.getAppbarColor_A1();
            this.mTxtColor = ColorStateList.valueOf(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
        }
        bar.recycle();
        a.recycle();
        initViews();
        initClickListener();
    }

    private void initViews() {
        addLeftButton();
        addRightButton();
        setBackgroundColor(this.mBackgroundColor);
    }

    private void addRightButton() {
        this.mRightBtn = (AmigoButton) ((LayoutInflater) this.mCxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(AmigoWidgetResource.getIdentifierByLayout(this.mCxt, "amigo_edit_mode_btn"), this, false);
        if (TextUtils.isEmpty(this.mRightBtnTxt)) {
            this.mRightBtnTxt = this.mCxt.getResources().getString(AmigoWidgetResource.getIdentifierByString(this.mCxt, "amigo_edit_mode_rightbtn_txt"));
        }
        LayoutParams rParams = new LayoutParams(-2, -1);
        rParams.addRule(11, -1);
        rParams.addRule(15, -1);
        this.mRightBtn.setText(this.mRightBtnTxt);
        if (this.mTxtColor != null) {
            this.mRightBtn.setTextColor(this.mTxtColor);
        }
        addView(this.mRightBtn, rParams);
    }

    private void addLeftButton() {
        this.mLeftBtn = (AmigoButton) ((LayoutInflater) this.mCxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(AmigoWidgetResource.getIdentifierByLayout(this.mCxt, "amigo_edit_mode_btn"), this, false);
        if (TextUtils.isEmpty(this.mLeftBtnTxt)) {
            this.mLeftBtnTxt = this.mCxt.getResources().getString(AmigoWidgetResource.getIdentifierByString(this.mCxt, "amigo_edit_mode_leftbtn_txt"));
        }
        LayoutParams lParams = new LayoutParams(-2, -1);
        lParams.addRule(9, -1);
        lParams.addRule(15, -1);
        this.mLeftBtn.setText(this.mLeftBtnTxt);
        if (this.mTxtColor != null) {
            this.mLeftBtn.setTextColor(this.mTxtColor);
        }
        addView(this.mLeftBtn, lParams);
    }

    private void initClickListener() {
        this.mLeftBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (AmigoEditModeView.this.mClickListener != null) {
                    AmigoEditModeView.this.mClickListener.leftBtnClick();
                }
            }
        });
        this.mRightBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (AmigoEditModeView.this.mClickListener != null) {
                    AmigoEditModeView.this.mClickListener.rightBtnClick();
                }
            }
        });
    }

    public void setEditModeBackgroud(int color) {
        setBackgroundColor(color);
    }

    public void setEditModeTextColor(int color) {
        this.mTxtColor = ColorStateList.valueOf(color);
        this.mLeftBtn.setTextColor(color);
        this.mRightBtn.setTextColor(color);
    }

    public void setEditModeTextColor(ColorStateList color) {
        if (color != null) {
            this.mTxtColor = color;
            this.mLeftBtn.setTextColor(color);
            this.mRightBtn.setTextColor(color);
        }
    }

    public void setEditModeBtnTxt(String leftbtntxt, String rightbtntxt) {
        this.mLeftBtn.setText(leftbtntxt);
        this.mRightBtn.setText(rightbtntxt);
    }

    public void setEditModeBtnClickListener(EditModeClickListener listener) {
        this.mClickListener = listener;
    }
}
