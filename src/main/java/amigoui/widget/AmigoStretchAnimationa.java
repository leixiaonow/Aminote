package amigoui.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

public class AmigoStretchAnimationa {
    private final int DISTANCE = 40;
    private final int DURATION = 200;
    private final String TAG = "AmigoStretchAnimationa->";
    private List<ChildView> mChildren = new ArrayList();
    private List<Integer> mFromBottomHeight = new ArrayList();
    private List<Integer> mFromTopHeight = new ArrayList();
    private boolean mGoUp = true;
    private Interpolator mInterpolator = new LinearInterpolator();
    private boolean mLastUpdate = false;
    private int mMotionY = -1;
    private boolean mRunning = false;

    public class ChildView {
        final int MULTIPLE = 10;
        View mChild;
        int mCurHeight;
        int mDHeight;
        int mOriginLPheight = 0;
        int mRawHeight;
        int mToHeight;

        public ChildView(View child, int index, int count, int originHeight) {
            this.mChild = child;
            this.mOriginLPheight = originHeight;
//            Log.d("AmigoStretchAnimationa->", "mOriginLPheight->" + this.mOriginLPheight);
            int measuredHeight = child.getMeasuredHeight();
            this.mCurHeight = measuredHeight;
            this.mRawHeight = measuredHeight;
            this.mToHeight = (this.mRawHeight + ((this.mRawHeight * 3) / 10)) - ((((this.mRawHeight * 3) / 10) * index) / count);
            this.mDHeight = this.mToHeight - this.mRawHeight;
        }

        public void changeChildLayout() {
            if (this.mChild.getMeasuredHeight() != this.mCurHeight) {
                LayoutParams lp = this.mChild.getLayoutParams();
                lp.height = this.mCurHeight;
                this.mChild.setLayoutParams(lp);
            }
        }

        public void revertLayoutParams() {
            LayoutParams lp = this.mChild.getLayoutParams();
            lp.height = this.mOriginLPheight;
            this.mChild.setLayoutParams(lp);
        }

        public void computeCurHeight(int deltaY) {
            if (this.mCurHeight <= this.mToHeight) {
                if (Math.abs(deltaY) > 100) {
                    this.mCurHeight += this.mDHeight / 4;
                } else {
                    this.mCurHeight += 4;
                }
                if (this.mCurHeight > this.mToHeight) {
                    this.mCurHeight = this.mToHeight;
                }
            }
        }
    }

    public void addChildren(List<View> children) {
        if (children.size() > 0) {
            if (this.mGoUp && children.size() > this.mFromTopHeight.size()) {
                initOriginHeight(children, this.mFromTopHeight);
            }
            if (!this.mGoUp && children.size() > this.mFromBottomHeight.size()) {
                initOriginHeight(children, this.mFromBottomHeight);
            }
            this.mChildren.clear();
            int count = children.size();
            for (int i = 0; i < count; i++) {
                this.mChildren.add(new ChildView((View) children.get(i), i, count, (this.mGoUp ? (Integer) this.mFromTopHeight.get(i) : (Integer) this.mFromBottomHeight.get(i)).intValue()));
            }
        }
    }

    private void initOriginHeight(List<View> children, List<Integer> target) {
        int count = children.size();
        for (int i = target.size(); i < count; i++) {
            target.add(Integer.valueOf(((View) children.get(i)).getLayoutParams().height));
        }
    }

    public void overScroll(int motionY, int motionPostion, int deltaY) {
        this.mRunning = true;
        if (Math.abs(motionY - this.mMotionY) > 40 && motionPostion < this.mChildren.size()) {
            this.mMotionY = motionY;
            for (int i = 0; i < motionPostion; i++) {
                ChildView child = (ChildView) this.mChildren.get(i);
                if (!(child == null || child.mChild.getVisibility() == View.GONE)) {
                    child.computeCurHeight(deltaY);
                    child.changeChildLayout();
                }
            }
        }
    }

    public void revertViewSize() {
        for (int i = 0; i < this.mChildren.size(); i++) {
            ChildView child = (ChildView) this.mChildren.get(i);
            child.mCurHeight = child.mRawHeight;
            child.revertLayoutParams();
        }
        this.mRunning = false;
        this.mLastUpdate = true;
        this.mChildren.clear();
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    public boolean isGoUp() {
        return this.mGoUp;
    }

    public void setGoUp(boolean up) {
        this.mGoUp = up;
    }

    public boolean isLastUpdate() {
        boolean last = this.mLastUpdate;
        if (this.mLastUpdate) {
            this.mLastUpdate = false;
        }
        return last;
    }

    public void overAnimation(float increase, boolean autoOver) {
        int count;
        if (autoOver) {
            count = this.mChildren.size() / 2;
        } else {
            count = this.mChildren.size();
        }
        if (count != 0) {
            Animator[] animators = new Animator[count];
            for (int i = 0; i < animators.length; i++) {
                ChildView view = (ChildView) this.mChildren.get(i);
                if (autoOver) {
                    animators[i] = createAnimator(view, (int) (((float) view.mRawHeight) * increase));
                } else {
                    animators[i] = createAnimator(view, view.mCurHeight);
                }
            }
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(200);
            animatorSet.playTogether(animators);
            animatorSet.start();
        }
    }

    private Animator createAnimator(final ChildView view, int startHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(new int[]{startHeight, view.mRawHeight});
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                AmigoStretchAnimationa.this.mRunning = true;
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                view.mCurHeight = view.mRawHeight;
                view.revertLayoutParams();
                AmigoStretchAnimationa.this.mRunning = false;
                AmigoStretchAnimationa.this.mLastUpdate = true;
            }

            public void onAnimationCancel(Animator animation) {
            }
        });
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                view.mCurHeight = ((Integer) animation.getAnimatedValue()).intValue();
                view.changeChildLayout();
            }
        });
        return animator;
    }
}
