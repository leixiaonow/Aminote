package com.gionee.res;

public class Dimen extends AbsIdentifier {
    public static final Dimen gn_fb_dimen_attach_border_size = new Dimen("gn_fb_dimen_attach_border_size");

    protected Dimen(String name) {
        super(name);
    }

    protected String getType() {
        return "dimen";
    }
}
