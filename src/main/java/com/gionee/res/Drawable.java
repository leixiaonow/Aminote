package com.gionee.res;

public class Drawable extends AbsIdentifier {
    public static final Drawable gn_fb_drawable_add_attach_bn = new Drawable("gn_fb_drawable_add_attach_bn");
    public static final Drawable gn_fb_drawable_historymenu = new Drawable("gn_fb_drawable_historymenu");
    public static final Drawable gn_fb_drawable_notification_icon = new Drawable("gn_fb_drawable_notification_icon");
    public static final Drawable gn_fb_drawable_sendbutton = new Drawable("gn_fb_drawable_sendbutton");
    public static final Drawable gn_fb_drawable_sendbutton_failed = new Drawable("gn_fb_drawable_sendbutton_failed");
    public static final Drawable gn_fb_drawable_subscript = new Drawable("gn_fb_drawable_subscript");
    public static final Drawable gn_fb_drawable_time_read = new Drawable("gn_fb_drawable_time_read");
    public static final Drawable gn_fb_drawable_time_unread = new Drawable("gn_fb_drawable_time_unread");

    protected Drawable(String name) {
        super(name);
    }

    protected String getType() {
        return "drawable";
    }
}
