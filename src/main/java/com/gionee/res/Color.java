package com.gionee.res;

public class Color extends AbsIdentifier {
    public static final Color gn_fb_color_orange = new Color("gn_fb_color_orange");
    public static final Color gn_fb_color_sendbutton_disable_text = new Color("gn_fb_color_sendbutton_disable_text");
    public static final Color gn_fb_color_sendbutton_enable_text = new Color("gn_fb_color_sendbutton_enable_text");
    public static final Color gn_fb_string_attach_text_textColor = new Color("gn_fb_string_attach_text_textColor");

    protected Color(String name) {
        super(name);
    }

    protected String getType() {
        return "color";
    }
}
