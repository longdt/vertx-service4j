package com.github.longdt.vertxservice.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.shareddata.Shareable;

import java.util.Collections;

public class ImmutableJsonArray extends JsonArray {
    private final Object[] objects;

    private ImmutableJsonArray(Object... objects) {
        super(Collections.emptyList());
        this.objects = objects;
    }

    public static ImmutableJsonArray of(Object... objects) {
        return new ImmutableJsonArray(objects);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(int index) {
        return (T) objects[index];
    }

    @Override
    public ImmutableJsonArray copy() {
        Object[] copies = new Object[objects.length];
        Object obj;
        for (int i = 0; i < copies.length; ++i) {
            obj = objects[i];
            if (obj instanceof Shareable) {
                obj = ((Shareable) obj).copy();
            }
            copies[i] = obj;
        }
        return new ImmutableJsonArray(copies);
    }
}
