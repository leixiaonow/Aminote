package com.gionee.feedback.db;

import android.content.Context;

public final class ProviderFactory {
    public ProviderFactory() {
        throw new RuntimeException("Stub!");
    }

    public static IDraftProvider draftProvider(Context context) {
        return new DraftProvider(context);
    }

    public static ITokenProvider tokenProvider(Context context) {
        return new TokenProvider(context);
    }
}
