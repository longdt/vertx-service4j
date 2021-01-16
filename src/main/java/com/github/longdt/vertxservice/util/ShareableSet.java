package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.HashSet;
import java.util.Set;

public class ShareableSet implements Shareable {
    private final Set<? extends Shareable> shareableSet;

    private ShareableSet(Set<? extends Shareable> shareableSet) {
        this.shareableSet = shareableSet;
    }

    public static <T extends Shareable> ShareableSet of(Set<T> shareableSet) {
        return new ShareableSet(shareableSet);
    }

    @SuppressWarnings("unchecked")
    public <T extends Shareable> Set<T> getObject() {
        return (Set<T>) shareableSet;
    }

    @Override
    public ShareableSet copy() {
        Set<Shareable> copiedSet = new HashSet<>(shareableSet.size());
        for (var shareable : shareableSet) {
            copiedSet.add(shareable.copy());
        }
        return new ShareableSet(copiedSet);
    }
}
