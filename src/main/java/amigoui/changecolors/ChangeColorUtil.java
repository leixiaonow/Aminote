package amigoui.changecolors;

import android.widget.TextView;

public class ChangeColorUtil {
    private static final int ACCENT_COLOR = 0xffff9000;
    private static final int COLOR_FORTHLY_ON_APPBAR = 0x10ffffff;
    private static final int COLOR_PRIMARY_ON_APPBAR = 0xffffffff;
    private static final int COLOR_PRIMARY_ON_BACKGROUD = 0xcc000000;
    private static final int COLOR_SECONDARY_ON_APPBAR = 0xccffffff;
    private static final int COLOR_SECONDARY_ON_BACKGROUD = 0x66000000;
    private static final int COLOR_THIRDLY_ON_APPBAR = 0x50ffffff;
    private static final int COLOR_THIRDLY_ON_BACKGROUD = 0x33000000;

    public static void changeTextViewTextColor(TextView textView) {
    }

    private static int changeTextColor(int color) {
        if (color == COLOR_PRIMARY_ON_BACKGROUD) {
            return ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1();
        }
        if (color == COLOR_SECONDARY_ON_BACKGROUD) {
            return ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2();
        }
        if (color == COLOR_THIRDLY_ON_BACKGROUD) {
            return ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3();
        }
        if (color == COLOR_PRIMARY_ON_APPBAR) {
            return ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
        }
        if (color == COLOR_SECONDARY_ON_APPBAR) {
            return ChameleonColorManager.getContentColorSecondaryOnAppbar_T2();
        }
        if (color == COLOR_THIRDLY_ON_APPBAR) {
            return ChameleonColorManager.getContentColorThirdlyOnAppbar_T3();
        }
        if (color == COLOR_FORTHLY_ON_APPBAR) {
            return ChameleonColorManager.getContentColorForthlyOnAppbar_T4();
        }
        if (color == ACCENT_COLOR) {
            return ChameleonColorManager.getAccentColor_G1();
        }
        return color;
    }
}
