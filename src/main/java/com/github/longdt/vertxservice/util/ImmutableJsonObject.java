package com.github.longdt.vertxservice.util;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

import java.util.Collections;

public class ImmutableJsonObject extends JsonObject {
    private final Shareable object;

    private ImmutableJsonObject(Shareable shareable) {
        super(Collections.emptyMap());
        this.object = shareable;
    }

    public static ImmutableJsonObject of(Shareable shareable) {
        return new ImmutableJsonObject(shareable);
    }

    @SuppressWarnings("unchecked")
    public <T extends Shareable> T getObject() {
        return (T) object;
    }

    @Override
    public ImmutableJsonObject copy() {
        return new ImmutableJsonObject(object.copy());
    }
}