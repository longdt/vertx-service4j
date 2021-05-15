package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.ArrayList;
import java.util.List;

public class ShareableList<E extends Shareable> implements Shareable {
    private final List<E> data;

    private ShareableList(List<E> data) {
        this.data = data;
    }

    public static <E extends Shareable> ShareableList<E> of(List<E> data) {
        return new ShareableList<>(data);
    }

    public List<E> unwrap() {
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ShareableList<E> copy() {
        List<E> copiedList = new ArrayList<>(data.size());
        for (var shareable : data) {
            copiedList.add(shareable != null ? (E) shareable.copy() : null);
        }
        return new ShareableList<>(copiedList);
    }
}
