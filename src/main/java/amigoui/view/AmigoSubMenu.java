package amigoui.view;

import android.graphics.drawable.Drawable;
import android.view.View;

public interface AmigoSubMenu extends AmigoMenu {
    void clearHeader();

    AmigoMenuItem getItem();

    AmigoSubMenu setHeaderIcon(int i);

    AmigoSubMenu setHeaderIcon(Drawable drawable);

    AmigoSubMenu setHeaderTitle(int i);

    AmigoSubMenu setHeaderTitle(CharSequence charSequence);

    AmigoSubMenu setHeaderView(View view);

    AmigoSubMenu setIcon(int i);

    AmigoSubMenu setIcon(Drawable drawable);
}
