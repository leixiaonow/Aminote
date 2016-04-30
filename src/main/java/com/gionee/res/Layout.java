package com.gionee.res;

public final class Layout extends AbsIdentifier {
    public static final Layout gn_fb_layout_customview = new Layout("gn_fb_layout_customview");
    public static final Layout gn_fb_layout_deleteattach = new Layout("gn_fb_layout_deleteattach");
    public static final Layout gn_fb_layout_expandtextview = new Layout("gn_fb_layout_expandtextview");
    public static final Layout gn_fb_layout_feedbackitem = new Layout("gn_fb_layout_feedbackitem");
    public static final Layout gn_fb_layout_mode_customview = new Layout("gn_fb_layout_mode_customview");
    public static final Layout gn_fb_layout_record = new Layout("gn_fb_layout_record");
    public static final Layout gn_fb_layout_replyitem = new Layout("gn_fb_layout_replyitem");
    public static final Layout gn_fb_layout_send = new Layout("gn_fb_layout_send");

    protected Layout(String name) {
        super(name);
    }

    protected String getType() {
        return "layout";
    }
}
