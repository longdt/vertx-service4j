package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.ArrayList;
import java.util.List;

public class ShareableList implements Shareable {
    private final List<? extends Shareable> shareableList;

    private ShareableList(List<? extends Shareable> shareableList) {
        this.shareableList = shareableList;
    }

    public static <T extends Shareable> ShareableList of(List<T> shareableList) {
        return new ShareableList(shareableList);
    }

    @SuppressWarnings("unchecked")
    public <T extends Shareable> List<T> getObject() {
        return (List<T>) shareableList;
    }

    @Override
    public ShareableList copy() {
        List<Shareable> copiedList = new ArrayList<>(shareableList.size());
        for (var shareable : shareableList) {
            copiedList.add(shareable.copy());
        }
        return new ShareableList(copiedList);
    }
}
