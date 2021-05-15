package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShareableSet<E extends Shareable> implements Shareable {
    private final Set<E> data;

    private ShareableSet(Set<E> data) {
        this.data = data;
    }

    public static <E extends Shareable> ShareableSet<E> of(Set<E> data) {
        return new ShareableSet<>(data);
    }

    public Set<E> unwrap() {
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ShareableSet<E> copy() {
        Set<E> copiedList = new HashSet<>(data.size());
        for (var shareable : data) {
            copiedList.add(shareable != null ? (E) shareable.copy() : null);
        }
        return new ShareableSet<>(copiedList);
    }
}
