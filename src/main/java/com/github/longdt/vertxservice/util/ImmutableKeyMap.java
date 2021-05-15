package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.HashMap;
import java.util.Map;

public class ImmutableKeyMap<K, V extends Shareable> implements Shareable {
    private final Map<K, V> data;

    private ImmutableKeyMap(Map<K, V> data) {
        this.data = data;
    }

    public static <K, V extends Shareable> ImmutableKeyMap<K, V> of(Map<K, V> data) {
        return new ImmutableKeyMap<>(data);
    }

    public Map<K, V> unwrap() {
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableKeyMap<K, V> copy() {
        Map<K, V> copiedMap = new HashMap<>(data.size());
        for (var entry : data.entrySet()) {
            copiedMap.put(entry.getKey(), (V) entry.getValue().copy());
        }
        return new ImmutableKeyMap<>(copiedMap);
    }
}