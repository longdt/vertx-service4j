package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.HashMap;
import java.util.Map;

public class ShareableMap<K extends Shareable, V extends Shareable> implements Shareable {
    private final Map<K, V> data;

    private ShareableMap(Map<K, V> data) {
        this.data = data;
    }

    public static <K extends Shareable, V extends Shareable> ShareableMap<K, V> of(Map<K, V> data) {
        return new ShareableMap<>(data);
    }

    public Map<K, V> unwrap() {
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ShareableMap<K, V> copy() {
        Map<K, V> copiedMap = new HashMap<>(data.size());
        for (var entry : data.entrySet()) {
            copiedMap.put((K) entry.getKey().copy(), (V) entry.getValue().copy());
        }
        return new ShareableMap<>(copiedMap);
    }
}
