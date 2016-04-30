package amigoui.widget;

import android.util.Log;
import android.view.View;
import android.widget.ListView;
import java.util.HashMap;
import java.util.LinkedHashMap;
import uk.co.senab.photoview.IPhotoView;

public class AmigoViewPagerEffect {
    private final boolean DEBUG = true;
    private final float DISTANCE = 33.0f;
    private final String TAG = "AmigoViewPagerEffect->";
    private HashMap<Integer, Object> mObject = new LinkedHashMap();
    private State mState;
    private int oldPage;

    private enum State {
        IDLE,
        GOING_LEFT,
        GOING_RIGHT
    }

    public void effect(int currentItem, int position, float positionOffset, int positionOffsetPixels) {
        Log.d("AmigoViewPagerEffect->", "currentItem: " + currentItem);
        Log.d("AmigoViewPagerEffect->", "position: " + position);
        Log.d("AmigoViewPagerEffect->", "positionOffset: " + positionOffset);
        Log.d("AmigoViewPagerEffect->", "mObject size: " + this.mObject.size());
        if (this.mState == State.IDLE && positionOffset > 0.0f) {
            this.oldPage = currentItem;
            this.mState = position == this.oldPage ? State.GOING_LEFT : State.GOING_RIGHT;
        }
        boolean goingLeft = position == this.oldPage;
        if (this.mState == State.GOING_LEFT && !goingLeft) {
            this.mState = State.GOING_RIGHT;
        } else if (this.mState == State.GOING_RIGHT && goingLeft) {
            this.mState = State.GOING_LEFT;
        }
        float effectOffset = positionOffset;
        if (this.mState == State.GOING_LEFT) {
            Log.d("AmigoViewPagerEffect->", "going left: ");
            if (effectOffset > 0.998f) {
                effectOffset = IPhotoView.DEFAULT_MIN_SCALE;
            }
        } else if (this.mState == State.GOING_RIGHT) {
            Log.d("AmigoViewPagerEffect->", "going right: ");
        }
        View left = findViewFromObject(position);
        View right = findViewFromObject(position + 1);
        effectLeft(left, effectOffset);
        effectRight(right, effectOffset);
        if (positionOffset == 0.0f) {
            this.mState = State.IDLE;
            revert(right);
            revert(left);
        }
    }

    private void effectLeft(View left, float effectOffset) {
        if (left != null && (left instanceof ListView)) {
            ListView listView = (ListView) left;
            for (int i = 0; i < listView.getChildCount(); i++) {
                View child = listView.getChildAt(i);
                if (this.mState == State.GOING_RIGHT) {
                    child.setTranslationX((((-effectOffset) * ((float) i)) * ((float) i)) * 33.0f);
                }
            }
        }
    }

    private void effectRight(View right, float effectOffset) {
        if (right != null && (right instanceof ListView)) {
            ListView listView = (ListView) right;
            for (int i = 0; i < listView.getChildCount(); i++) {
                View child = listView.getChildAt(i);
                if (this.mState == State.GOING_LEFT) {
                    child.setTranslationX((((IPhotoView.DEFAULT_MIN_SCALE - effectOffset) * ((float) i)) * ((float) i)) * 33.0f);
                }
            }
        }
    }

    private void revert(View view) {
        if (view != null && (view instanceof ListView)) {
            ListView listView = (ListView) view;
            for (int i = 0; i < listView.getChildCount(); i++) {
                listView.getChildAt(i).setTranslationX(0.0f);
            }
        }
    }

    public void setObjectForPosition(Object obj, int position) {
        if (obj instanceof AmigoListView) {
            AmigoListView listView = (AmigoListView) obj;
            listView.setViewPagerEffectEnable(true);
            if (listView.getDivider() != null) {
                listView.setDivider(null);
                listView.setDividerHeight(0);
                listView.setModifiedDiveder(true);
            }
        }
        this.mObject.put(Integer.valueOf(position), obj);
    }

    public Object getObjectForPosition(int position) {
        return this.mObject.get(Integer.valueOf(position));
    }

    public View findViewFromObject(int position) {
        return (View) this.mObject.get(Integer.valueOf(position));
    }
}
