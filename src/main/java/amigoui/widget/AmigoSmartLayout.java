package amigoui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public abstract class AmigoSmartLayout extends LinearLayout {
    public static final int HIGH_DEGREE = 90;
    public static final int LOW_DEGREE = 30;
    public static final int MIDDLE_DEGREE = 60;
    public static final int X_HIGH_DEGREE = 100;
    protected Context mContext;
    protected long mGoneTime;
    protected int mSmartLayoutDegree = 60;
    protected int mUserDegree = 30;
    protected long mVisibleTime;

    protected abstract int getUserDegree();

    protected abstract void processUserDegree();

    protected abstract boolean saveUserDegree(int i);

    public AmigoSmartLayout(Context context) {
        super(context);
        init(context);
    }

    public AmigoSmartLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mUserDegree = getUserDegree();
    }

    public void setSmartDegree(int degree) {
        this.mSmartLayoutDegree = degree;
    }

    public int getSmartDegree() {
        return this.mSmartLayoutDegree;
    }

    public long getVisibleTime() {
        if (this.mGoneTime > this.mVisibleTime) {
            return this.mGoneTime - this.mVisibleTime;
        }
        return 0;
    }

    public void setVisibility(int visibility) {
        switch (visibility) {
            case 0:
                this.mUserDegree = getUserDegree();
                if (this.mSmartLayoutDegree >= this.mUserDegree) {
                    this.mVisibleTime = System.currentTimeMillis();
                    break;
                }
                return;
        }
        super.setVisibility(visibility);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mSmartLayoutDegree < this.mUserDegree) {
            setVisibility(GONE);//8
        }
        this.mVisibleTime = System.currentTimeMillis();
    }
}
