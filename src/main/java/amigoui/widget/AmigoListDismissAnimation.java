package amigoui.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AmigoListDismissAnimation {
    private final int DURATION = 200;
    private AnimatorSet mAnimatorSet;
    private OnAmigoItemDismissCallback mCallback;
    private AbsListView mListView;
    private List<View> mViews;

    public AmigoListDismissAnimation(AbsListView listView) {
        this.mListView = listView;
    }

    public void setAmigoItemDismissCallback(OnAmigoItemDismissCallback callback) {
        this.mCallback = callback;
    }

    public void startAnimation(Collection<Integer> positions) {
        final List<Integer> positionsCopy = new ArrayList<>(positions);
        if (this.mListView == null) {
            throw new IllegalStateException("Call setListView() on this AnimateDismissAdapter before calling setAdapter()!");
        }
        this.mViews = getVisibleViewsForPositions(positionsCopy);
        if (!this.mViews.isEmpty()) {
            List<Animator> animators = new ArrayList<>();
            int pos = -2;
            for (View view : this.mViews) {
                int childPos = this.mListView.getPositionForView(view);
                boolean continu = pos == childPos + -1;
                pos = childPos;
                animators.add(createAnimatorForView(view, continu));
            }
            Animator[] animatorsArray = new Animator[animators.size()];
            for (int i = 0; i < animatorsArray.length; i++) {
                animatorsArray[i] = (Animator) animators.get(i);
            }
            this.mAnimatorSet = new AnimatorSet();
            this.mAnimatorSet.setDuration(200);
            this.mAnimatorSet.playTogether(animatorsArray);
            this.mAnimatorSet.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator arg0) {
                }

                public void onAnimationRepeat(Animator arg0) {
                }

                public void onAnimationEnd(Animator arg0) {
                    if (AmigoListDismissAnimation.this.mCallback != null) {
                        AmigoListDismissAnimation.this.invokeCallback(positionsCopy);
                    }
                }

                public void onAnimationCancel(Animator arg0) {
                }
            });
            this.mAnimatorSet.start();
        } else if (this.mCallback != null) {
            invokeCallback(positionsCopy);
        }
    }

    public void endAnimation() {
        if (this.mAnimatorSet != null && this.mAnimatorSet.isRunning()) {
            this.mAnimatorSet.end();
        }
    }

    private List<View> getVisibleViewsForPositions(Collection<Integer> positions) {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < this.mListView.getChildCount(); i++) {
            View child = this.mListView.getChildAt(i);
            if (positions.contains(this.mListView.getPositionForView(child))) {
                views.add(child);
            }
        }
        return views;
    }

    private Animator createAnimatorForView(final View view, boolean continu) {
        final LayoutParams lp = view.getLayoutParams();
        final int originalHeight = view.getHeight();
        final Drawable oriDrawable = view.getBackground();
        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0);
        final boolean z = continu;
        animator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator arg0) {
                if (z) {
                    view.setBackgroundResource(AmigoWidgetResource.getIdentifierByDrawable(view.getContext(), "amigo_listview_delete_bg"));
                } else {
                    view.setBackgroundResource(AmigoWidgetResource.getIdentifierByDrawable(view.getContext(), "amigo_listview_delete_top_bg"));
                }
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
                lp.height = originalHeight;
                view.setLayoutParams(lp);
                view.setBackground(oriDrawable);
            }

            public void onAnimationCancel(Animator arg0) {
            }
        });
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                view.setLayoutParams(lp);
            }
        });
        return animator;
    }

    private void invokeCallback(Collection<Integer> positions) {
        ArrayList<Integer> positionsList = new ArrayList<>(positions);
        Collections.sort(positionsList);
        int[] dismissPositions = new int[positionsList.size()];
        for (int i = 0; i < positionsList.size(); i++) {
            dismissPositions[i] = (Integer) positionsList.get((positionsList.size() - 1) - i);
        }
        this.mCallback.onDismiss(this.mListView, dismissPositions);
    }
}
