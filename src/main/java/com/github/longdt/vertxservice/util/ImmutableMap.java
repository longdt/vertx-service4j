package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.HashMap;
import java.util.Map;

public class ImmutableMap<K, V> implements Shareable {
    private final Map<K, V> data;

    private ImmutableMap(Map<K, V> data) {
        this.data = data;
    }

    public static <K, V> ImmutableMap<K, V> of(Map<K, V> data) {
        return new ImmutableMap<>(data);
    }

    public Map<K, V> unwrap() {
        return data;
    }

    @Override
    public ImmutableMap<K, V> copy() {
        return new ImmutableMap<>(new HashMap<>(data));
    }
}