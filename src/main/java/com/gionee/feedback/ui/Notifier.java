package com.gionee.feedback.ui;

import android.annotation.TargetApi;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import com.gionee.res.Drawable;
import com.gionee.res.Text;
import com.gionee.res.Widget;

@TargetApi(11)
public class Notifier {
    public static final String GN_FB_INTENT_FROM_NOTIFICATION = "gn_fb_intent_from_notification";
    public static final String GN_FB_NOTIFICATION_EXTRAS = "gn_fb_notification_extras";

    public static void notify(Context context) {
        int icon = -1;
        try {
            icon = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128).icon;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        Builder builder = new Builder(context);
        if (icon == -1) {
            icon = Drawable.gn_fb_drawable_notification_icon.getIdentifier(context);
        }
        builder.setSmallIcon(icon);
        builder.setAutoCancel(true);
        Resources resources = context.getResources();
        builder.setTicker(resources.getString(Text.gn_fb_string_notification_ticker.getIdentifier(context)));
        builder.setContentTitle(resources.getString(Text.gn_fb_string_notification_title.getIdentifier(context)));
        builder.setContentText(resources.getString(Text.gn_fb_string_notification_content.getIdentifier(context)));
        Intent intent = new Intent(context, FeedBackActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(GN_FB_NOTIFICATION_EXTRAS, GN_FB_INTENT_FROM_NOTIFICATION);
        intent.addFlags(2);
        intent.putExtras(bundle);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 134217728));
        ((NotificationManager) context.getSystemService("notification")).notify(Widget.gn_fb_id_notification.getIdentifier(context), builder.build());
    }
}
