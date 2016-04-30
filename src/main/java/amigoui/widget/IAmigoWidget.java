package amigoui.widget;

import android.os.Bundle;

public interface IAmigoWidget {
    boolean canAddToGioneeLauncher();

    int getPermittedCount();

    void onAddToGioneeLauncher();

    void onDestroy();

    void onPauseWhenShown(int i);

    void onResumeWhenShown(int i);

    void onScroll(float f);

    void onScrollEnd(int i);

    boolean onScrollStart();

    void onStartDrag();

    void onStopDrag();

    void setScreen(int i);

    int[] setWigetPoi(int[] iArr);

    void showUpToLimit();

    void startCovered(int i);

    void stopCovered(int i);

    void updateView(Bundle bundle);
}
