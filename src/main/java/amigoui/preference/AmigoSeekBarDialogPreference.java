package amigoui.preference;

import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

public class AmigoSeekBarDialogPreference extends AmigoDialogPreference {
    private static final String TAG = "SeekBarDialogPreference";
    private Drawable mMyIcon = getDialogIcon();

    public AmigoSeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_seekbar_dialog"));
        createActionButtons();
        setDialogIcon(null);
    }

    public void createActionButtons() {
        setPositiveButtonText(17039370);
        setNegativeButtonText(17039360);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ImageView iconView = (ImageView) view.findViewById(16908294);
        if (this.mMyIcon != null) {
            iconView.setImageDrawable(this.mMyIcon);
        } else {
            iconView.setVisibility(8);
        }
    }

    protected static SeekBar getSeekBar(View dialogView) {
        return (SeekBar) dialogView.findViewById(AmigoWidgetResource.getIdentifierById(dialogView.getContext(), "amigo_seekbar"));
    }
}
