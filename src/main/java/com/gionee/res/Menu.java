package com.gionee.res;

public class Menu extends AbsIdentifier {
    public static final Menu gn_fb_menu_attach_choice = new Menu("gn_fb_menu_attach_choice");
    public static final Menu gn_fb_menu_multi_choice = new Menu("gn_fb_menu_multi_choice");

    protected Menu(String name) {
        super(name);
    }

    protected String getType() {
        return "menu";
    }
}
