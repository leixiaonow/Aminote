package com.gionee.appupgrade.common;

public class FactoryAppUpgrade {
    private static IGnAppUpgrade sIGnAppUpgrade = null;

    private FactoryAppUpgrade() {
    }

    public static IGnAppUpgrade getGnAppUpgrade() {
        if (sIGnAppUpgrade == null) {
            sIGnAppUpgrade = new GnAppUpgradeImple();
        }
        return sIGnAppUpgrade;
    }

    public static void destoryGnAppUpgrade() {
        sIGnAppUpgrade = null;
    }
}
