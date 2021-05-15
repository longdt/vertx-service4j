package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.HashMap;
import java.util.Map;

public class ShareableKeyMap<K extends Shareable, V> implements Shareable {
    private final Map<K, V> data;

    private ShareableKeyMap(Map<K, V> data) {
        this.data = data;
    }

    public static <K extends Shareable, V> ShareableKeyMap<K, V> of(Map<K, V> data) {
        return new ShareableKeyMap<>(data);
    }

    public Map<K, V> unwrap() {
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ShareableKeyMap<K, V> copy() {
        Map<K, V> copiedMap = new HashMap<>(data.size());
        for (var entry : data.entrySet()) {
            copiedMap.put((K) entry.getKey().copy(), entry.getValue());
        }
        return new ShareableKeyMap<>(copiedMap);
    }
}
