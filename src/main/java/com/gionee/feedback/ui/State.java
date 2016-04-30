package com.gionee.feedback.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class State {
    public static final State RECORD = new State("RECORD", 2);
    public static final State SEND = new State("SEND", 1);
    private static final List<State> STATES = new ArrayList();
    private String mName;
    private int mValue;

    private State(String name, int value) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        this.mName = name;
        this.mValue = value;
        STATES.add(this);
    }

    public String getName() {
        return this.mName;
    }

    public int value() {
        return this.mValue;
    }

    public static List<State> values() {
        return Collections.unmodifiableList(STATES);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof State)) {
            return false;
        }
        return this.mName.equals(((State) o).getName());
    }

    public int hashCode() {
        return this.mName.hashCode();
    }

    public String toString() {
        return this.mName;
    }
}
