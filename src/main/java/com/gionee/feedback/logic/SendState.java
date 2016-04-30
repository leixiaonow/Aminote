package com.gionee.feedback.logic;

public enum SendState {
    INITIAL(0),
    SENDING(1),
    SEND_SUCCESS(2),
    SEND_FAILED(3);
    
    private int mValue;

    private SendState(int value) {
        this.mValue = value;
    }

    public int value() {
        return this.mValue;
    }

    public static SendState getState(int value) {
        switch (value) {
            case 0:
                return INITIAL;
            case 1:
                return SENDING;
            case 2:
                return SEND_SUCCESS;
            case 3:
                return SEND_FAILED;
            default:
                return INITIAL;
        }
    }
}
