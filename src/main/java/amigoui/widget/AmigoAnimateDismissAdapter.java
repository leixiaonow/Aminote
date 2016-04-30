package amigoui.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AmigoAnimateDismissAdapter extends AmigoBaseAdapterDecorator {
    private OnAmigoItemDismissCallback mCallback;

    public AmigoAnimateDismissAdapter(BaseAdapter baseAdapter, OnAmigoItemDismissCallback callback) {
        super(baseAdapter);
        this.mCallback = callback;
    }

    public void animateDismiss(int index) {
        animateDismiss(Arrays.asList(new Integer[]{Integer.valueOf(index)}));
    }

    public void animateDismiss(Collection<Integer> positions) {
        final List<Integer> positionsCopy = new ArrayList<>(positions);
        if (getAbsListView() == null) {
            throw new IllegalStateException("Call setListView() on this AnimateDismissAdapter before calling setAdapter()!");
        }
        List<View> views = getVisibleViewsForPositions(positionsCopy);
        if (views.isEmpty()) {
            invokeCallback(positionsCopy);
            return;
        }
        int i;
        List<Animator> animators = new ArrayList<>();
        for (i = 0; i < views.size(); i++) {
            boolean continu;
            if (positionsCopy.contains(positionsCopy.get(i) - 1)) {
                continu = true;
            } else {
                continu = false;
            }
            animators.add(createAnimatorForView(views.get(i), continu));
        }
        AnimatorSet animatorSet = new AnimatorSet();
        Animator[] animatorsArray = new Animator[animators.size()];
        for (i = 0; i < animatorsArray.length; i++) {
            animatorsArray[i] = animators.get(i);
        }
        animatorSet.playTogether(animatorsArray);
        animatorSet.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator arg0) {
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
                AmigoAnimateDismissAdapter.this.invokeCallback(positionsCopy);
            }

            public void onAnimationCancel(Animator arg0) {
            }
        });
        animatorSet.start();
    }

    private void invokeCallback(Collection<Integer> positions) {
        ArrayList<Integer> positionsList = new ArrayList<>(positions);
        Collections.sort(positionsList);
        int[] dismissPositions = new int[positionsList.size()];
        for (int i = 0; i < positionsList.size(); i++) {
            dismissPositions[i] = positionsList.get((positionsList.size() - 1) - i);
        }
        this.mCallback.onDismiss(getAbsListView(), dismissPositions);
    }

    private List<View> getVisibleViewsForPositions(Collection<Integer> positions) {
        List<View> views = new ArrayList<>();
        List<Integer> positionsCopy = new ArrayList<>(positions);
        for (int i = 0; i < positions.size(); i++) {
            View child = getAbsListView().getChildAt(positionsCopy.get(i));
            if (child != null) {
                views.add(child);
            }
        }
        return views;
    }

    private Animator createAnimatorForView(final View view, boolean continu) {
        final LayoutParams lp = view.getLayoutParams();
        int originalHeight = view.getHeight();
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
                lp.height = 0;
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
}
