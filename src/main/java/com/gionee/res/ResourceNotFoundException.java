package com.gionee.res;

public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public ResourceNotFoundException(String type, String name) {
        super("resource " + name + " width " + type + " not found!!!");
    }
}
