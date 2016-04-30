package amigoui.view;

import android.graphics.drawable.Drawable;
import android.view.View;

public interface AmigoContextMenu extends AmigoMenu {

    public interface ContextMenuInfo {
    }

    void clearHeader();

    AmigoContextMenu setHeaderIcon(int i);

    AmigoContextMenu setHeaderIcon(Drawable drawable);

    AmigoContextMenu setHeaderTitle(int i);

    AmigoContextMenu setHeaderTitle(CharSequence charSequence);

    AmigoContextMenu setHeaderView(View view);
}
