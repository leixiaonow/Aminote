package amigoui.preference;

import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class AmigoCheckBoxAndClickPreference extends AmigoCheckBoxPreference {
    private View mImageView;
    private OnClickListener mRBtnClickListener;

    public AmigoCheckBoxAndClickPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_preference_checkbox_and_click"));
        setWidgetLayoutResource(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_preference_checkbox_and_click_right_btn"));
    }

    public AmigoCheckBoxAndClickPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842895);
    }

    public AmigoCheckBoxAndClickPreference(Context context) {
        this(context, null);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.mImageView = view.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_right_button"));
        if (this.mImageView != null) {
            this.mImageView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (AmigoCheckBoxAndClickPreference.this.mRBtnClickListener != null) {
                        AmigoCheckBoxAndClickPreference.this.mRBtnClickListener.onClick(v);
                    }
                }
            });
        }
    }

    public void setRBtnOnClickListener(OnClickListener listener) {
        this.mRBtnClickListener = listener;
    }
}
