package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.HashMap;
import java.util.Map;

public class ShareableMap implements Shareable {
    private Map<String, ? extends Shareable> shareableMap;

    private ShareableMap(Map<String, ? extends Shareable> shareableMap) {
        this.shareableMap = shareableMap;
    }

    public static <T extends Shareable> Shareable of(Map<String, T> shareableMap) {
        return new ShareableMap(shareableMap);
    }

    @SuppressWarnings("unchecked")
    public <T extends Shareable> Map<String, T> getObject() {
        return (Map<String, T>) shareableMap;
    }

    @Override
    public ShareableMap copy() {
        Map<String, Shareable> copiedMap = new HashMap<>(shareableMap.size());
        for (var entry : shareableMap.entrySet()) {
            copiedMap.put(entry.getKey(), entry.getValue().copy());
        }
        return new ShareableMap(copiedMap);
    }
}
