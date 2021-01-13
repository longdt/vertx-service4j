package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

public class Arguments implements Shareable {
    Object[] objects;

    Arguments() {
    }

    Arguments(Object... objects) {
        this.objects = objects;
    }

    public static Arguments of(Object... objects) {
        return new Arguments(objects);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(int index) {
        return (T) objects[index];
    }

    @Override
    public Arguments copy() {
        Object[] copies = new Object[objects.length];
        Object obj;
        for (int i = 0; i < copies.length; ++i) {
            obj = objects[i];
            if (obj instanceof Shareable) {
                obj = ((Shareable) obj).copy();
            }
            copies[i] = obj;
        }
        return new Arguments(copies);
    }
}
