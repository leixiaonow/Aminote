package com.gionee.res;

import android.content.Context;

public interface Identifier {
    int getIdentifier(Context context) throws ResourceNotFoundException;
}
