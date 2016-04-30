package com.gionee.res;

import amigoui.changecolors.ColorConfigConstants;

public class Id extends AbsIdentifier {
    public static final Id gn_fb_menu_attach_choice_delete = new Id("gn_fb_menu_attach_choice_delete");

    protected Id(String name) {
        super(name);
    }

    protected String getType() {
        return ColorConfigConstants.ID;
    }
}
