package amigoui.widget;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import java.util.Set;

public interface AmigoMultiChoiceAdapter {
    void enterMultiChoiceMode();

    int getCheckedItemCount();

    Set<Long> getCheckedItems();

    boolean isChecked(long j);

    boolean isItemCheckable(int i);

    void save(Bundle bundle);

    void setAdapterView(AdapterView<? super BaseAdapter> adapterView);

    void setItemChecked(long j, boolean z);

    void setOnItemClickListener(OnItemClickListener onItemClickListener);
}
